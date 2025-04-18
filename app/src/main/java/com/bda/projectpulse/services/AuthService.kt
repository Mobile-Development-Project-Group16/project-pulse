package com.bda.projectpulse.services

import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun login(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("Login failed")
        
        val userDoc = firestore.collection("users")
            .document(user.uid)
            .get()
            .await()
            
        return userDoc.toObject(User::class.java)?.copy(uid = user.uid)
            ?: throw Exception("User data not found")
    }

    suspend fun register(email: String, password: String, displayName: String, role: UserRole): User {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("User creation failed")
        
        val userData = User(
            uid = user.uid,
            email = email,
            displayName = displayName,
            role = role
        )
        
        firestore.collection("users")
            .document(user.uid)
            .set(userData)
            .await()
            
        return userData
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            role = UserRole.USER // Default role, will be updated when user data is loaded
        )
    }
} 