package com.bda.projectpulse.data

import com.bda.projectpulse.models.User
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    fun getUsers(): Flow<List<User>>
    fun getUserById(userId: String): Flow<User>
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
} 