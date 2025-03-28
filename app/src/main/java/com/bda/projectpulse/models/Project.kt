package com.bda.projectpulse.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Project(
    @DocumentId
    val id: String = "",
    
    val name: String = "",
    val description: String = "",
    val status: ProjectStatus = ProjectStatus.PLANNING,
    val priority: ProjectPriority = ProjectPriority.MEDIUM,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val tags: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    @PropertyName("team_members")
    val teamMembers: List<String> = emptyList()
)

enum class ProjectStatus {
    PLANNING,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    CANCELLED
}

enum class ProjectPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
} 