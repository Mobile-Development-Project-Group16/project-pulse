package com.bda.projectpulse.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val apiKeysCollection = firestore.collection("api_keys")

    suspend fun saveApiKey(apiKey: String): Boolean {
        return try {
            apiKeysCollection.document("openrouter")
                .set(mapOf("key" to apiKey))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getApiKey(): String? {
        return try {
            val snapshot = apiKeysCollection.document("openrouter")
                .get()
                .await()
            snapshot.getString("key")
        } catch (e: Exception) {
            null
        }
    }
} 