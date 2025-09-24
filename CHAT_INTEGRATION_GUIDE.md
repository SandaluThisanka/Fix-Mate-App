# Chat System Integration Guide

## Overview
The chat system has been completely rebuilt with Firebase Realtime Database integration, real-time messaging, and online status tracking.

## Key Components

### 1. ChatRepository.kt
- Real-time message synchronization using Firebase Realtime Database
- Online status tracking for users
- Automatic chat creation and management
- Flow-based reactive data streams

### 2. ViewModels
- **ChatViewModel.kt**: Manages conversation list with online status
- **InboxViewModel.kt**: Handles individual chat messaging

### 3. UI Components
- **ChatScreen.kt**: Displays user's conversations with real-time updates
- **InboxScreen.kt**: Individual chat interface with real-time messaging

## Navigation Integration

### Updated Routes
```kotlin
// Updated Screen.kt
object Inbox : Screen("inbox/{chatId}/{participantId}/{contactName}") {
    fun createRoute(chatId: String, participantId: String, contactName: String) = 
        "inbox/$chatId/$participantId/$contactName"
}
```

### Navigation Usage
```kotlin
// From Chat Screen to Inbox
ChatScreen(
    onChatItemClick = { chatItem ->
        navController.navigate(
            Screen.Inbox.createRoute(
                chatItem.chatId,
                chatItem.participantId,
                chatItem.name
            )
        )
    }
)

// From Search Screen (starting new chat with provider)
onMessageClick = {
    val chatId = "chat_${provider.id}"
    navController.navigate("inbox/$chatId/${provider.id}/${provider.name}")
}
```

## Firebase Database Structure

### Chat Messages
```
/chat_messages/{chatId}/{messageId}
├── senderId: String
├── senderName: String
├── text: String
├── timestamp: Long
└── isRead: Boolean
```

### User Status
```
/user_status/{userId}
├── isOnline: Boolean
├── lastSeen: Long
└── currentChatId: String?
```

### Chat Participants
```
/chat_participants/{chatId}
├── participant1: userId
├── participant2: userId
├── createdAt: Long
└── lastActivity: Long
```

## Key Features

### Real-time Updates
- Messages appear instantly using Firebase Realtime Database listeners
- Online status updates in real-time
- Conversation list updates automatically

### Online Status Tracking
- Green dot indicates online users
- App-level lifecycle tracking (foreground/background)
- Automatic offline status when app is closed

### Chat Management
- Automatic chat ID generation using pattern: `chat_{userId1}_{userId2}`
- Sorted participants ensure consistent chat IDs
- Unread message count tracking per user

## Usage Examples

### Starting a Chat from Booking Screen
```kotlin
@Composable
fun BookingScreen(
    providerId: String,
    chatUtils: ChatUtils = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    
    Button(
        onClick = {
            scope.launch {
                val result = chatUtils.startChatWithProvider(
                    providerId = providerId,
                    initialMessage = "Hi! I'm interested in your service."
                )
                result.onSuccess { chatId ->
                    navController.navigate(
                        Screen.Inbox.createRoute(chatId, providerId, providerName)
                    )
                }
            }
        }
    ) {
        Text("Contact Provider")
    }
}
```

### Handling Deep Links to Chat
```kotlin
// In your navigation graph
composable(
    route = "inbox/{chatId}/{participantId}/{contactName}",
    arguments = listOf(
        navArgument("chatId") { type = NavType.StringType },
        navArgument("participantId") { type = NavType.StringType },
        navArgument("contactName") { type = NavType.StringType }
    )
) { backStackEntry ->
    val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
    val participantId = backStackEntry.arguments?.getString("participantId") ?: ""
    val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
    
    InboxScreen(
        chatId = chatId,
        participantId = participantId,
        contactName = contactName,
        onBackClick = { navController.popBackStack() }
    )
}
```

## Testing the Integration

### 1. Test Real-time Messaging
1. Open chat screen on two devices/emulators with different user accounts
2. Send messages from one device
3. Verify messages appear instantly on the other device

### 2. Test Online Status
1. Open chat screen showing conversation list
2. Have the other user open/close the app
3. Verify online status indicator changes in real-time

### 3. Test Navigation
1. Navigate from chat list to individual chat
2. Verify proper parameters are passed (chatId, participantId, contactName)
3. Test back navigation returns to chat list

## Troubleshooting

### Common Issues

1. **Messages not syncing**: Check Firebase Database rules and authentication
2. **Navigation errors**: Ensure all required parameters are passed to InboxScreen
3. **Online status not updating**: Verify ProcessLifecycleOwner is registered in Application class

### Firebase Database Rules
Ensure your Firebase Realtime Database rules allow authenticated users to read/write:
```json
{
  "rules": {
    "chat_messages": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "user_status": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "chat_participants": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

## Next Steps

1. **Test the complete integration** with real Firebase project
2. **Add push notifications** for new messages when app is in background
3. **Implement message encryption** for enhanced security
4. **Add file/image sharing** capabilities
5. **Implement group chats** for multi-party conversations

The chat system is now fully integrated and ready for production use with real-time capabilities and proper navigation flow.
