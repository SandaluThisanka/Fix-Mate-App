package com.fixmate.presentation.auth.login

sealed class LoginEvent {
    data class GoogleSignIn(val onNavigateToUserTypeSelection: (String, String) -> Unit, val onLoginSuccess: () -> Unit) : LoginEvent()
}
