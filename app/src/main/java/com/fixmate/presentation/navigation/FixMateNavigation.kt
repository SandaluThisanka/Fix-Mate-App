package com.fixmate.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fixmate.presentation.auth.google.UserTypeSelectionScreen
import com.fixmate.presentation.auth.login.LoginScreen
import com.fixmate.presentation.auth.registration.RegistrationScreen
import com.fixmate.presentation.auth.welcome.WelcomeScreen
import com.fixmate.presentation.chat.InboxScreen
import com.fixmate.presentation.customer.booking.BookingConfirmationScreen
import com.fixmate.presentation.customer.booking.BookingDetailsScreen
import com.fixmate.presentation.customer.booking.BookingScreen
import com.fixmate.presentation.customer.home.HomeScreen
import com.fixmate.presentation.customer.services.ServiceListScreen
import com.fixmate.presentation.onboarding.OnboardingScreen
import com.fixmate.presentation.provider.location.SetLocationScreen
import com.fixmate.presentation.provider.profile.ProviderProfileScreen
import com.fixmate.presentation.provider.service.ServiceSelectionScreen
import com.fixmate.presentation.customer.payment.StripePaymentScreen
import com.fixmate.presentation.customer.payment.PaymentSuccessScreen
import com.fixmate.presentation.splash.SplashScreen
import com.fixmate.presentation.customer.settings.FavoritesScreen
import com.fixmate.presentation.customer.settings.PaymentMethodsScreen
import com.fixmate.presentation.customer.settings.PrivacySecurityScreen
import com.fixmate.presentation.customer.settings.HelpSupportScreen
import com.fixmate.presentation.provider.services.MyServicesScreen

@Composable
fun SevaLKNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - Entry point
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Flow
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        // Authentication Flow
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegistration = {
                    navController.navigate(Screen.Registration.route)
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.Registration.route)
                },
                onLoginSuccess = {
                    // Navigate based on user type
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToUserTypeSelection = { email, name ->
                    navController.navigate(Screen.UserTypeSelection.createRoute(email, name))
                }
            )
        }
        
        composable(Screen.Registration.route) {
            RegistrationScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToServiceSelection = {
                    navController.navigate(Screen.ServiceSelection.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Registration.route) { inclusive = true }
                    }
                },
                onNavigateToUserTypeSelection = { email, name ->
                    navController.navigate(Screen.UserTypeSelection.createRoute(email, name))
                }
            )
        }
        
        composable(Screen.UserTypeSelection.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            UserTypeSelectionScreen(
                userEmail = email,
                userName = name,
                onNavigateToServiceSelection = {
                    navController.navigate(Screen.ServiceSelection.route) {
                        popUpTo(Screen.UserTypeSelection.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UserTypeSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ServiceSelection.route) {
            ServiceSelectionScreen(
                onNavigateToNext = {
                    navController.navigate(Screen.SetLocation.route)
                }
            )
        }
        
        composable(Screen.SetLocation.route) {
            SetLocationScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Registration.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Customer Flow
        composable(Screen.Home.route) {
            MainNavigation(
                navController = navController
            )
        }
        
        composable(Screen.ServiceList.route) {
            ServiceListScreen(

            )
        }
        
        composable(
            route = "booking/{providerId}",
            arguments = listOf(
                navArgument("providerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")
            BookingScreen(
                providerId = providerId,
                onNavigateBack = { 
                    navController.popBackStack() 
                },
                onNavigateToConfirmation = { bookingId, providerName, serviceName ->
                    navController.navigate(
                        Screen.BookingConfirmation.createRoute(bookingId, providerName, serviceName)
                    ) {
                        popUpTo("booking/$providerId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "booking_confirmation/{bookingId}/{providerName}/{serviceName}",
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType },
                navArgument("providerName") { type = NavType.StringType },
                navArgument("serviceName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val providerName = backStackEntry.arguments?.getString("providerName") ?: ""
            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
            BookingConfirmationScreen(
                navController = navController,
                bookingId = bookingId,
                providerName = providerName,
                serviceName = serviceName
            )
        }

        // Provider Flow
        composable(Screen.CustomerHome.route) {
            HomeScreen(
                navController = navController
            )
        }

        // Customer Settings Screens
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }

        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(navController = navController)
        }

        composable(Screen.PrivacySecurity.route) {
            PrivacySecurityScreen(navController = navController)
        }

        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(navController = navController)
        }

        // Provider Settings Screens
        composable(Screen.MyServices.route) {
            MyServicesScreen(navController = navController)
        }
        
        composable(Screen.ProviderProfile.route) {
            ProviderProfileScreen(
                navController = navController,
                onLogoutClick = {},
                onSwitchToCustomerClick = { navController.navigate(Screen.CustomerHome.route) }
            )
        }
        
        // Shared Screens
        composable(Screen.Chat.route) {
            MainNavigation(
                navController = navController,
                initialTab = "MESSAGE"
            )
        }

        composable(
            route = "inbox/{chatId}/{participantId}/{contactName}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("participantId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val participantId = backStackEntry.arguments?.getString("participantId") ?: ""
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            InboxScreen(
                chatId = chatId,
                participantId = participantId,
                contactName = contactName,
                onBackClick = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
                    }
                }
            )
        }

        composable("booking_details/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingDetailsScreen(
                navController = navController,
                bookingId = bookingId
            )
        }

        composable("stripe_payment/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            StripePaymentScreen(
                bookingId = bookingId,
                onBackClick = {
                    navController.popBackStack()
                },
                onPaymentSuccess = { amount ->
                    navController.navigate("payment_success/${amount}") {
                        popUpTo("stripe_payment") { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = "payment_success/{amount}",
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            PaymentSuccessScreen(
                amount = amount,
                onBackToHome = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
