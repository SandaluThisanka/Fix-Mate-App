# SevaLK Authentication Flow Implementation

This document explains how the authentication flow is implemented in the SevaLK Kotlin Jetpack Compose app, similar to the Flutter implementation you provided.

## Overview

The authentication flow determines which screen to show based on:
1. **First time user**: Show onboarding
2. **Authenticated user**: Show home screen
3. **Unauthenticated user**: Show login screen

## Key Components

### 1. AuthStateManager
**Location**: `fixmate.presentation.auth.AuthStateManager`

This is the central component that manages authentication state, similar to your Flutter app's routing logic.

**Features**:
- Tracks authentication state using `AuthState` enum
- Monitors Firebase Auth state changes
- Manages first-time user detection using SharedPreferences
- Handles user type detection (customer vs provider)

```kotlin
enum class AuthState {
    LOADING,
    FIRST_TIME_USER,
    AUTHENTICATED,
    UNAUTHENTICATED
}
```

### 2. Navigation Flow

#### App Startup Sequence:
1. **MainActivity** launches with **SplashScreen**
2. **SplashScreen** calls `authStateManager.checkInitialAuthState()`
3. Based on `AuthState`, navigation occurs:
   - `FIRST_TIME_USER` → Onboarding
   - `AUTHENTICATED` → Home
   - `UNAUTHENTICATED` → Login

#### Navigation Routes:
```kotlin
// In SevaLKNavigation.kt
startDestination = Screen.Splash.route

composable(Screen.Splash.route) {
    SplashScreen(
        onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) },
        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
        onNavigateToHome = { navController.navigate(Screen.Home.route) }
    )
}
```

### 3. First-Time User Detection

Uses Android SharedPreferences (similar to Flutter's `IsFirstRun`):

```kotlin
private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

private fun checkIfFirstTimeUser() {
    val isFirstRun = sharedPreferences.getBoolean("is_first_run", true)
    if (isFirstRun) {
        authState = AuthState.FIRST_TIME_USER
    } else {
        authState = AuthState.UNAUTHENTICATED
    }
}

fun markOnboardingCompleted() {
    sharedPreferences.edit().putBoolean("is_first_run", false).apply()
}
```

### 4. Authentication State Monitoring

Firebase Auth state changes are monitored automatically:

```kotlin
init {
    firebaseAuth.addAuthStateListener { auth ->
        val user = auth.currentUser
        currentUser = user
        
        if (user != null) {
            checkUserTypeAndNavigate(user)
        } else {
            checkIfFirstTimeUser()
        }
    }
}
```

## Usage Examples

### 1. In Any Composable Screen

```kotlin
@Composable
fun MyScreen(authStateManager: AuthStateManager = hiltViewModel()) {
    val authState by remember { derivedStateOf { authStateManager.authState } }
    val currentUser by remember { derivedStateOf { authStateManager.currentUser } }
    
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.UNAUTHENTICATED -> {
                // Navigate to login
            }
            AuthState.AUTHENTICATED -> {
                // User is logged in, continue
            }
            // Handle other states...
        }
    }
}
```

### 2. In ViewModels

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val authStateManager: AuthStateManager
) : ViewModel() {
    
    val isAuthenticated get() = authStateManager.authState == AuthState.AUTHENTICATED
    val currentUser get() = authStateManager.currentUser
    
    fun signOut() {
        authStateManager.signOut()
    }
}
```

### 3. Logout Functionality

```kotlin
@Composable
fun ProfileScreen() {
    LogoutButton(
        onLogoutSuccess = {
            // Navigation handled automatically by AuthStateManager
        }
    )
}
```

## Comparison with Flutter Implementation

| Flutter | Kotlin Jetpack Compose |
|---------|------------------------|
| `IsFirstRun.isFirstCall()` | `SharedPreferences.getBoolean("is_first_run", true)` |
| `FirebaseAuth.instance.authStateChanges()` | `FirebaseAuth.addAuthStateListener()` |
| `globalNavigatorKey.currentState!.pushReplacementNamed()` | `navController.navigate() with popUpTo` |
| Manual routing in `_routeUserForAuth()` | Automatic routing via `AuthStateManager` |
| Static method calls | Dependency injection with Hilt |

## Key Benefits

1. **Centralized State Management**: All auth logic in one place
2. **Automatic Navigation**: State changes trigger navigation automatically
3. **Dependency Injection**: Easy testing and clean architecture
4. **Type Safety**: Kotlin's type system prevents navigation errors
5. **Reactive UI**: Compose recomposes automatically on state changes

## Files Modified/Created

### Created:
- `AuthStateManager.kt` - Main auth state management
- `LogoutComponents.kt` - Reusable logout UI components
- `AuthExampleScreen.kt` - Usage examples

### Modified:
- `SplashScreen.kt` - Integration with AuthStateManager
- `MainActivity.kt` - Start with splash screen
- `SevaLKNavigation.kt` - Added splash screen route
- `Screen.kt` - Added splash screen route
- `LoginViewModel.kt` - Integration with AuthStateManager
- `OnboardingScreen.kt` - Mark completion via AuthStateManager
- `MainNavigation.kt` - Handle auth state changes

This implementation provides the same functionality as your Flutter app but leverages Kotlin's type system and Jetpack Compose's reactive nature for a more robust solution.
