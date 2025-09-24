package com.fixmate.utils

import com.fixmate.data.repositories.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatUtils @Inject constructor(
    private val chatRepository: ChatRepository
) {
    
    /**
     * Creates or gets existing chat ID for communication between two users
     */
    suspend fun createOrGetChatId(participantId: String): String {
        return chatRepository.createOrGetChatId(participantId)
    }
    
    /**
     * Initiates a chat with a service provider
     */
    suspend fun startChatWithProvider(
        providerId: String,
        initialMessage: String? = null
    ): Result<String> {
        return try {
            val chatId = createOrGetChatId(providerId)
            
            // Send initial message if provided
            initialMessage?.let { message ->
                chatRepository.sendMessage(chatId, providerId, message)
            }
            
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Creates a navigation route for starting a chat with a provider
     */
    suspend fun createChatNavigationRoute(
        providerId: String,
        providerName: String
    ): String {
        val chatId = createOrGetChatId(providerId)
        return "inbox/$chatId/$providerId/$providerName"
    }
    
    /**
     * Formats chat ID for display purposes
     */
    fun formatChatDisplayName(chatId: String): String {
        return chatId.removePrefix("chat_")
            .split("_")
            .joinToString(" vs ")
    }
}
