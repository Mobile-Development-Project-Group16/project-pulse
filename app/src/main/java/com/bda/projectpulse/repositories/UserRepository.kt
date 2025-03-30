package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getCurrentUser(): User? {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            var currentUser: User? = null
            auth.currentUser?.let { firebaseUser ->
                getUserById(firebaseUser.uid).collect { user ->
                    currentUser = user
                }
            }
            return currentUser
        } catch (e: Exception) {
            return null
        }
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
            userData["email"] = user.email
            userData["displayName"] = user.displayName
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
            updates["email"] = user.email
            updates["displayName"] = user.displayName
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
} 