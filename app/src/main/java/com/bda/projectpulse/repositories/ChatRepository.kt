package com.bda.projectpulse.repositories

import android.net.Uri
import com.bda.projectpulse.models.Attachment
import com.bda.projectpulse.models.AttachmentType
import com.bda.projectpulse.models.ChatMessage
import com.bda.projectpulse.models.Notification
import com.bda.projectpulse.models.NotificationType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val notificationRepository: NotificationRepository
) {
    fun getProjectMessages(projectId: String): Flow<List<ChatMessage>> = callbackFlow {
        val messagesRef = firestore.collection("projects")
            .document(projectId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val subscription = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    try {
                        val attachments = (doc.get("attachments") as? List<Map<String, Any>>)?.map { attachmentMap ->
                            Attachment(
                                id = attachmentMap["id"] as? String ?: "",
                                name = attachmentMap["name"] as? String ?: "",
                                url = attachmentMap["url"] as? String ?: "",
                                type = AttachmentType.valueOf(attachmentMap["type"] as? String ?: "OTHER"),
                                size = (attachmentMap["size"] as? Number)?.toLong() ?: 0L
                            )
                        } ?: emptyList()

                        ChatMessage(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            isFromCurrentUser = false,
                            attachments = attachments
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(messages)
            }
        }

        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(
        projectId: String,
        text: String,
        senderId: String,
        senderName: String,
        attachments: List<Uri> = emptyList()
    ) {
        val uploadedAttachments = attachments.map { uri ->
            uploadFile(projectId, uri)
        }

        val message = hashMapOf(
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "senderId" to senderId,
            "senderName" to senderName,
            "attachments" to uploadedAttachments
        )

        firestore.collection("projects")
            .document(projectId)
            .collection("messages")
            .add(message)
            .await()
            
        // Create notifications for all project members
        createChatNotifications(projectId, senderId, senderName, text)
    }
    
    private suspend fun createChatNotifications(
        projectId: String,
        senderId: String,
        senderName: String,
        messageText: String
    ) {
        try {
            // Get project info to get the project name
            val project = firestore.collection("projects")
                .document(projectId)
                .get()
                .await()
            
            val projectName = project.getString("name") ?: "Project"
            
            // Get all team members for this project to notify them
            val teamMembers = project.get("teamMembers") as? List<String> ?: emptyList()
            
            // Create and send notification to each team member except the sender
            teamMembers.forEach { memberId ->
                if (memberId != senderId) {
                    val notification = Notification(
                        id = "",
                        type = NotificationType.CHAT_MESSAGE,
                        title = "New message in $projectName",
                        message = "$senderName: ${if (messageText.length > 50) messageText.substring(0, 47) + "..." else messageText}",
                        recipientId = memberId,
                        senderId = senderId,
                        timestamp = Timestamp.now(),
                        read = false,
                        data = mapOf(
                            "projectId" to projectId
                        )
                    )
                    
                    notificationRepository.createNotification(notification)
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail the message send operation
            println("Error creating chat notifications: ${e.message}")
        }
    }

    private suspend fun uploadFile(projectId: String, uri: Uri): Map<String, Any> {
        val file = File(uri.path ?: "")
        val extension = file.extension.lowercase()
        val type = when (extension) {
            "jpg", "jpeg", "png", "gif" -> AttachmentType.IMAGE
            "pdf" -> AttachmentType.PDF
            "doc", "docx" -> AttachmentType.DOCUMENT
            "xls", "xlsx" -> AttachmentType.EXCEL
            else -> AttachmentType.OTHER
        }

        val fileName = "${System.currentTimeMillis()}_${file.name}"
        val storageRef = storage.reference
            .child("projects")
            .child(projectId)
            .child("attachments")
            .child(fileName)

        val uploadTask = storageRef.putFile(uri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()

        return mapOf(
            "id" to fileName,
            "name" to file.name,
            "url" to downloadUrl,
            "type" to type.name,
            "size" to file.length()
        )
    }

    suspend fun deleteMessage(projectId: String, messageId: String) {
        firestore.collection("projects")
            .document(projectId)
            .collection("messages")
            .document(messageId)
            .delete()
            .await()
    }
} 