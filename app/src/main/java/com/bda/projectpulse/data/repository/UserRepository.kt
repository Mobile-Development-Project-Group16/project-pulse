package com.bda.projectpulse.data.repository

import com.bda.projectpulse.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null
        return firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun getAvailableUsers(): List<User> {
        return firestore.collection("users")
            .get()
            .await()
            .toObjects(User::class.java)
    }

    suspend fun getUserById(userId: String): User? {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        firestore.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }
} 