package com.bda.projectpulse.data.repository

import com.bda.projectpulse.data.api.OpenRouterApi
import com.bda.projectpulse.data.models.AIChatMessage
import com.bda.projectpulse.data.models.AIChatRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatRepository @Inject constructor(
    private val api: OpenRouterApi
) {
    suspend fun sendMessage(
        apiKey: String,
        messages: List<AIChatMessage>,
        projectId: String,
        projectName: String
    ) = api.chatCompletion(
        apiKey = "Bearer $apiKey",
        referer = "https://projectpulse.com/projects/$projectId",
        title = "ProjectPulse - $projectName",
        request = AIChatRequest(messages = messages)
    )
} 