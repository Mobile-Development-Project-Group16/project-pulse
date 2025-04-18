package com.bda.projectpulse.data.repository

import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    fun getUsers(): Flow<List<User>> = flow {
        val snapshot = firestore.collection("users")
            .get()
            .await()
        val users = snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(uid = doc.id)
        }
        emit(users)
    }

    suspend fun createUser(user: User, password: String) {
        // Validate required fields
        requireNotNull(user.email) { "Email is required" }
        
        // First create the auth user
        val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
        val uid = authResult.user?.uid ?: throw IllegalStateException("Failed to create auth user")
        
        // Then create the user document in Firestore
        val userWithId = user.copy(uid = uid)
        firestore.collection("users")
            .document(uid)
            .set(userWithId)
            .await()
    }

    suspend fun updateUser(user: User) {
        firestore.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }

    suspend fun deleteUser(userId: String) {
        // Delete from Firestore first
        firestore.collection("users")
            .document(userId)
            .delete()
            .await()
            
        // Then delete from Authentication
        // Note: This requires the user to have recently signed in
        // You might need to handle this differently in production
        try {
            auth.currentUser?.delete()?.await()
        } catch (e: Exception) {
            // Handle the error or rethrow if needed
            throw e
        }
    }
} 