package com.bda.projectpulse.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

enum class UserRole {
    ADMIN,
    MANAGER,
    USER
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
    var role: UserRole = UserRole.USER,
    
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp = Timestamp.now(),
    
    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt: Timestamp = Timestamp.now()
) 