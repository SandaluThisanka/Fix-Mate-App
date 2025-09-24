package com.fixmate

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber


@HiltAndroidApp
class FixMateApplication : Application(), DefaultLifecycleObserver {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private var instance: FixMateApplication? = null
        
        var isNetworkAvailable by mutableStateOf(true)
            private set

        var currentUserId by mutableStateOf<String?>(null)
            private set

        var isProviderMode by mutableStateOf(false)
            private set

        fun updateNetworkStatus(isAvailable: Boolean) {
            val previousStatus = isNetworkAvailable
            isNetworkAvailable = isAvailable
            
            // Handle network status change for online user status
            if (previousStatus != isAvailable && FirebaseAuth.getInstance().currentUser != null) {
                instance?.handleNetworkStatusChange(isAvailable)
            }
        }

        fun updateCurrentUser(userId: String?) {
            currentUserId = userId
        }

        fun updateProviderMode(isProvider: Boolean) {
            isProviderMode = isProvider
        }
    }


    override fun onCreate() {
        super<Application>.onCreate()
        
        // Set the singleton instance
        instance = this

        // Initialize Firebase FIRST - before any dependency injection that might use it
        initializeFirebase()

        // Initialize logging
        initializeLogging()

        // Setup Firebase listeners
        setupFirebaseListeners()

        // Setup lifecycle observer for online status
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        Timber.d("FixMate Application initialized successfully")
    }

    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        // App comes to foreground - set user online
        setUserOnlineStatus(true)
        setupOnDisconnectHandler()
    }

    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        // App goes to background - set user offline
        setUserOnlineStatus(false)
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In production, you might want to plant a different tree
            // that logs to Crashlytics or another service
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    // Log to crash reporting service in production
                    // FirebaseCrashlytics.getInstance().log(message)
                }
            })
        }
    }

    private fun initializeFirebase() {
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firebase Realtime Database with offline persistence
        // THIS MUST BE DONE BEFORE ANY OTHER USAGE OF FirebaseDatabase
        try {
            val realtimeDatabase = FirebaseDatabase.getInstance()
            realtimeDatabase.setPersistenceEnabled(true)
            Timber.d("Firebase Realtime Database persistence enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to enable Firebase Realtime Database persistence")
        }

        // Configure Firestore settings for offline persistence
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings
    }

    private fun setupFirebaseListeners() {
        // Auth state is now handled by AuthStateManager
        // This is just for logging purposes and online status management
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            updateCurrentUser(user?.uid)

            if (user != null) {
                Timber.d("User authenticated: ${user.uid}")
                // Set user online when they authenticate and setup disconnect handler
                val isAppInForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                    Lifecycle.State.STARTED)
                if (isAppInForeground) {
                    setUserOnlineStatus(true)
                    setupOnDisconnectHandler()
                }
            } else {
                Timber.d("User signed out")
                // Clean up user-specific data
                cleanupUserServices()
            }
        }
    }

    private fun cleanupUserServices() {
        // Reset global state
        updateCurrentUser(null)
        updateProviderMode(false)

        // Clear any cached data
        // cacheManager.clearUserCache()
    }

    override fun onTerminate() {
        super<Application>.onTerminate()
        Timber.d("FixMate Application terminated")
    }

    override fun onLowMemory() {
        super<Application>.onLowMemory()
        Timber.w("Low memory warning - clearing caches")
        // Clear non-essential caches
        // cacheManager.clearNonEssentialCache()
    }

    private fun setUserOnlineStatus(isOnline: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            applicationScope.launch {
                try {
                    // Only set online if network is available and user wants to be online
                    val actualStatus = isOnline && isNetworkAvailable
                    
                    val statusData = mapOf(
                        "isOnline" to actualStatus,
                        "lastSeen" to System.currentTimeMillis()
                    )
                    
                    FirebaseDatabase.getInstance()
                        .getReference("user_status")
                        .child(currentUser.uid)
                        .setValue(statusData)
                        
                    Timber.d("User online status updated: $actualStatus (requested: $isOnline, network: $isNetworkAvailable)")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to update user online status")
                }
            }
        }
    }

    fun handleNetworkStatusChange(isNetworkAvailable: Boolean) {
        val isAppInForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
            Lifecycle.State.STARTED)
        
        if (isNetworkAvailable && isAppInForeground) {
            // Network restored and app is in foreground - set online
            setUserOnlineStatus(true)
            setupOnDisconnectHandler()
        } else if (!isNetworkAvailable) {
            // Network lost - set offline immediately
            setUserOnlineStatus(false)
        }
        
        Timber.d("Network status changed: $isNetworkAvailable, app in foreground: $isAppInForeground")
    }

    private fun setupOnDisconnectHandler() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val statusRef = FirebaseDatabase.getInstance()
                    .getReference("user_status")
                    .child(currentUser.uid)
                
                // Set up automatic offline status when client disconnects
                val offlineData = mapOf(
                    "isOnline" to false,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
                
                statusRef.onDisconnect().setValue(offlineData)
                Timber.d("OnDisconnect handler set up for user: ${currentUser.uid}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to setup onDisconnect handler")
            }
        }
    }
}