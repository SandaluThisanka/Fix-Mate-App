# Firebase Cloud Messaging (FCM) Implementation Guide for SevaLK

This guide explains how to set up push notifications for the SevaLK app using Firebase Cloud Messaging (FCM) with a custom Express.js backend.

## Overview

The implementation includes:
1. **Android App**: Handles FCM token generation and notification reception
2. **Express.js Backend**: Processes notification requests and sends FCM messages
3. **Firebase Firestore**: Stores notification queue and user tokens

## Architecture

```
Customer Books Service → App Creates Booking → Notification Queued in Firestore → Express.js Backend Processes Queue → FCM Sends Push Notification → Provider Receives Notification
```

## Android App Implementation

### 1. FCM Service (`FCMService.kt`)
- Handles incoming push notifications
- Updates FCM tokens when they change
- Displays notifications with proper navigation

### 2. Notification Repository (`NotificationRepository.kt`)
- Manages FCM token operations
- Queues notifications in Firestore for backend processing
- Handles different notification types (booking, status updates, messages)

### 3. FCM Token Manager (`FCMTokenManager.kt`)
- Initializes tokens when users log in
- Refreshes tokens when needed
- Clears tokens on logout

### 4. Integration Points
- **BookingRepository**: Sends notifications when bookings are created/updated
- **AuthStateManager**: Initializes FCM tokens on login
- **ChatRepository**: Can send message notifications (optional)

## Express.js Backend Setup

### Required Dependencies
```bash
npm install express firebase-admin dotenv cors
```

### Environment Variables (.env)
```
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=your-service-account-email
FIREBASE_PRIVATE_KEY=your-service-account-private-key
PORT=3000
```

### Sample Express.js Server Structure

```javascript
// server.js
const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert({
    projectId: process.env.FIREBASE_PROJECT_ID,
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
    privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
  }),
});

const db = admin.firestore();
const messaging = admin.messaging();

// Process notification queue
async function processNotificationQueue() {
  try {
    const snapshot = await db.collection('pending_notifications')
      .where('processed', '==', false)
      .limit(10)
      .get();
    
    const batch = db.batch();
    
    for (const doc of snapshot.docs) {
      const notificationData = doc.data();
      
      try {
        // Send FCM message
        const message = {
          token: notificationData.token, // Single token
          notification: {
            title: notificationData.title,
            body: notificationData.body,
          },
          data: notificationData.data || {},
          android: {
            notification: {
              icon: 'ic_notification',
              color: '#F3CA19',
              channelId: 'sevalk_notifications',
            },
          },
        };
        
        const response = await messaging.send(message);
        console.log(`Sent notification with message ID: ${response}`);
        
        // Mark as processed
        batch.update(doc.ref, { processed: true, processedAt: Date.now() });
        
      } catch (error) {
        console.error('Error sending notification:', error);
        // Mark as failed
        batch.update(doc.ref, { 
          processed: true, 
          failed: true, 
          error: error.message,
          processedAt: Date.now() 
        });
      }
    }
    
    await batch.commit();
    
  } catch (error) {
    console.error('Error processing notification queue:', error);
  }
}

// Process queue every 10 seconds
setInterval(processNotificationQueue, 10000);

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: Date.now() });
});

// Manual notification endpoint (for testing)
app.post('/send-notification', async (req, res) => {
  try {
    const { tokens, title, body, data } = req.body;
    
    const message = {
      tokens,
      notification: { title, body },
      data: data || {},
      android: {
        notification: {
          icon: 'ic_notification',
          color: '#F3CA19',
          channelId: 'sevalk_notifications',
        },
      },
    };
    
    const response = await messaging.sendMulticast(message);
    res.json({ 
      success: true, 
      successCount: response.successCount,
      failureCount: response.failureCount 
    });
    
  } catch (error) {
    console.error('Error sending notification:', error);
    res.status(500).json({ error: error.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`FCM notification server running on port ${PORT}`);
});
```

### Deployment Options

#### 1. Local Development
```bash
node server.js
```

#### 2. Cloud Deployment (Heroku, Railway, etc.)
- Deploy the Express.js server to any cloud platform
- Set environment variables in your deployment platform
- The server will automatically process the notification queue

#### 3. VPS/Server Deployment
```bash
# Using PM2 for process management
npm install -g pm2
pm2 start server.js --name "sevalk-notifications"
pm2 startup
pm2 save
```

## Firebase Setup

### 1. Enable Firebase Cloud Messaging
1. Go to Firebase Console → Project Settings
2. Navigate to Cloud Messaging tab
3. Note your Server Key (legacy) or use Service Account

### 2. Generate Service Account Key
1. Go to Project Settings → Service Accounts
2. Click "Generate new private key"
3. Download the JSON file
4. Extract the required fields for environment variables

### 3. Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read/write bookings they're involved in
    match /bookings/{bookingId} {
      allow read, write: if request.auth != null && 
        (resource.data.customerId == request.auth.uid || 
         resource.data.providerId == request.auth.uid);
    }
    
    // Notification queue - only system can write, but allow read for debugging
    match /pending_notifications/{notificationId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

## Testing

### 1. Test FCM Token Generation
- Log in to the app
- Check Firestore users collection for deviceTokens array
- Verify tokens are being added

### 2. Test Notification Queue
- Create a booking in the app
- Check pending_notifications collection in Firestore
- Verify notification document is created

### 3. Test Backend Processing
- Start the Express.js server
- Watch server logs for queue processing
- Verify notifications are marked as processed

### 4. Test Push Notification Reception
- Ensure app is in background
- Create a booking from another account
- Verify push notification appears

## Troubleshooting

### Common Issues

1. **Tokens not updating**: Check if user is authenticated when FCMTokenManager.initializeTokenForUser() is called
2. **Notifications not sending**: Verify Firebase service account credentials
3. **App not receiving notifications**: Check if FCMService is registered in AndroidManifest.xml
4. **Backend not processing**: Check Firestore connection and permissions

### Debug Steps

1. Check Android logs for FCM token generation
2. Verify Firestore documents are being created
3. Check Express.js server logs for processing status
4. Test FCM sending with Firebase Console

## Security Considerations

1. **Service Account Security**: Store private keys securely in environment variables
2. **Firestore Rules**: Ensure proper read/write permissions
3. **Token Management**: Regularly clean up old/invalid tokens
4. **Rate Limiting**: Implement rate limiting on notification endpoints

## Performance Optimization

1. **Batch Processing**: Process multiple notifications in batches
2. **Token Cleanup**: Remove invalid tokens from user documents
3. **Queue Management**: Set TTL on old notification documents
4. **Monitoring**: Add logging and monitoring for notification delivery rates

This implementation provides a robust, scalable solution for push notifications in the SevaLK app without requiring Firebase Cloud Functions on the paid tier.
