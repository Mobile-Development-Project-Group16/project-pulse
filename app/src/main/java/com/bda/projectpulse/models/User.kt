package com.bda.projectpulse.models

import com.google.firebase.Timestamp
import java.time.LocalDate

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)