package com.fixmate.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fixmate.ui.theme.S_YELLOW
import com.fixmate.ui.theme.SevaLKTheme
import com.fixmate.R
import com.fixmate.presentation.chat.viewmodel.InboxViewModel
import com.fixmate.presentation.components.CustomerAvatar
import com.fixmate.presentation.customer.profile.ImagePickerDialog
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: String,
    val text: String,
    val imageUrl: String? = null,
    val messageType: String = "text",
    val time: String,
    val isFromMe: Boolean,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    chatId: String,
    participantId: String,
    contactName: String = "Contact",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Initialize the chat
    LaunchedEffect(chatId) {
        viewModel.initializeChat(chatId, participantId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 28.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                ){
                    CustomerAvatar(
                        customerId = participantId,
                        size = 40.dp
                    )
                }
                
                Column {
                    Text(
                        text = contactName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        fontSize = 14.sp,
                        color = if (isOnline) Color.Green else Color.Gray
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { /* Handle call */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.Black
                    )
                }
                
                IconButton(
                    onClick = { /* Handle more options */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.Black
                    )
                }
            }
        }
        
        // Messages
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = S_YELLOW)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { messageItem ->
                    val message = Message(
                        id = messageItem.id,
                        text = messageItem.text,
                        imageUrl = messageItem.imageUrl,
                        messageType = messageItem.messageType,
                        time = formatMessageTime(messageItem.timestamp),
                        isFromMe = messageItem.isFromMe,
                        isRead = messageItem.isRead
                    )
                    MessageBubble(message = message)
                }
            }
        }
        
        // Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.05f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { 
                    Text(
                        "Message",
                        color = Color.Gray,
                        fontSize = 16.sp
                    ) 
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                trailingIcon = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
//                        IconButton(
//                            onClick = { /* Handle attachment */ },
//                            modifier = Modifier.size(24.dp)
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.attachment),
//                                contentDescription = "Attach",
//                                tint = Color.Gray,
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
                        
                        IconButton(
                            onClick = { showImagePicker = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.camera),
                                contentDescription = "Camera",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
            
            IconButton(
                onClick = { 
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(S_YELLOW, CircleShape)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Show image picker dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { imageUri ->
                viewModel.sendImageMessage(imageUri)
                showImagePicker = false
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (message.isFromMe) S_YELLOW else Color.Gray.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                        )
                    )
                    .padding(if (message.messageType == "image") 4.dp else 12.dp)
                    .widthIn(max = 280.dp)
            ) {
                when (message.messageType) {
                    "image" -> {
                        // Display image message
                        if (!message.imageUrl.isNullOrEmpty()) {
                            Column {
                                AsyncImage(
                                    model = message.imageUrl,
                                    contentDescription = "Shared image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (message.text.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = message.text,
                                        fontSize = 14.sp,
                                        color = if (message.isFromMe) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Display text message
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = if (message.isFromMe) Color.White else Color.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.time,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

@Preview
@Composable
fun InboxScreenPreview() {
    SevaLKTheme {
        InboxScreen(
            chatId = "preview_chat",
            participantId = "preview_participant",
            contactName = "Mike's Plumbing"
        )
    }
}

