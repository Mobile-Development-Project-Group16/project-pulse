package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")

    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null
        val userDoc = usersCollection.document(currentUser.uid).get().await()
        
        return User(
            uid = currentUser.uid,
            email = currentUser.email ?: "",
            displayName = currentUser.displayName ?: "",
            photoUrl = currentUser.photoUrl?.toString(),
            role = UserRole.valueOf(userDoc.getString("role") ?: "USER")
        )
    }

    fun getUsers(): Flow<List<User>> = flow {
        try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
            emit(users)
        } catch (e: Exception) {
            throw e
        }
    }

    fun getUserById(userId: String): Flow<User?> = flow {
        try {
            val snapshot = usersCollection.document(userId).get().await()
            emit(snapshot.toObject(User::class.java))
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createUser(user: User) {
        try {
            // Create a mutable map and add entries
            val userData = mutableMapOf<String, Any>()
            userData["uid"] = user.uid
            userData["email"] = user.email ?: ""
            userData["displayName"] = user.displayName ?: ""
            if (user.photoUrl != null) {
                userData["photoUrl"] = user.photoUrl
            }
            userData["role"] = user.role.name
            userData["createdAt"] = user.createdAt ?: Timestamp.now()
            userData["updatedAt"] = user.updatedAt ?: Timestamp.now()
            
            // Set the document with the map
            usersCollection.document(user.uid).set(userData).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUser(user: User) {
        try {
            // Create a mutable map and add entries
            val updates = mutableMapOf<String, Any>()
            updates["uid"] = user.uid
            updates["email"] = user.email ?: ""
            updates["displayName"] = user.displayName ?: ""
            if (user.photoUrl != null) {
                updates["photoUrl"] = user.photoUrl
            }
            updates["role"] = user.role.name
            updates["updatedAt"] = Timestamp.now()
            
            // Update the document with the map
            usersCollection.document(user.uid).update(updates).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteUser(userId: String) {
        try {
            usersCollection.document(userId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun searchUsersByEmail(query: String): List<User> {
        val snapshot = usersCollection
            .whereGreaterThanOrEqualTo("email", query)
            .whereLessThanOrEqualTo("email", query + "\uf8ff")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                User(
                    uid = doc.id,
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    photoUrl = doc.getString("photoUrl")
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun updateUserProfile(userId: String, photoUrl: String? = null) {
        val updates = mutableMapOf<String, Any>()
        photoUrl?.let { updates["photoUrl"] = it }
        
        if (updates.isNotEmpty()) {
            usersCollection.document(userId).update(updates).await()
        }
    }
} 