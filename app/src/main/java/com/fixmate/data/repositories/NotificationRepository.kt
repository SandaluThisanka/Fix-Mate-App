package com.fixmate.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.fixmate.utils.Constants
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationRepository {
    suspend fun getFCMToken(): Result<String>
    suspend fun updateUserFCMToken(userId: String, token: String): Result<Unit>
    suspend fun sendBookingNotification(
        providerId: String,
        customerName: String,
        serviceName: String,
        bookingId: String
    ): Result<Unit>
    suspend fun sendBookingStatusUpdate(
        customerId: String,
        status: String,
        serviceName: String,
        bookingId: String
    ): Result<Unit>
    suspend fun sendMessageNotification(
        recipientId: String,
        senderName: String,
        message: String,
        chatId: String
    ): Result<Unit>
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fcm: FirebaseMessaging
) : NotificationRepository {

    override suspend fun getFCMToken(): Result<String> {
        return try {
            val token = fcm.token.await()
            Timber.d("FCM token retrieved: $token")
            Result.success(token)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get FCM token")
            Result.failure(e)
        }
    }

    override suspend fun updateUserFCMToken(userId: String, token: String): Result<Unit> {
        return try {
            // Update user document with new FCM token
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(
                    mapOf(
                        "fcmToken" to token,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Timber.d("FCM token updated for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update FCM token for user: $userId")
            Result.failure(e)
        }
    }

    override suspend fun sendBookingNotification(
        providerId: String,
        customerName: String,
        serviceName: String,
        bookingId: String
    ): Result<Unit> {
        return try {
            // Get provider's FCM token
            val providerDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(providerId)
                .get()
                .await()

            if (!providerDoc.exists()) {
                return Result.failure(Exception("Provider not found"))
            }

            val token = providerDoc.getString("fcmToken") ?: ""
            
            if (token.isEmpty()) {
                Timber.w("No FCM token found for provider: $providerId")
                return Result.success(Unit) // Don't fail if no token
            }

            // Create notification payload
            val notificationData = mapOf(
                "type" to "booking_request",
                "bookingId" to bookingId,
                "customerName" to customerName,
                "serviceName" to serviceName,
                "providerId" to providerId,
                "timestamp" to System.currentTimeMillis().toString()
            )

            // Store notification in database for your Express.js backend to process
            firestore.collection("pending_notifications")
                .add(
                    mapOf(
                        "token" to token,
                        "title" to "New Booking Request",
                        "body" to "$customerName has requested your $serviceName service",
                        "data" to notificationData,
                        "createdAt" to System.currentTimeMillis(),
                        "processed" to false
                    )
                )
                .await()

            Timber.d("Booking notification queued for provider: $providerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send booking notification")
            Result.failure(e)
        }
    }

    override suspend fun sendBookingStatusUpdate(
        customerId: String,
        status: String,
        serviceName: String,
        bookingId: String
    ): Result<Unit> {
        return try {
            val customerDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(customerId)
                .get()
                .await()

            if (!customerDoc.exists()) {
                return Result.failure(Exception("Customer not found"))
            }

            val token = customerDoc.getString("fcmToken") ?: ""
            
            if (token.isEmpty()) {
                Timber.w("No FCM token found for customer: $customerId")
                return Result.success(Unit)
            }

            val notificationData = mapOf(
                "type" to "booking_status_update",
                "bookingId" to bookingId,
                "status" to status,
                "serviceName" to serviceName,
                "customerId" to customerId,
                "timestamp" to System.currentTimeMillis().toString()
            )

            firestore.collection("pending_notifications")
                .add(
                    mapOf(
                        "token" to token,
                        "title" to "Booking Update",
                        "body" to "Your $serviceName booking has been $status",
                        "data" to notificationData,
                        "createdAt" to System.currentTimeMillis(),
                        "processed" to false
                    )
                )
                .await()

            Timber.d("Booking status notification queued for customer: $customerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send booking status notification")
            Result.failure(e)
        }
    }

    override suspend fun sendMessageNotification(
        recipientId: String,
        senderName: String,
        message: String,
        chatId: String
    ): Result<Unit> {
        return try {
            val recipientDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(recipientId)
                .get()
                .await()

            if (!recipientDoc.exists()) {
                return Result.failure(Exception("Recipient not found"))
            }

            val token = recipientDoc.getString("fcmToken") ?: ""
            
            if (token.isEmpty()) {
                Timber.w("No FCM token found for recipient: $recipientId")
                return Result.success(Unit)
            }

            val notificationData = mapOf(
                "type" to "message",
                "chatId" to chatId,
                "senderId" to recipientId, // The sender from recipient's perspective
                "senderName" to senderName,
                "message" to message,
                "timestamp" to System.currentTimeMillis().toString()
            )

            firestore.collection("pending_notifications")
                .add(
                    mapOf(
                        "token" to token,
                        "title" to "New Message from $senderName",
                        "body" to message,
                        "data" to notificationData,
                        "createdAt" to System.currentTimeMillis(),
                        "processed" to false
                    )
                )
                .await()

            Timber.d("Message notification queued for recipient: $recipientId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message notification")
            Result.failure(e)
        }
    }
}
