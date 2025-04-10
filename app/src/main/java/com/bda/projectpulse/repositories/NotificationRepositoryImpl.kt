package com.bda.projectpulse.repositories

import android.util.Log
import com.bda.projectpulse.models.Notification
import com.bda.projectpulse.models.NotificationType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging
) : NotificationRepository {
    private val notificationsCollection = db.collection("notifications")
    private val TAG = "NotificationRepo"

    override fun getNotificationsForUser(userId: String): Flow<List<Notification>> = flow {
        try {
            Log.d(TAG, "Getting notifications for user: $userId")
            val snapshot = notificationsCollection
                .whereEqualTo("recipientId", userId)
                .get()
                .await()
                
            val notifications = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.timestamp }
            
            Log.d(TAG, "Found ${notifications.size} notifications for user $userId")
            notifications.forEach { notification ->
                Log.d(TAG, "Notification: type=${notification.type}, title=${notification.title}, read=${notification.read}")
            }
            
            emit(notifications)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications", e)
            emit(emptyList<Notification>())
        }
    }

    override suspend fun createNotification(notification: Notification) {
        try {
            Log.d(TAG, "Creating notification: type=${notification.type}, for recipient=${notification.recipientId}, title=${notification.title}")
            val docRef = notificationsCollection.add(notification).await()
            Log.d(TAG, "Notification created successfully with ID: ${docRef.id}")
            // Send push notification will be implemented later
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            Log.d(TAG, "Marking notification as read: $notificationId")
            notificationsCollection.document(notificationId)
                .update("read", true)
                .await()
            Log.d(TAG, "Notification marked as read successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read", e)
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        try {
            Log.d(TAG, "Marking all notifications as read for user: $userId")
            val batch = db.batch()
            val notifications = notificationsCollection
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("read", false)
                .get()
                .await()

            Log.d(TAG, "Found ${notifications.size()} unread notifications to mark as read")
            notifications.documents.forEach { doc ->
                batch.update(doc.reference, "read", true)
            }

            batch.commit().await()
            Log.d(TAG, "All notifications marked as read successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all notifications as read", e)
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        try {
            Log.d(TAG, "Deleting notification: $notificationId")
            notificationsCollection.document(notificationId)
                .delete()
                .await()
            Log.d(TAG, "Notification deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification", e)
        }
    }

    override suspend fun subscribeToTopic(topic: String) {
        try {
            Log.d(TAG, "Subscribing to topic: $topic")
            messaging.subscribeToTopic(topic).await()
            Log.d(TAG, "Subscribed to topic successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topic", e)
        }
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        try {
            Log.d(TAG, "Unsubscribing from topic: $topic")
            messaging.unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from topic successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from topic", e)
        }
    }

    override fun getUnreadNotificationCount(userId: String): Flow<Int> {
        val countFlow = MutableStateFlow(0)
        
        try {
            Log.d(TAG, "Setting up listener for unread notification count for user: $userId")
            notificationsCollection
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("read", false)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed for unread count", e)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        countFlow.value = snapshot.size()
                        Log.d(TAG, "Unread notification count updated: ${snapshot.size()}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up unread count listener", e)
        }
            
        return countFlow.asStateFlow()
    }
} 