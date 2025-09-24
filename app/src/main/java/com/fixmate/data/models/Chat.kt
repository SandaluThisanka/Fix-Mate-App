package com.fixmate.data.models

data class ChatConversation(
    val id: String = "",
    val bookingId: String = "",
    val participants: List<String> = emptyList(), // User IDs
    val lastMessage: ChatMessage? = null,
    val unreadCount: Map<String, Int> = emptyMap(), // User ID to unread count
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val attachments: List<MessageAttachment> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val readBy: Map<String, Long> = emptyMap(), // User ID to read timestamp
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val replyTo: String? = null, // Message ID being replied to
    val status: MessageStatus = MessageStatus.SENT
)

data class MessageAttachment(
    val id: String = "",
    val type: AttachmentType = AttachmentType.IMAGE,
    val url: String = "",
    val fileName: String = "",
    val fileSize: Long = 0L,
    val thumbnail: String? = null
)

data class QuickReply(
    val id: String = "",
    val text: String = "",
    val category: String = "" // e.g., "greeting", "pricing", "scheduling"
)

enum class MessageType {
    TEXT, IMAGE, FILE, LOCATION, BOOKING_UPDATE, SYSTEM_MESSAGE
}

enum class AttachmentType {
    IMAGE, DOCUMENT, VIDEO, AUDIO
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}
