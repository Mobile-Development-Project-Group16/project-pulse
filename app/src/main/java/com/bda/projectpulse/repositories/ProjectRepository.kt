package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.ProjectStatus
import com.bda.projectpulse.models.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
class ProjectRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val projectsCollection = firestore.collection("projects")

    suspend fun getProjects(userRole: UserRole? = null): Flow<List<Project>> = callbackFlow {
        println("ProjectRepository: Setting up projects listener")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        // If the user is an admin, we'll show all projects
        val isAdmin = userRole == UserRole.ADMIN
        println("ProjectRepository: User role is ${userRole?.name}, isAdmin: $isAdmin")
        
        // Query for all projects (for admin) or just the user's projects
        val subscription = projectsCollection
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("ProjectRepository: Error in projects listener: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                println("ProjectRepository: Received projects snapshot")
                println("ProjectRepository: Snapshot metadata: ${snapshot?.metadata}")
                println("ProjectRepository: Snapshot size: ${snapshot?.size()}")
                
                val projects = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            println("ProjectRepository: Processing document: ${doc.id}")
                            
                            val ownerId = (data["ownerId"] as? String) ?: (data["createdBy"] as? String) ?: ""
                            val teamMembers = (data["teamMembers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            
                            // Include project if:
                            // 1. User is admin (can see all projects), OR
                            // 2. User is the owner or a team member
                            if (isAdmin || ownerId == currentUserId || teamMembers.contains(currentUserId)) {
                                val project = Project(
                                    id = doc.id,
                                    name = data["name"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    status = data["status"]?.let { status ->
                                        try {
                                            ProjectStatus.valueOf(status.toString())
                                        } catch (e: Exception) {
                                            println("ProjectRepository: Error parsing status: ${e.message}")
                                            ProjectStatus.PLANNING
                                        }
                                    } ?: ProjectStatus.PLANNING,
                                    startDate = (data["startDate"] as? Timestamp) ?: Timestamp.now(),
                                    endDate = (data["endDate"] as? Timestamp),
                                    ownerId = ownerId,
                                    teamMembers = teamMembers
                                )
                                println("ProjectRepository: Successfully parsed project: ${project.name}")
                                project
                            } else {
                                println("ProjectRepository: Project ${doc.id} not visible to user $currentUserId")
                                null
                            }
                        } else {
                            println("ProjectRepository: Document data is null for doc: ${doc.id}")
                            null
                        }
                    } catch (e: Exception) {
                        println("ProjectRepository: Error parsing project document: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()

                println("ProjectRepository: Emitting ${projects.size} projects")
                trySend(projects)
            }

        awaitClose { 
            println("ProjectRepository: Closing projects listener")
            subscription.remove() 
        }
    }

    fun getProjectById(projectId: String): Flow<Project?> {
        return flow {
            val document = projectsCollection.document(projectId).get().await()
            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    emit(Project(
                        id = document.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        status = try {
                            ProjectStatus.valueOf((data["status"] as? String) ?: ProjectStatus.PLANNING.name)
                        } catch (e: Exception) {
                            ProjectStatus.PLANNING
                        },
                        startDate = (data["startDate"] as? Timestamp) ?: Timestamp.now(),
                        endDate = (data["endDate"] as? Timestamp),
                        ownerId = (data["ownerId"] as? String) ?: (data["createdBy"] as? String) ?: "",
                        teamMembers = (data["teamMembers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    ))
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        }
    }

    fun getUserProjects(userId: String): Flow<List<Project>> {
        return callbackFlow {
            val subscription = projectsCollection
                .whereArrayContains("teamMembers", userId)
                .orderBy("startDate", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val projects = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val data = doc.data
                            if (data != null) {
                                Project(
                                    id = doc.id,
                                    name = data["name"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    status = data["status"]?.let { status ->
                                        try {
                                            ProjectStatus.valueOf(status.toString())
                                        } catch (e: Exception) {
                                            ProjectStatus.PLANNING
                                        }
                                    } ?: ProjectStatus.PLANNING,
                                    startDate = (data["startDate"] as? Timestamp) ?: Timestamp.now(),
                                    endDate = (data["endDate"] as? Timestamp),
                                    ownerId = (data["ownerId"] as? String) ?: (data["createdBy"] as? String) ?: "",
                                    teamMembers = (data["teamMembers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                )
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    trySend(projects)
                }

            awaitClose { subscription.remove() }
        }
    }

    suspend fun createProject(project: Project, userRole: String): Result<String> {
        return try {
            // Check if user has permission to create projects
            if (userRole != "ADMIN" && userRole != "MANAGER") {
                return Result.failure(Exception("Only administrators and managers can create projects"))
            }
            
            println("ProjectRepository: Creating project with name: ${project.name}")
            println("ProjectRepository: Project data: $project")
            
            val db = FirebaseFirestore.getInstance()
            if (db == null) {
                println("ProjectRepository: ERROR - Firestore instance is null")
                return Result.failure(Exception("Firestore instance is null"))
            }

            val projectsCollection = db.collection("projects")
            val newProjectRef = projectsCollection.document()
            
            // Create a map of data to store - add both ownerId and createdBy fields
            val projectData = mapOf(
                "id" to newProjectRef.id,
                "name" to project.name,
                "description" to project.description,
                "status" to project.status.name,
                "startDate" to project.startDate,
                "endDate" to project.endDate,
                "ownerId" to project.ownerId,
                "createdBy" to project.ownerId, // Save as createdBy as well
                "teamMembers" to project.teamMembers
            )
            
            println("ProjectRepository: Generated new project ID: ${newProjectRef.id}")
            println("ProjectRepository: Adding project to Firestore")
            
            newProjectRef.set(projectData).await()
            
            println("ProjectRepository: Project created successfully with ID: ${newProjectRef.id}")
            Result.success(newProjectRef.id)
        } catch (e: Exception) {
            println("ProjectRepository: Error creating project: ${e.message}")
            println("ProjectRepository: Error type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateProject(projectId: String, project: Project): Result<Unit> {
        try {
            val projectData = mapOf(
                "name" to project.name,
                "description" to project.description,
                "status" to project.status.name,
                "startDate" to project.startDate,
                "endDate" to project.endDate,
                "ownerId" to project.ownerId,
                "createdBy" to project.ownerId,
                "teamMembers" to project.teamMembers
            )
            projectsCollection.document(projectId).update(projectData).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        try {
            projectsCollection.document(projectId).delete().await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun updateProjectStatus(projectId: String, status: ProjectStatus) {
        projectsCollection.document(projectId)
            .update(mapOf(
                "status" to status.name,
                "updatedAt" to Timestamp.now()
            ))
            .await()
    }

    suspend fun addTeamMember(projectId: String, userId: String): Result<Unit> {
        try {
            projectsCollection.document(projectId).update(
                mapOf(
                    "teamMembers" to com.google.firebase.firestore.FieldValue.arrayUnion(userId),
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun removeTeamMember(projectId: String, userId: String): Result<Unit> {
        try {
            projectsCollection.document(projectId).update(
                mapOf(
                    "teamMembers" to com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
} 