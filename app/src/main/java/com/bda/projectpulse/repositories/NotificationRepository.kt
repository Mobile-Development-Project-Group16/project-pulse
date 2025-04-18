package com.bda.projectpulse.repositories

import kotlinx.coroutines.flow.Flow
import com.bda.projectpulse.models.Notification

/**
 * Repository interface for managing notifications
 */
interface NotificationRepository {
    /**
     * Get notifications for a specific user
     */
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    
    /**
     * Create a new notification
     */
    suspend fun createNotification(notification: Notification)
    
    /**
     * Mark a notification as read
     */
    suspend fun markAsRead(notificationId: String)
    
    /**
     * Mark all notifications as read for a user
     */
    suspend fun markAllAsRead(userId: String)
    
    /**
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: String)
    
    /**
     * Subscribe to a topic for push notifications
     */
    suspend fun subscribeToTopic(topic: String)
    
    /**
     * Unsubscribe from a topic for push notifications
     */
    suspend fun unsubscribeFromTopic(topic: String)
    
    /**
     * Get count of unread notifications for a user
     */
    fun getUnreadNotificationCount(userId: String): Flow<Int>
} 