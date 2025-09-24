package com.fixmate.utils

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.fixmate.data.repositories.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository,
    private val fcm: FirebaseMessaging
) {
    
    /**
     * Initialize FCM token for the current user
     * Call this method when user logs in successfully
     */
    fun initializeTokenForUser(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get current FCM token
                val tokenResult = notificationRepository.getFCMToken()
                
                tokenResult.fold(
                    onSuccess = { token ->
                        // Update user's FCM token in Firestore
                        val updateResult = notificationRepository.updateUserFCMToken(userId, token)
                        updateResult.fold(
                            onSuccess = {
                                Timber.d("FCM token initialized successfully for user: $userId")
                            },
                            onFailure = { e ->
                                Timber.e(e, "Failed to update FCM token for user: $userId")
                            }
                        )
                    },
                    onFailure = { e ->
                        Timber.e(e, "Failed to get FCM token for user: $userId")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error initializing FCM token for user: $userId")
            }
        }
    }
    
    /**
     * Refresh FCM token for current user
     * Call this when user updates their profile or when needed
     */
    fun refreshTokenForUser(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delete current token and get a new one
                fcm.deleteToken()
                
                // Get new token
                val tokenResult = notificationRepository.getFCMToken()
                
                tokenResult.fold(
                    onSuccess = { token ->
                        // Update user's FCM token in Firestore
                        val updateResult = notificationRepository.updateUserFCMToken(userId, token)
                        updateResult.fold(
                            onSuccess = {
                                Timber.d("FCM token refreshed successfully for user: $userId")
                            },
                            onFailure = { e ->
                                Timber.e(e, "Failed to update refreshed FCM token for user: $userId")
                            }
                        )
                    },
                    onFailure = { e ->
                        Timber.e(e, "Failed to get refreshed FCM token for user: $userId")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing FCM token for user: $userId")
            }
        }
    }
    
    /**
     * Remove FCM tokens for user (call on logout)
     */
    fun clearTokensForUser(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delete current token
                fcm.deleteToken()
                Timber.d("FCM token cleared for user: $userId")
            } catch (e: Exception) {
                Timber.e(e, "Error clearing FCM token for user: $userId")
            }
        }
    }
}
