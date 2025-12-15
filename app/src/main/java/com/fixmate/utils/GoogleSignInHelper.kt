package com.fixmate.utils

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.fixmate.R
import timber.log.Timber
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(private val context: Context) {
    
    suspend fun getSignInIntent(): Intent {
        // Create GoogleSignInOptions without filtering by account
        val webClientId = context.getString(R.string.default_web_client_id)
        Timber.d("Google Sign-In: Using Web Client ID: $webClientId")
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        
        val client = GoogleSignIn.getClient(context, gso)
        
        try {
            // Sign out and revoke access to force account selection
            Timber.d("Google Sign-In: Clearing previous sign-in state")
            client.signOut().await()
            client.revokeAccess().await()
            Timber.d("Google Sign-In: Previous state cleared successfully")
        } catch (e: Exception) {
            Timber.w(e, "Failed to clear Google Sign-In cache")
        }
        
        return client.signInIntent
    }
    
    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            val account = task.getResult(ApiException::class.java)
            Timber.d("Google Sign-In successful: ${account?.email}")
            Timber.d("ID Token available: ${account?.idToken != null}")
            Timber.d("Display Name: ${account?.displayName}")
            account
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                10 -> "Developer Error: Check SHA-1 fingerprint in Firebase Console"
                12500 -> "Sign In Failed: Google Play Services is updating"
                12501 -> "Sign In Cancelled by user"
                12502 -> "Sign In Failed: Network error"
                else -> "Error code: ${e.statusCode}"
            }
            Timber.e(e, "Google Sign-In failed: $errorMessage")
            null
        }
    }
    
    suspend fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        val client = GoogleSignIn.getClient(context, gso)
        try {
            client.signOut().await()
            client.revokeAccess().await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to sign out from Google")
        }
    }
}
