# Google Sign-In Implementation for SevaLK

## Overview
This implementation adds Google Sign-In functionality to the SevaLK app with the following flow:

1. User taps Google Sign-In button on Login or Registration screen
2. User authenticates with Google
3. App navigates to UserTypeSelectionScreen to ask: Customer or Service Provider?
4. Based on selection:
   - **Service Provider**: Creates both User and ServiceProvider documents → Navigate to ServiceSelectionScreen → SetLocationScreen
   - **Customer**: Creates User document → Navigate to Home screen

## Key Components Added

### 1. UserTypeSelectionScreen
- Screen that appears after Google authentication
- Allows user to choose between Customer and Service Provider
- Creates appropriate documents in Firebase

### 2. Google Sign-In Helper
- `GoogleSignInHelper.kt`: Manages Google Sign-In configuration
- Handles Google authentication flow
- Extracts user information (email, name)

### 3. Updated AuthRepository
- `signInWithGoogle()`: Authenticates with Firebase using Google ID token
- `createGoogleUser()`: Creates user document for existing authenticated users
- `createGoogleServiceProvider()`: Creates both user and service provider documents

### 4. Updated ViewModels
- `RegistrationViewModel`: Added Google Sign-In handling
- `LoginViewModel`: Added Google Sign-In with user existence check

### 5. Navigation Updates
- Added `UserTypeSelection` route with email/name parameters
- Updated navigation flows for Google Sign-In

## Configuration Required

### 1. Add Google Services Configuration
- `google-services.json` is already configured
- Web client ID is added to `strings.xml`

### 2. Dependencies Added
- Google Play Services Auth: `com.google.android.gms:play-services-auth:21.2.0`

## User Flow

### For New Users (First time with Google):
1. Tap Google Sign-In button
2. Complete Google authentication
3. Navigate to UserTypeSelectionScreen
4. Select user type (Customer/Service Provider)
5. Navigate to appropriate next screen based on selection

### For Existing Users:
1. Tap Google Sign-In button
2. Complete Google authentication
3. App checks if user exists in database
4. If exists: Navigate directly to Home
5. If not exists: Navigate to UserTypeSelectionScreen

## Implementation Details

### Google Sign-In Button Integration
Both Login and Registration screens now have functional Google Sign-In buttons that:
- Launch Google Sign-In intent
- Handle authentication results
- Navigate based on user status

### Firebase Integration
- Users are authenticated with Firebase using Google ID tokens
- User documents are created in the `users` collection
- Service providers get additional documents in `service_providers` collection

### Error Handling
- Comprehensive error handling for authentication failures
- User-friendly error messages
- Loading states during authentication

## Testing the Implementation

1. **Test Google Sign-In Flow**:
   - Tap Google button on Login/Registration screen
   - Complete Google authentication
   - Verify navigation to UserTypeSelectionScreen

2. **Test User Type Selection**:
   - Select "Customer" → Should navigate to Home
   - Select "Service Provider" → Should navigate to ServiceSelectionScreen

3. **Test Database Creation**:
   - Verify user documents are created in Firebase
   - For service providers, verify both collections are populated

This implementation provides a complete Google Sign-In integration that follows your specified requirements while maintaining good user experience and proper error handling.
