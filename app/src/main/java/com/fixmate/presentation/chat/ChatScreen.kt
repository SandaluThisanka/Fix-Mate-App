package com.fixmate.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fixmate.ui.theme.S_GREEN
import com.fixmate.ui.theme.S_YELLOW
import com.fixmate.ui.theme.SevaLKTheme
import com.fixmate.R
import com.fixmate.presentation.chat.viewmodel.ChatViewModel
import com.fixmate.presentation.components.CustomerAvatar
import java.text.SimpleDateFormat
import java.util.*

data class ChatItem(
    val chatId: String,
    val participantId: String,
    val name: String,
    val message: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onChatItemClick: (ChatItem) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messages",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            IconButton(
                onClick = { /* Handle search */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            }
        }
        
        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = S_YELLOW)
            }
        } else if (conversations.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No conversations yet",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start booking services to chat with providers",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Messages List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations) { conversation ->
                    val lastMessageText = when {
                        conversation.lastMessage == null -> "No messages yet"
                        conversation.lastMessage.messageType == "image" -> "📷 Photo"
                        else -> conversation.lastMessage.text
                    }

                    val chatItem = ChatItem(
                        chatId = conversation.chatId,
                        participantId = conversation.participantId,
                        name = conversation.participantName,
                        message = lastMessageText,
                        time = formatTime(conversation.lastMessage?.timestamp ?: conversation.updatedAt),
                        unreadCount = conversation.unreadCount,
                        isOnline = conversation.isOnline
                    )
                    
                    ChatItemRow(
                        chatItem = chatItem,
                        onClick = { onChatItemClick(chatItem) }
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun ChatItemRow(
    chatItem: ChatItem,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with CustomerAvatar component
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            CustomerAvatar(
                customerId = chatItem.participantId,
                size = 50.dp
            )
            
            // Online indicator
            if (chatItem.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .background(S_GREEN, CircleShape)
                        .padding(2.dp)
                )
            }
        }
        
        // Message content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = chatItem.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatItem.time,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    if (chatItem.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(S_YELLOW, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chatItem.unreadCount.toString(),
                                fontSize = 12.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = chatItem.message,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    SevaLKTheme {
        ChatScreen()
    }
}

