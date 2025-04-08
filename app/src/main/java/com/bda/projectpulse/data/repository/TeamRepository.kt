package com.bda.projectpulse.data.repository

import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getTeamMembers(projectId: String): List<User> {
        return firestore.collection("projects")
            .document(projectId)
            .collection("team_members")
            .get()
            .await()
            .toObjects(User::class.java)
    }

    suspend fun addTeamMember(projectId: String, userId: String, role: UserRole) {
        val userRef = firestore.collection("users").document(userId)
        val user = userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")

        val teamMember = user.copy(role = role)
        
        firestore.collection("projects")
            .document(projectId)
            .collection("team_members")
            .document(userId)
            .set(teamMember)
            .await()
    }

    suspend fun removeTeamMember(projectId: String, userId: String) {
        firestore.collection("projects")
            .document(projectId)
            .collection("team_members")
            .document(userId)
            .delete()
            .await()
    }

    suspend fun updateTeamMemberRole(projectId: String, userId: String, newRole: UserRole) {
        firestore.collection("projects")
            .document(projectId)
            .collection("team_members")
            .document(userId)
            .update("role", newRole)
            .await()
    }
} 