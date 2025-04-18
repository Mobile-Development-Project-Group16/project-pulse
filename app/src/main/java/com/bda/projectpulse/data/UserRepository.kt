package com.bda.projectpulse.data

import com.bda.projectpulse.models.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDataSource: UserDataSource
) {
    fun getUsers(): Flow<List<User>> {
        return userDataSource.getUsers()
    }

    fun getUserById(userId: String): Flow<User> {
        return userDataSource.getUserById(userId)
    }

    suspend fun getCurrentUser(): User? {
        return userDataSource.getCurrentUser()
    }

    suspend fun createUser(user: User) {
        userDataSource.createUser(user)
    }

    suspend fun updateUser(user: User) {
        userDataSource.updateUser(user)
    }

    suspend fun deleteUser(userId: String) {
        userDataSource.deleteUser(userId)
    }
} 