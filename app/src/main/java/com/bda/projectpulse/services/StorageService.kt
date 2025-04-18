package com.bda.projectpulse.services

import android.net.Uri
import com.bda.projectpulse.models.Attachment
import com.bda.projectpulse.models.AttachmentType
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Singleton

@Singleton
class StorageService(
    private val storage: FirebaseStorage
) {
    suspend fun uploadFile(
        projectId: String,
        uri: Uri,
        folder: String = "attachments"
    ): Attachment {
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
            .child(folder)
            .child(fileName)

        val uploadTask = storageRef.putFile(uri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()

        return Attachment(
            id = fileName,
            name = file.name,
            url = downloadUrl,
            type = type,
            size = file.length()
        )
    }

    suspend fun uploadTaskFile(
        projectId: String,
        taskId: String,
        uri: Uri
    ): Attachment {
        return uploadFile(projectId, uri, "tasks/$taskId")
    }

    suspend fun uploadChatFile(
        projectId: String,
        uri: Uri
    ): Attachment {
        return uploadFile(projectId, uri, "chat")
    }

    suspend fun deleteFile(projectId: String, filePath: String) {
        storage.reference
            .child("projects")
            .child(projectId)
            .child(filePath)
            .delete()
            .await()
    }
} 