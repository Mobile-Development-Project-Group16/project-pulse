package com.bda.projectpulse.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthManager private constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "FirebaseAuthManager"

    companion object {
        @Volatile
        private var instance: FirebaseAuthManager? = null

        fun getInstance(): FirebaseAuthManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseAuthManager().also { instance = it }
            }
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = getUserData(firebaseUser.uid)
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String, role: UserRole): Result<User> {
        return try {
            // Check if trying to register as admin
            if (role == UserRole.ADMIN) {
                val hasAdmin = hasAdminUser()
                Log.d(TAG, "Checking for admin user. Has admin: $hasAdmin")
                if (hasAdmin) {
                    Log.d(TAG, "Admin user exists, preventing admin registration")
                    return Result.failure(Exception("Admin user already exists"))
                }
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    role = role
                )
                saveUserData(user)
                Log.d(TAG, "User registered successfully with role: ${user.role}")
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign up", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    private suspend fun saveUserData(user: User) {
        try {
            firestore.collection("users")
                .document(user.uid)
                .set(user)
                .await()
            Log.d(TAG, "User data saved successfully for user: ${user.uid}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data", e)
            throw e
        }
    }

    suspend fun getUserData(uid: String): User {
        return firestore.collection("users")
            .document(uid)
            .get()
            .await()
            .toObject(User::class.java) ?: throw Exception("User data not found")
    }

    suspend fun updateUserRole(uid: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update("role", newRole)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasAdminUser(): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", UserRole.ADMIN)
                .get()
                .await()
            val hasAdmin = !snapshot.isEmpty
            Log.d(TAG, "Admin user check result: $hasAdmin")
            hasAdmin
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for admin user", e)
            false
        }
    }
} 