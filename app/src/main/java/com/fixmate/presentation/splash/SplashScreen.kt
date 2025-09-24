package com.fixmate.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import com.fixmate.R
import com.fixmate.presentation.auth.AuthState
import com.fixmate.presentation.auth.AuthViewModel

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val authState by remember { derivedStateOf { authViewModel.authState } }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,        // Background color of the status bar
            darkIcons = true          // false = light icons, true = dark icons
        )
        // Show splash screen for at least 2 seconds
        delay(1000)
        authViewModel.checkInitialAuthState()
    }

    // Handle navigation based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.FIRST_TIME_USER, 
            AuthState.AUTHENTICATED,
            AuthState.UNAUTHENTICATED -> {
                delay(500) // Small delay for smooth transition
                onNavigateToWelcome() // Always navigate to Welcome screen first
            }
            AuthState.LOADING -> {
                // Stay on splash screen
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash),
                contentDescription = "FixMate Logo",
                modifier = Modifier.size(420.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FixMate",
                fontSize = 62.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Your trusted service partner",
                fontSize = 18.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}
