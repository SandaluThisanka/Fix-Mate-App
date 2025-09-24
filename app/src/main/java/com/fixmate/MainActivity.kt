package com.fixmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fixmate.data.repositories.PaymentRepository
import com.fixmate.presentation.navigation.Screen
import com.fixmate.presentation.navigation.SevaLKNavigation
import com.fixmate.ui.theme.SevaLKTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var paymentRepository: PaymentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add this logging
        System.out.println("SevaLK: Activity created, about to test repository")
        System.out.println("SevaLK: Repository class: ${paymentRepository.javaClass.simpleName}")
        System.out.println("SevaLK: Repository interfaces: ${paymentRepository.javaClass.interfaces.joinToString()}")

        // Test the repository method that's failing
        try {
            System.out.println("SevaLK: About to call repository method")
            // Your payment method call here
        } catch (e: Exception) {
            System.out.println("SevaLK: Repository method failed: ${e.message}")
            e.printStackTrace()
        }

        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            SevaLKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    SevaLKNavigation(
                        startDestination = Screen.Splash.route
                    )
                }
            }
        }
    }
}
