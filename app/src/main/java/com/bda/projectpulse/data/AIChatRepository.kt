package com.bda.projectpulse.data

import com.bda.projectpulse.data.api.OpenRouterApi
import com.bda.projectpulse.data.models.AIChatMessage
import com.bda.projectpulse.data.models.AIChatRequest
import com.bda.projectpulse.data.models.AIChatResponse
import com.bda.projectpulse.data.models.AIChatChoice
import com.bda.projectpulse.data.models.ModelConfig
import com.bda.projectpulse.data.models.AvailableModels
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch

@Singleton
class AIChatRepository @Inject constructor(
    private val openRouterApi: OpenRouterApi,
    private val apiKeyDataSource: ApiKeyDataSource,
    private val chatHistoryDataSource: ChatHistoryDataSource,
    private val modelConfigDataSource: ModelConfigDataSource,
    private val firestore: FirebaseFirestore
) {
    private val responseCache = mutableMapOf<String, AIChatResponse>()
    private val maxHistorySize = 10
    private val cacheCollection = firestore.collection("ai_responses")
    private val projectsCollection = firestore.collection("projects")

    fun sendMessage(projectId: String, message: String): Flow<AIChatResponse> = flow {
        // Get API key, history, active model, and project details in parallel
        val result = withContext(Dispatchers.IO) {
            val apiKey = apiKeyDataSource.getApiKey() 
                ?: throw Exception("API key not configured. Please configure your OpenRouter API key in Admin Settings.")
            val history = chatHistoryDataSource.getChatHistory(projectId)
                .takeLast(maxHistorySize)
            val model = modelConfigDataSource.activeModel.value
            val project = projectsCollection.document(projectId).get().await().let { doc ->
                if (!doc.exists()) throw Exception("Project not found")
                mapOf(
                    "name" to (doc.getString("name") ?: ""),
                    "description" to (doc.getString("description") ?: ""),
                    "status" to (doc.getString("status") ?: "")
                )
            }
            FourTuple(apiKey, history, model, project)
        }
        
        val apiKey = result.first
        val history = result.second
        val activeModel = result.third
        val project = result.fourth
        
        // Check in-memory cache first
        val cacheKey = "$projectId:$message:${activeModel.id}"
        responseCache[cacheKey]?.let { cachedResponse ->
            emit(cachedResponse)
            return@flow
        }

        // Check Firestore cache
        val firestoreResponse = withContext(Dispatchers.IO) {
            try {
                val doc = cacheCollection
                    .whereEqualTo("projectId", projectId)
                    .whereEqualTo("message", message)
                    .whereEqualTo("modelId", activeModel.id)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                doc?.let {
                    AIChatResponse(
                        id = it.getString("id") ?: "",
                        created = System.currentTimeMillis(),
                        choices = listOf(
                            AIChatChoice(
                                message = AIChatMessage(
                                    role = "assistant",
                                    content = it.getString("response") ?: ""
                                ),
                                finish_reason = "stop"
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        if (firestoreResponse != null) {
            responseCache[cacheKey] = firestoreResponse
            emit(firestoreResponse)
            return@flow
        }

        // Prepare messages for API call, including project context
        val systemMessage = AIChatMessage(
            role = "system",
            content = """You are an AI assistant for the project "${project["name"]}". 
                       Project Description: ${project["description"]}
                       Current Status: ${project["status"]}
                       
                       You should provide assistance and answer questions about this specific project.
                       When asked about "the project", refer to this project specifically.""".trimIndent()
        )
        
        val messages = listOf(systemMessage) + history.map { AIChatMessage(it.role, it.content) } + AIChatMessage("user", message)

        // Make API call
        val response = withContext(Dispatchers.IO) {
            try {
                openRouterApi.chatCompletion(
                    apiKey = "Bearer $apiKey",
                    referer = "https://projectpulse.com",
                    title = "ProjectPulse Chat",
                    request = AIChatRequest(
                        model = activeModel.id,
                        messages = messages,
                        temperature = 0.7,
                        max_tokens = 1000
                    )
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("401") == true -> "Invalid API key. Please check your OpenRouter API key in Admin Settings."
                    e.message?.contains("400") == true -> "Invalid request. Please check your model selection and try again."
                    e.message?.contains("429") == true -> "Rate limit exceeded. Please try again later."
                    e.message?.contains("timeout") == true -> "Request timed out. Please check your internet connection and try again."
                    else -> "Failed to connect to AI service: ${e.message}"
                }
                throw Exception(errorMessage)
            }
        }

        // Save messages to chat history first
        withContext(Dispatchers.IO) {
            try {
                // Save user message
                chatHistoryDataSource.saveMessage(
                    projectId = projectId,
                    message = ChatMessage(role = "user", content = message)
                )
                
                // Save AI response
                chatHistoryDataSource.saveMessage(
                    projectId = projectId,
                    message = ChatMessage(
                        role = "assistant", 
                        content = response.choices.first().message.content
                    )
                )

                // Cache in memory
                responseCache[cacheKey] = response

                // Cache in Firestore
                cacheCollection.add(
                    mapOf(
                        "projectId" to projectId,
                        "message" to message,
                        "modelId" to activeModel.id,
                        "response" to response.choices.first().message.content,
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Emit response immediately
        emit(response)
    }

    suspend fun clearChatHistory(projectId: String) {
        withContext(Dispatchers.IO) {
            chatHistoryDataSource.clearChatHistory(projectId)
            // Clear in-memory cache
            responseCache.keys.removeAll { it.startsWith("$projectId:") }
            // Clear Firestore cache
            try {
                cacheCollection
                    .whereEqualTo("projectId", projectId)
                    .get()
                    .await()
                    .documents
                    .forEach { it.reference.delete().await() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getChatHistory(projectId: String): List<ChatMessage> {
        return withContext(Dispatchers.IO) {
            chatHistoryDataSource.getChatHistory(projectId)
        }
    }
}

private data class FourTuple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) 