package com.fixmate.presentation.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val authStateManager: AuthStateManager
) : ViewModel() {
    
    val authState get() = authStateManager.authState
    val currentUser get() = authStateManager.currentUser
    val isProviderMode get() = authStateManager.isProviderMode
    
    fun checkInitialAuthState() {
        authStateManager.checkInitialAuthState()
    }
    
    fun markOnboardingCompleted() {
        authStateManager.markOnboardingCompleted()
    }
    
    fun signOut() {
        authStateManager.signOut()
    }
    
    fun updateProviderMode(isProvider: Boolean) {
        authStateManager.updateProviderMode(isProvider)
    }
    
    fun refreshAuthState() {
        authStateManager.refreshAuthState()
    }
    
    fun handleSuccessfulAuthentication() {
        authStateManager.handleSuccessfulAuthentication()
    }
    
    fun handleNewUserSignUp() {
        authStateManager.handleNewUserSignUp()
    }
}
