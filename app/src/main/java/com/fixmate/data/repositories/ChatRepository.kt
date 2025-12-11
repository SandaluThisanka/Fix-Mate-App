package com.fixmate.data.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.data.models.UserType
import com.fixmate.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class ChatConversationItem(
    val chatId: String = "",
    val participantId: String = "",
    val participantName: String = "",
    val participantType: String = "", // "customer" or "service_provider"
    val lastMessage: ChatMessageItem? = null,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChatMessageItem(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val messageType: String = "text", // "text" or "image"
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false,
    val isRead: Boolean = false
)

data class UserOnlineStatus(
    val userId: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

interface ChatRepository {
    suspend fun getCurrentUserId(): String?
    suspend fun getCurrentUserName(): String?
    suspend fun getCurrentUserType(): String?
    
    fun getChatConversations(): Flow<List<ChatConversationItem>>
    fun getChatMessages(chatId: String): Flow<List<ChatMessageItem>>
    fun getUserOnlineStatus(userId: String): Flow<UserOnlineStatus>
    
    suspend fun sendMessage(chatId: String, recipientId: String, message: String): Result<Unit>
    suspend fun sendImageMessage(chatId: String, recipientId: String, imageUri: Uri): Result<Unit>
    suspend fun markMessageAsRead(chatId: String, messageId: String): Result<Unit>
    suspend fun markAllMessagesAsRead(chatId: String): Result<Unit>
    
    suspend fun setUserOnlineStatus(isOnline: Boolean): Result<Unit>
    suspend fun updateLastSeen(): Result<Unit>
    
    suspend fun createOrGetChatId(participantId: String): String
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val firestore: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : ChatRepository {

    private val chatsRef = database.getReference("chats")
    private val messagesRef = database.getReference("messages")
    private val userStatusRef = database.getReference("user_status")

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getCurrentUserName(): String? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            document.getString("displayName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current user name")
            null
        }
    }

    override suspend fun getCurrentUserType(): String? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            normalizeUserType(document.getString("userType"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current user type")
            null
        }
    }

    override fun getChatConversations(): Flow<List<ChatConversationItem>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableListOf<ChatConversationItem>()
                
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key ?: continue
                    val participants = chatSnapshot.child("participants")

                    val currentUserExists = participants.hasChild(currentUserId)
                    if (!currentUserExists) continue
                    // Check if current user is a participant
                    var participantId: String? = null
                    for (participantSnapshot in participants.children) {
                        val userId = participantSnapshot.key
                        if (userId != currentUserId) {
                            participantId = userId
                            break
                        }
                    }
                    
                    if (participantId != null) {
                        val lastMessageSnapshot = chatSnapshot.child("lastMessage")
                        val lastMessage = if (lastMessageSnapshot.exists()) {
                            ChatMessageItem(
                                id = lastMessageSnapshot.child("id").getValue(String::class.java) ?: "",
                                senderId = lastMessageSnapshot.child("senderId").getValue(String::class.java) ?: "",
                                senderName = lastMessageSnapshot.child("senderName").getValue(String::class.java) ?: "",
                                text = lastMessageSnapshot.child("text").getValue(String::class.java) ?: "",
                                imageUrl = lastMessageSnapshot.child("imageUrl").getValue(String::class.java),
                                messageType = lastMessageSnapshot.child("messageType").getValue(String::class.java) ?: "text",
                                timestamp = lastMessageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
                                isFromMe = lastMessageSnapshot.child("senderId").getValue(String::class.java) == currentUserId,
                                isRead = lastMessageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                            )
                        } else null
                        
                        val unreadCount = chatSnapshot.child("unreadCount").child(currentUserId).getValue(Int::class.java) ?: 0
                        val participantName = participants.child(participantId).child("name").getValue(String::class.java) ?: "Unknown"
                        val participantType = participants.child(participantId).child("type").getValue(String::class.java) ?: "customer"
                        val updatedAt = chatSnapshot.child("updatedAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                        
                        conversations.add(
                            ChatConversationItem(
                                chatId = chatId,
                                participantId = participantId,
                                participantName = participantName,
                                participantType = participantType,
                                lastMessage = lastMessage,
                                unreadCount = unreadCount,
                                updatedAt = updatedAt
                            )
                        )
                    }
                }
                
                // Sort by last update time
                conversations.sortByDescending { it.updatedAt }
                trySend(conversations)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Failed to listen for chat conversations")
                trySend(emptyList())
            }
        }

        chatsRef.addValueEventListener(listener)
        awaitClose { chatsRef.removeEventListener(listener) }
    }

    override fun getChatMessages(chatId: String): Flow<List<ChatMessageItem>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessageItem>()
                
                for (messageSnapshot in snapshot.children) {
                    val message = ChatMessageItem(
                        id = messageSnapshot.key ?: "",
                        senderId = messageSnapshot.child("senderId").getValue(String::class.java) ?: "",
                        senderName = messageSnapshot.child("senderName").getValue(String::class.java) ?: "",
                        text = messageSnapshot.child("text").getValue(String::class.java) ?: "",
                        imageUrl = messageSnapshot.child("imageUrl").getValue(String::class.java),
                        messageType = messageSnapshot.child("messageType").getValue(String::class.java) ?: "text",
                        timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
                        isFromMe = messageSnapshot.child("senderId").getValue(String::class.java) == currentUserId,
                        isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                    )
                    messages.add(message)
                }
                
                // Sort by timestamp
                messages.sortBy { it.timestamp }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Failed to listen for chat messages")
                trySend(emptyList())
            }
        }

        messagesRef.child(chatId).addValueEventListener(listener)
        awaitClose { messagesRef.child(chatId).removeEventListener(listener) }
    }

    override fun getUserOnlineStatus(userId: String): Flow<UserOnlineStatus> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = UserOnlineStatus(
                    userId = userId,
                    isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                    lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                )
                trySend(status)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Failed to listen for user online status")
                trySend(UserOnlineStatus(userId = userId, isOnline = false))
            }
        }

        userStatusRef.child(userId).addValueEventListener(listener)
        awaitClose { userStatusRef.child(userId).removeEventListener(listener) }
    }

    override suspend fun sendMessage(chatId: String, recipientId: String, message: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentUserName = getCurrentUserName() ?: "Unknown User"
            val currentUserType = getCurrentUserType() ?: "customer"
            
            // Get recipient info
            val recipientDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(recipientId)
                .get()
                .await()
            val recipientName = recipientDoc.getString("displayName") ?: "Unknown User"
            val recipientType = normalizeUserType(recipientDoc.getString("userType"))

            val messageId = messagesRef.child(chatId).push().key ?: return Result.failure(Exception("Failed to generate message ID"))
            val timestamp = System.currentTimeMillis()

            val messageData = mapOf(
                "id" to messageId,
                "senderId" to currentUserId,
                "senderName" to currentUserName,
                "text" to message,
                "timestamp" to timestamp,
                "isRead" to false
            )

            // Save message
            messagesRef.child(chatId).child(messageId).setValue(messageData).await()

            // Update chat metadata
            val chatData = mapOf(
                "participants" to mapOf(
                    currentUserId to mapOf(
                        "name" to currentUserName,
                        "type" to currentUserType
                    ),
                    recipientId to mapOf(
                        "name" to recipientName,
                        "type" to recipientType
                    )
                ),
                "lastMessage" to messageData,
                "updatedAt" to timestamp,
                "unreadCount" to mapOf(
                    recipientId to ServerValue.increment(1)
                )
            )

            chatsRef.child(chatId).updateChildren(chatData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message")
            Result.failure(e)
        }
    }

    override suspend fun sendImageMessage(chatId: String, recipientId: String, imageUri: Uri): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentUserName = getCurrentUserName() ?: "Unknown User"
            val currentUserType = getCurrentUserType() ?: "customer"
            
            // Upload image first
            val imageUploadResult = imageRepository.uploadChatImage(imageUri, currentUserId)
            if (imageUploadResult.isFailure) {
                return Result.failure(imageUploadResult.exceptionOrNull() ?: Exception("Failed to upload image"))
            }
            
            val imageUrl = imageUploadResult.getOrNull() ?: return Result.failure(Exception("Image URL is null"))
            
            // Get recipient info
            val recipientDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(recipientId)
                .get()
                .await()

            val recipientName = recipientDoc.getString("displayName") ?: "Unknown User"
            val recipientType = normalizeUserType(recipientDoc.getString("userType"))

            val messageId = messagesRef.child(chatId).push().key ?: return Result.failure(Exception("Failed to generate message ID"))
            val timestamp = System.currentTimeMillis()

            val messageData = mapOf(
                "id" to messageId,
                "senderId" to currentUserId,
                "senderName" to currentUserName,
                "text" to "", // Empty text for image messages
                "imageUrl" to imageUrl,
                "messageType" to "image",
                "timestamp" to timestamp,
                "isRead" to false
            )

            // Save message
            messagesRef.child(chatId).child(messageId).setValue(messageData).await()

            // Update chat metadata
            val chatData = mapOf(
                "participants" to mapOf(
                    currentUserId to mapOf(
                        "name" to currentUserName,
                        "type" to currentUserType
                    ),
                    recipientId to mapOf(
                        "name" to recipientName,
                        "type" to recipientType
                    )
                ),
                "lastMessage" to messageData,
                "updatedAt" to timestamp,
                "unreadCount" to mapOf(
                    recipientId to ServerValue.increment(1)
                )
            )

            chatsRef.child(chatId).updateChildren(chatData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send image message")
            Result.failure(e)
        }
    }

    override suspend fun markMessageAsRead(chatId: String, messageId: String): Result<Unit> {
        return try {
            messagesRef.child(chatId).child(messageId).child("isRead").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark message as read")
            Result.failure(e)
        }
    }

    override suspend fun markAllMessagesAsRead(chatId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Reset unread count for current user
            chatsRef.child(chatId).child("unreadCount").child(currentUserId).setValue(0).await()
            
            // Mark all messages as read (optional - for better UX)
            val messagesSnapshot = messagesRef.child(chatId).get().await()
            val updates = mutableMapOf<String, Any>()
            
            for (messageSnapshot in messagesSnapshot.children) {
                val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                if (senderId != currentUserId) {
                    updates["${messageSnapshot.key}/isRead"] = true
                }
            }
            
            if (updates.isNotEmpty()) {
                messagesRef.child(chatId).updateChildren(updates).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark all messages as read")
            Result.failure(e)
        }
    }

    override suspend fun setUserOnlineStatus(isOnline: Boolean): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val statusData = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()
            )
            
            userStatusRef.child(currentUserId).setValue(statusData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set user online status")
            Result.failure(e)
        }
    }

    override suspend fun updateLastSeen(): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            userStatusRef.child(currentUserId).child("lastSeen").setValue(System.currentTimeMillis()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update last seen")
            Result.failure(e)
        }
    }

    override suspend fun createOrGetChatId(participantId: String): String {
        val currentUserId = getCurrentUserId() ?: throw Exception("User not authenticated")
        
        // Create a consistent chat ID by sorting user IDs
        val sortedIds = listOf(currentUserId, participantId).sorted()
        return "chat_${sortedIds[0]}_${sortedIds[1]}"
    }

    private fun normalizeUserType(userType: String?): String {
        val normalized = userType?.lowercase(Locale.US)
        val providerToken = UserType.SERVICE_PROVIDER.name.lowercase(Locale.US)
        return if (normalized == Constants.USER_TYPE_PROVIDER || normalized == providerToken) {
            Constants.USER_TYPE_PROVIDER
        } else {
            Constants.USER_TYPE_CUSTOMER
        }
    }
}
