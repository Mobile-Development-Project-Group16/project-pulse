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
        
        // Add to team_members subcollection
        firestore.collection("projects")
            .document(projectId)
            .collection("team_members")
            .document(userId)
            .set(teamMember)
            .await()
            
        // Update project's teamMembers array
        firestore.collection("projects")
            .document(projectId)
            .update(
                mapOf(
                    "teamMembers" to com.google.firebase.firestore.FieldValue.arrayUnion(userId),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .await()
    }

    suspend fun removeTeamMember(projectId: String, userId: String) {
        // Remove from team_members subcollection
        firestore.collection("projects")
            .document(projectId)
            .collection("team_members")
            .document(userId)
            .delete()
            .await()
            
        // Update project's teamMembers array
        firestore.collection("projects")
            .document(projectId)
            .update(
                mapOf(
                    "teamMembers" to com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )
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