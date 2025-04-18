package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val teamsCollection = db.collection("teams")

    suspend fun getTeamMembers(): List<User> {
        val currentTeam = teamsCollection.document("current").get().await()
        val memberIds = currentTeam.get("members") as? List<String> ?: emptyList()
        
        return memberIds.mapNotNull { userId ->
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                User(
                    uid = userId,
                    email = userDoc.getString("email") ?: "",
                    displayName = userDoc.getString("displayName") ?: "",
                    photoUrl = userDoc.getString("photoUrl")
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun addTeamMember(user: User) {
        val currentTeam = teamsCollection.document("current")
        val teamData = currentTeam.get().await()
        val currentMembers = teamData.get("members") as? List<String> ?: emptyList()
        
        if (!currentMembers.contains(user.uid)) {
            currentTeam.update("members", currentMembers + user.uid).await()
        }
    }

    suspend fun removeTeamMember(user: User) {
        val currentTeam = teamsCollection.document("current")
        val teamData = currentTeam.get().await()
        val currentMembers = teamData.get("members") as? List<String> ?: emptyList()
        
        currentTeam.update("members", currentMembers.filter { it != user.uid }).await()
    }
} 