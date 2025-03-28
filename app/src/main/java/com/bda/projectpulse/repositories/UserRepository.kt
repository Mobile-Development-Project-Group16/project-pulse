package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    fun getUsers(): Flow<List<User>> = callbackFlow {
        val subscription = usersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(uid = doc.id)
                } ?: emptyList()

                trySend(users)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun getUserById(userId: String): Result<User?> = try {
        val doc = usersCollection.document(userId).get().await()
        Result.success(doc.toObject(User::class.java)?.copy(uid = doc.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createUser(user: User): Result<String> = try {
        val userWithTimestamp = user.copy(
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        val docRef = usersCollection.add(userWithTimestamp).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUser(userId: String, user: User): Result<Unit> = try {
        val userWithTimestamp = user.copy(
            updatedAt = Timestamp.now()
        )
        usersCollection.document(userId).set(userWithTimestamp).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteUser(userId: String): Result<Unit> = try {
        usersCollection.document(userId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 