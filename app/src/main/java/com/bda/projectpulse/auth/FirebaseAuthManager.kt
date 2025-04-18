package com.bda.projectpulse.auth

import android.util.Log
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "FirebaseAuthManager"

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun getUserData(userId: String): User {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.copy(uid = userId)
                ?: throw Exception("User data not found")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user data", e)
            throw e
        }
    }

    suspend fun hasAdminUser(): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", UserRole.ADMIN.name)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for admin user", e)
            throw e
        }
    }

    fun signOut() {
        auth.signOut()
    }
} 