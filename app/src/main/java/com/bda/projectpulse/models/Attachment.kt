package com.bda.projectpulse.models

import com.google.firebase.Timestamp

data class Attachment(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val type: AttachmentType = AttachmentType.OTHER,
    val size: Long = 0L,
    val uploadedAt: Timestamp = Timestamp.now()
)

enum class AttachmentType {
    IMAGE,
    DOCUMENT,
    PDF,
    EXCEL,
    OTHER
} 