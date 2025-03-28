package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.ProjectStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class ProjectRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val projectsCollection = firestore.collection("projects")

    fun getProjects(): Flow<List<Project>> = callbackFlow {
        val subscription = projectsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val projects = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Project::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(projects)
            }

        awaitClose { subscription.remove() }
    }

    fun getProjectById(projectId: String): Flow<Project?> = callbackFlow {
        val subscription = projectsCollection
            .document(projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val project = snapshot?.toObject(Project::class.java)?.copy(id = snapshot.id)
                trySend(project)
            }

        awaitClose { subscription.remove() }
    }

    fun getUserProjects(userId: String): Flow<List<Project>> = callbackFlow {
        val subscription = projectsCollection
            .whereArrayContains("team_members", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val projects = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Project::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(projects)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createProject(project: Project): Result<String> = try {
        val projectWithTimestamp = project.copy(
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        val docRef = projectsCollection.add(projectWithTimestamp).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProject(projectId: String, project: Project): Result<Unit> = try {
        val projectWithTimestamp = project.copy(
            updatedAt = Timestamp.now()
        )
        projectsCollection.document(projectId).set(projectWithTimestamp).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteProject(projectId: String): Result<Unit> = try {
        projectsCollection.document(projectId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProjectStatus(projectId: String, status: ProjectStatus): Result<Unit> = try {
        projectsCollection.document(projectId)
            .update("status", status, "updatedAt", Timestamp.now())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addTeamMember(projectId: String, userId: String): Result<Unit> = try {
        projectsCollection.document(projectId).update(
            "teamMembers", com.google.firebase.firestore.FieldValue.arrayUnion(userId),
            "updatedAt", Timestamp.now()
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun removeTeamMember(projectId: String, userId: String): Result<Unit> = try {
        projectsCollection.document(projectId).update(
            "teamMembers", com.google.firebase.firestore.FieldValue.arrayRemove(userId),
            "updatedAt", Timestamp.now()
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 