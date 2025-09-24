package com.fixmate.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.repositories.ChatConversationItem
import com.fixmate.data.repositories.ChatRepository
import com.fixmate.data.repositories.UserOnlineStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _conversations = MutableStateFlow<List<ChatConversationItem>>(emptyList())
    val conversations: StateFlow<List<ChatConversationItem>> = _conversations.asStateFlow()

    private val onlineStatusCache = mutableMapOf<String, StateFlow<UserOnlineStatus>>()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            chatRepository.getChatConversations()
                .onStart { _isLoading.value = true }
                .catch { e ->
                    Timber.e(e, "Failed to load chat conversations")
                    _isLoading.value = false
                }
                .collect { conversations ->
                    _isLoading.value = false
                    
                    // Update conversations with online status
                    val updatedConversations = conversations.map { conversation ->
                        val onlineStatus = getOrCreateOnlineStatusFlow(conversation.participantId)
                        val currentStatus = onlineStatus.value
                        conversation.copy(isOnline = currentStatus.isOnline)
                    }
                    
                    _conversations.value = updatedConversations
                    
                    // Start observing online status for new participants
                    conversations.forEach { conversation ->
                        observeParticipantOnlineStatus(conversation.participantId)
                    }
                }
        }
    }

    private fun getOrCreateOnlineStatusFlow(userId: String): StateFlow<UserOnlineStatus> {
        return onlineStatusCache.getOrPut(userId) {
            chatRepository.getUserOnlineStatus(userId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = UserOnlineStatus(userId = userId, isOnline = false)
                )
        }
    }

    private fun observeParticipantOnlineStatus(participantId: String) {
        viewModelScope.launch {
            getOrCreateOnlineStatusFlow(participantId).collect { status ->
                // Update the conversation with the new online status
                _conversations.value = _conversations.value.map { conversation ->
                    if (conversation.participantId == participantId) {
                        conversation.copy(isOnline = status.isOnline)
                    } else {
                        conversation
                    }
                }
            }
        }
    }

    fun setUserOnline() {
        viewModelScope.launch {
            chatRepository.setUserOnlineStatus(true)
                .onFailure { e ->
                    Timber.e(e, "Failed to set user online")
                }
        }
    }

    fun setUserOffline() {
        viewModelScope.launch {
            chatRepository.setUserOnlineStatus(false)
                .onFailure { e ->
                    Timber.e(e, "Failed to set user offline")
                }
        }
    }

    fun markAllMessagesAsRead(chatId: String) {
        viewModelScope.launch {
            chatRepository.markAllMessagesAsRead(chatId)
                .onFailure { e ->
                    Timber.e(e, "Failed to mark messages as read")
                }
        }
    }

    fun refreshConversations() {
        loadConversations()
    }
}
