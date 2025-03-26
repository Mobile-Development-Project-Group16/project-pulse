package com.bda.projectpulse.models

import com.google.firebase.firestore.PropertyName

enum class UserRole {
    ADMIN,
    PROJECT_MANAGER,
    TEAM_MEMBER
}

data class User(
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = "",
    
    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String = "",
    
    @get:PropertyName("displayName")
    @set:PropertyName("displayName")
    var displayName: String = "",
    
    @get:PropertyName("role")
    @set:PropertyName("role")
    var role: UserRole = UserRole.TEAM_MEMBER,
    
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),
    
    @get:PropertyName("lastLoginAt")
    @set:PropertyName("lastLoginAt")
    var lastLoginAt: Long = System.currentTimeMillis(),
    
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true
) 