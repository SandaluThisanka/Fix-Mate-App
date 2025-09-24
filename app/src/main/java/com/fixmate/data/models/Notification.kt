package com.fixmate.data.models

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val data: Map<String, String> = emptyMap(), // Additional data for navigation
    val imageUrl: String = "",
    val isRead: Boolean = false,
    val isPush: Boolean = true, // Whether to send as push notification
    val isInApp: Boolean = true, // Whether to show in app notification list
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val actionButtons: List<NotificationAction> = emptyList()
)

data class NotificationAction(
    val id: String = "",
    val text: String = "",
    val action: String = "", // Deep link or action identifier
    val style: ActionStyle = ActionStyle.DEFAULT
)

data class NotificationSettings(
    val userId: String = "",
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val smsEnabled: Boolean = false,
    val categories: Map<NotificationType, Boolean> = mapOf(
        NotificationType.BOOKING_UPDATE to true,
        NotificationType.MESSAGE to true,
        NotificationType.PAYMENT to true,
        NotificationType.PROMOTION to false,
        NotificationType.SYSTEM to true,
        NotificationType.REVIEW to true,
        NotificationType.GENERAL to true
    ),
    val quietHours: QuietHours = QuietHours(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class QuietHours(
    val enabled: Boolean = false,
    val startTime: String = "22:00", // 24-hour format
    val endTime: String = "08:00"
)

enum class NotificationType {
    BOOKING_UPDATE,    // New booking, status changes
    MESSAGE,           // New chat messages
    PAYMENT,           // Payment confirmations, failures
    PROMOTION,         // Offers, discounts
    SYSTEM,            // App updates, maintenance
    REVIEW,            // New reviews, review reminders
    GENERAL            // General announcements
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}

enum class ActionStyle {
    DEFAULT, DESTRUCTIVE, CANCEL
}
