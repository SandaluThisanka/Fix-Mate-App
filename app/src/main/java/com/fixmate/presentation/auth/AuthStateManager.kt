package com.fixmate.presentation.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.utils.Constants
import com.fixmate.utils.FCMTokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class AuthState {
    LOADING,
    FIRST_TIME_USER,
    AUTHENTICATED,
    UNAUTHENTICATED
}

@Singleton
class AuthStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val fcmTokenManager: FCMTokenManager
) {
    private val authScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    var authState by mutableStateOf(AuthState.LOADING)
        private set

    var currentUser by mutableStateOf<FirebaseUser?>(null)
        private set

    var isProviderMode by mutableStateOf(false)
        private set

    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    init {
        // Listen for auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            currentUser = user
            
            if (user != null) {
                Timber.d("Auth state listener: User authenticated: ${user.uid}")
                // Only auto-navigate if the auth state is still loading (app startup)
                // This prevents interference with manual login flows
                if (authState == AuthState.LOADING) {
                    Timber.d("Auth state is LOADING, checking user type and navigating")
                    checkUserTypeAndNavigate(user)
                } else {
                    Timber.d("Auth state is ${authState}, skipping auto-navigation")
                }
            } else {
                Timber.d("Auth state listener: User not authenticated")
                // Only check first time user if not already authenticated
                if (authState != AuthState.AUTHENTICATED) {
                    Timber.d("Auth state is not AUTHENTICATED, checking if first time user")
                    checkIfFirstTimeUser()
                } else {
                    Timber.d("Auth state is AUTHENTICATED but user is null - this might be a logout")
                    // This could be a logout scenario
                    authState = AuthState.UNAUTHENTICATED
                }
            }
        }
    }

    fun checkInitialAuthState() {
        val user = firebaseAuth.currentUser
        currentUser = user
        
        if (user != null) {
            Timber.d("User already authenticated: ${user.uid}")
            checkUserTypeAndNavigate(user)
        } else {
            Timber.d("No authenticated user")
            checkIfFirstTimeUser()
        }
    }

    private fun checkIfFirstTimeUser() {
        val isFirstRun = sharedPreferences.getBoolean("is_first_run", true)
        
        if (isFirstRun) {
            Timber.d("First time user - showing onboarding")
            authState = AuthState.FIRST_TIME_USER
        } else {
            Timber.d("Returning user - showing login")
            authState = AuthState.UNAUTHENTICATED
        }
    }

    private fun checkUserTypeAndNavigate(user: FirebaseUser) {
        // Mark onboarding as completed when user is authenticated
        markOnboardingCompleted()
        
        // Initialize FCM token for the user
        fcmTokenManager.initializeTokenForUser(user.uid)
        
        // Check if user is a service provider
        firestore.collection(Constants.COLLECTION_USERS)
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userType = document.getString("userType")
                    isProviderMode = userType == Constants.USER_TYPE_PROVIDER
                    Timber.d("User type determined: $userType")
                } else {
                    // User document doesn't exist, might be a new user
                    isProviderMode = false
                    Timber.d("User document not found, defaulting to customer mode")
                }
                // Set auth state last to ensure all data is ready
                authState = AuthState.AUTHENTICATED
                Timber.d("Auth state updated to AUTHENTICATED")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Failed to get user type")
                // Default to customer mode on error
                isProviderMode = false
                // Still set as authenticated even if user type fetch fails
                authState = AuthState.AUTHENTICATED
                Timber.d("Auth state updated to AUTHENTICATED (with error in user type fetch)")
            }
    }

    suspend fun checkUserTypeAsync(user: FirebaseUser): Boolean {
        return try {
            // Mark onboarding as completed when user is authenticated
            markOnboardingCompleted()
            
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(user.uid)
                .get()
                .await()
            
            if (document.exists()) {
                val userType = document.getString("userType")
                isProviderMode = userType == Constants.USER_TYPE_PROVIDER
                Timber.d("User type determined: $userType")
                isProviderMode
            } else {
                isProviderMode = false
                Timber.d("User document not found, defaulting to customer mode")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user type")
            isProviderMode = false
            false
        }
    }

    fun markOnboardingCompleted() {
        sharedPreferences.edit().putBoolean("is_first_run", false).apply()
        Timber.d("Onboarding completed, marked in preferences")
    }
    
    fun markOnboardingCompletedImmediate() {
        sharedPreferences.edit().putBoolean("is_first_run", false).commit() // Use commit for immediate write
        Timber.d("Onboarding completed immediately, marked in preferences")
    }

    fun signOut() {
        val currentUser = firebaseAuth.currentUser
        
        // Set user offline before signing out
        setUserOfflineBeforeLogout()
        
        // Clear FCM tokens
        if (currentUser != null) {
            fcmTokenManager.clearTokensForUser(currentUser.uid)
        }
        
        firebaseAuth.signOut()
        isProviderMode = false
        authState = AuthState.UNAUTHENTICATED
        Timber.d("User signed out")
    }

    private fun setUserOfflineBeforeLogout() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            authScope.launch {
                try {
                    val statusData = mapOf(
                        "isOnline" to false,
                        "lastSeen" to System.currentTimeMillis()
                    )
                    
                    FirebaseDatabase.getInstance()
                        .getReference("user_status")
                        .child(currentUser.uid)
                        .setValue(statusData)
                        .await()
                        
                    Timber.d("User status set to offline before logout")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to set user offline before logout")
                }
            }
        }
    }

    fun updateProviderMode(isProvider: Boolean) {
        isProviderMode = isProvider
        Timber.d("Provider mode updated: $isProvider")
    }

    fun refreshAuthState() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            checkUserTypeAndNavigate(user)
        } else {
            checkIfFirstTimeUser()
        }
    }
    
    fun handleSuccessfulAuthentication() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            Timber.d("Handling successful authentication for user: ${user.uid}")
            // Mark onboarding as completed immediately for successful authentication
            markOnboardingCompletedImmediate()
            
            // Set a temporary authenticated state to prevent navigation issues
            authState = AuthState.AUTHENTICATED
            
            // Then check user type and update accordingly
            checkUserTypeAndNavigate(user)
        } else {
            Timber.w("handleSuccessfulAuthentication called but no user found")
            checkIfFirstTimeUser()
        }
    }
    
    fun handleNewUserSignUp() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            Timber.d("Handling new user sign up for user: ${user.uid}")
            // Mark onboarding as completed for new users
            markOnboardingCompleted()
            // Set auth state to authenticated immediately
            authState = AuthState.AUTHENTICATED
            isProviderMode = false // Default for new users
        }
    }
}
