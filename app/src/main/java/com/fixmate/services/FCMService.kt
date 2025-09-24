package com.fixmate.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fixmate.MainActivity
import com.fixmate.R
import com.fixmate.data.repositories.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    companion object {
        private const val CHANNEL_ID = "sevalk_notifications"
        private const val CHANNEL_NAME = "SevaLK Notifications"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token received: $token")
        
        // Update token in user document
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    authRepository.updateFCMToken(userId, token)
                    Timber.d("FCM token updated for user: $userId")
                } else {
                    Timber.w("No current user to update FCM token")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update FCM token")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Message received from: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Timber.d("Message notification body: ${notification.body}")
            showNotification(
                title = notification.title ?: "SevaLK",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        when (type) {
            "booking_request" -> {
                val customerName = data["customerName"] ?: "Unknown Customer"
                val serviceName = data["serviceName"] ?: "Service"
                val bookingId = data["bookingId"] ?: ""
                
                showNotification(
                    title = "New Booking Request",
                    body = "$customerName has requested your $serviceName service",
                    data = data
                )
            }
            "booking_status_update" -> {
                val status = data["status"] ?: "updated"
                val serviceName = data["serviceName"] ?: "Service"
                
                showNotification(
                    title = "Booking Update",
                    body = "Your $serviceName booking has been $status",
                    data = data
                )
            }
            "message" -> {
                val senderName = data["senderName"] ?: "Someone"
                val messageText = data["message"] ?: "sent you a message"
                
                showNotification(
                    title = "New Message from $senderName",
                    body = messageText,
                    data = data
                )
            }
            else -> {
                Timber.d("Unknown message type: $type")
            }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Add data to intent for navigation
            data["type"]?.let { putExtra("notification_type", it) }
            data["bookingId"]?.let { putExtra("booking_id", it) }
            data["chatId"]?.let { putExtra("chat_id", it) }
            data["senderId"]?.let { putExtra("sender_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for SevaLK app"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
