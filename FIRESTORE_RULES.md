# Firestore Security Rules for FixMate

## ⚠️ IMPORTANT: Apply These Rules Immediately

Copy the rules below and paste them into Firebase Console → Firestore Database → Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if user is the owner
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Users collection - සියලු authenticated users වලට read කරන්න පුළුවන්
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && request.auth.uid == userId;
      allow update: if isOwner(userId);
      allow delete: if isOwner(userId);
    }
    
    // Service Providers collection - සියලු authenticated users වලට read කරන්න පුළුවන්
    // (Map එකේ providers පෙන්වන්න ඕනේ නිසා)
    match /service_providers/{providerId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && request.auth.uid == providerId;
      allow update: if isOwner(providerId);
      allow delete: if isOwner(providerId);
    }
    
    // Bookings collection - Providers සහ Customers එකටම access කරන්න පුළුවන්
    match /bookings/{bookingId} {
      allow read: if isAuthenticated() && (
        request.auth.uid == resource.data.customerId ||
        request.auth.uid == resource.data.providerId
      );
      allow create: if isAuthenticated() && 
        request.auth.uid == request.resource.data.customerId;
      allow update: if isAuthenticated() && (
        request.auth.uid == resource.data.customerId ||
        request.auth.uid == resource.data.providerId
      );
      allow delete: if isAuthenticated() && (
        request.auth.uid == resource.data.customerId ||
        request.auth.uid == resource.data.providerId
      );
    }
    
    // Chats collection
    match /chats/{chatId} {
      allow read, write: if isAuthenticated();
      allow create: if isAuthenticated();
    }
    
    // Messages subcollection
    match /chats/{chatId}/messages/{messageId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
    }
    
    // Pending notifications collection
    match /pending_notifications/{notificationId} {
      allow read, write: if isAuthenticated();
    }
    
    // Payments collection
    match /payments/{paymentId} {
      allow read: if isAuthenticated() && (
        request.auth.uid == resource.data.customerId ||
        request.auth.uid == resource.data.providerId
      );
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && (
        request.auth.uid == resource.data.customerId ||
        request.auth.uid == resource.data.providerId
      );
    }
  }
}
```

## How to Apply These Rules:

1. Go to Firebase Console (https://console.firebase.google.com)
2. Select your FixMate project
3. Click on "Firestore Database" in the left menu
4. Click on the "Rules" tab
5. Replace the existing rules with the rules above
6. Click "Publish" to apply the changes

**Important:** After publishing the rules, wait 1-2 minutes for them to propagate globally.
