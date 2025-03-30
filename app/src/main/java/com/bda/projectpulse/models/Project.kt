package com.bda.projectpulse.models

import com.google.firebase.Timestamp

data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp? = null,
    val status: ProjectStatus = ProjectStatus.PLANNING,
    val teamMembers: List<String> = emptyList()
)

enum class ProjectStatus {
    TODO, // For backward compatibility
    PLANNING,
    ACTIVE,
    COMPLETED,
    ON_HOLD,
    CANCELLED
}

enum class ProjectPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
} 