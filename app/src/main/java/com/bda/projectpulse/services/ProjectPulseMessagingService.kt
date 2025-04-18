package com.bda.projectpulse.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bda.projectpulse.MainActivity
import com.bda.projectpulse.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service that handles Firebase Cloud Messaging (FCM) messages.
 * This implementation is simplified to avoid Hilt dependency issues.
 */
class ProjectPulseMessagingService : FirebaseMessagingService() {
    
    private val TAG = "FCMService"
    
    companion object {
        const val CHANNEL_ID = "project_pulse_notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")
        // In a production app, you would send this token to your server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        // Get notification data
        val title = message.notification?.title ?: "New Notification"
        val body = message.notification?.body ?: ""
        val type = message.data["type"]
        val taskId = message.data["taskId"]
        val projectId = message.data["projectId"]

        // Show a notification
        showNotification(title, body, taskId, projectId, type)
    }

    private fun showNotification(
        title: String, 
        body: String, 
        taskId: String?, 
        projectId: String?,
        type: String?
    ) {
        // Create notification channel for Android O and above
        createNotificationChannel()

        // Create intent for notification click
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation", "notifications")
            
            if (!taskId.isNullOrEmpty()) {
                putExtra("taskId", taskId)
            }
            
            if (!projectId.isNullOrEmpty()) {
                putExtra("projectId", projectId)
            }
            
            if (!type.isNullOrEmpty()) {
                putExtra("type", type)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        
        Log.d(TAG, "Notification displayed with ID: $notificationId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Project Pulse Notifications"
            val descriptionText = "Notifications for tasks, chat messages, and updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
} 