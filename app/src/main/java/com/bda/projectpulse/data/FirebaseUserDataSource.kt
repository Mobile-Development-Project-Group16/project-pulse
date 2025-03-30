package com.bda.projectpulse.data

import com.bda.projectpulse.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataSource {

    private val usersCollection = firestore.collection("users")

    override fun getUsers(): Flow<List<User>> {
        return usersCollection.snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<User>() } }
    }

    override fun getUserById(userId: String): Flow<User> {
        return usersCollection.document(userId).snapshots()
            .map { snapshot -> snapshot.toObject<User>() ?: throw IllegalStateException("User not found") }
    }

    override suspend fun createUser(user: User) {
        val userData = user.copy(uid = usersCollection.document().id)
        usersCollection.document(userData.uid).set(userData).await()
    }

    override suspend fun updateUser(user: User) {
        usersCollection.document(user.uid).set(user).await()
    }

    override suspend fun deleteUser(userId: String) {
        usersCollection.document(userId).delete().await()
    }
} 