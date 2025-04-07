package com.bda.projectpulse.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(email: String, password: String, name: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("User creation failed")
        
        // Create user document in Firestore
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to "USER" // Default role
        )
        
        firestore.collection("users")
            .document(user.uid)
            .set(userData)
            .await()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun logout() {
        auth.signOut()
    }
} 