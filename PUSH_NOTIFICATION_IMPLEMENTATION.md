# SevaLK FCM Push Notifications Implementation

## Overview

This implementation adds Firebase Cloud Messaging (FCM) support to the SevaLK app for sending push notifications when customers book service providers. The solution uses a custom Express.js backend instead of Firebase Cloud Functions to keep costs down.

## Features Implemented

### 1. Android App Changes

#### FCM Service (`FCMService.kt`)
- ✅ Handles incoming push notifications
- ✅ Updates FCM tokens when they change
- ✅ Displays notifications with proper intents for navigation
- ✅ Supports different notification types (booking, status updates, messages)

#### Notification Repository (`NotificationRepository.kt`)
- ✅ Manages FCM token operations
- ✅ Queues notifications in Firestore for backend processing
- ✅ Handles booking notifications
- ✅ Handles booking status update notifications
- ✅ Handles message notifications (structure ready)

#### FCM Token Manager (`FCMTokenManager.kt`)
- ✅ Initializes FCM tokens when users log in
- ✅ Refreshes tokens when needed
- ✅ Clears tokens on user logout

#### Updated Repositories
- ✅ **BookingRepository**: Sends notification when booking is created
- ✅ **BookingRepository**: Sends notification when booking status changes
- ✅ **AuthRepository**: Added FCM token update method
- ✅ **AuthStateManager**: Initializes FCM token on successful login

#### User Model Updates
- ✅ **User.kt**: Already had `deviceTokens` field for storing FCM tokens

#### Android Manifest Updates
- ✅ Added FCM service registration
- ✅ Added necessary permissions for notifications
- ✅ Added default notification icon and channel configuration

#### Dependency Injection
- ✅ Added Firebase Messaging to DI
- ✅ Added NotificationRepository to DI
- ✅ Updated BookingRepository DI to include NotificationRepository

### 2. Express.js Backend

#### Server Implementation (`backend/server.js`)
- ✅ Processes notification queue from Firestore
- ✅ Sends FCM messages using Firebase Admin SDK
- ✅ Handles different notification types
- ✅ Provides health check and queue status endpoints
- ✅ Manual notification sending for testing

#### Configuration
- ✅ `package.json` with required dependencies
- ✅ `.env.example` with configuration template
- ✅ Proper error handling and logging

### 3. Documentation
- ✅ Complete implementation guide (`FCM_IMPLEMENTATION_GUIDE.md`)
- ✅ Setup instructions for Express.js backend
- ✅ Firebase configuration guide
- ✅ Testing and troubleshooting information

## How It Works

### Notification Flow

1. **Customer Books Service**: Customer creates a booking in the app
2. **Booking Created**: `BookingRepository.createBooking()` is called
3. **Notification Queued**: Notification is added to `pending_notifications` collection in Firestore
4. **Backend Processing**: Express.js server polls the queue every 10 seconds
5. **FCM Sending**: Server sends push notification using Firebase Admin SDK
6. **Provider Receives**: Service provider gets push notification
7. **Status Updates**: When provider updates booking status, customer gets notified

### Data Structure

#### FCM Token in User Document
```json
{
  "id": "user123",
  "email": "user@example.com",
  "displayName": "John Doe",
  "fcmToken": "fcm_token_string"
}
```

#### Notification Queue Document
```json
{
  "token": "fcm_token_string",
  "title": "New Booking Request",
  "body": "John Doe has requested your plumbing service",
  "data": {
    "type": "booking_request",
    "bookingId": "booking123",
    "customerName": "John Doe",
    "serviceName": "Plumbing"
  },
  "createdAt": 1234567890,
  "processed": false
}
```

## Setup Instructions

### 1. Android App
The app is already configured! Just build and run. FCM tokens will be automatically generated and stored when users log in.

### 2. Express.js Backend

1. **Navigate to backend directory**:
   ```bash
   cd backend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Configure environment**:
   ```bash
   cp .env.example .env
   # Edit .env with your Firebase service account details
   ```

4. **Start server**:
   ```bash
   npm start
   # or for development:
   npm run dev
   ```

### 3. Firebase Configuration

1. **Get Service Account Key**:
   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Download the JSON file

2. **Extract Environment Variables**:
   - `FIREBASE_PROJECT_ID`: From "project_id" field
   - `FIREBASE_CLIENT_EMAIL`: From "client_email" field  
   - `FIREBASE_PRIVATE_KEY`: From "private_key" field (keep the \\n characters)

3. **Update .env file** with these values

## Testing

### Test FCM Token Generation
1. Log in to the app
2. Check Firestore users collection
3. Verify `fcmToken` field is populated

### Test Booking Notifications
1. Create a booking from customer account
2. Check `pending_notifications` collection in Firestore
3. Watch Express.js server logs for processing
4. Verify provider receives push notification

### Test Manual Notification
```bash
curl -X POST http://localhost:3000/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "token": "fcm_token_here",
    "title": "Test Notification",
    "body": "This is a test message"
  }'
```

## Current Status

### ✅ Completed
- FCM token management when user logs in
- Notification sending when booking is created
- Notification sending when booking status changes
- Express.js backend for processing notifications
- Complete documentation and setup guides

### 🔄 Optional Enhancements
- Chat message notifications (structure ready, needs integration)
- Token cleanup for invalid/expired tokens
- Advanced notification scheduling
- Push notification analytics

### 📋 Next Steps
1. Set up the Express.js backend server
2. Configure Firebase service account credentials
3. Test the notification flow end-to-end
4. Deploy the backend to your preferred hosting platform

## Dependencies Added
- Android: `firebase-messaging-ktx:23.4.0` (already in build.gradle)
- Backend: `express`, `firebase-admin`, `cors`, `dotenv`

The implementation is complete and ready for testing! 🚀
