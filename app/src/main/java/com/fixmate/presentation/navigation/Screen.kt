package com.fixmate.presentation.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object UserTypeSelection : Screen("user_type_selection/{email}/{name}") {
        fun createRoute(email: String, name: String) = "user_type_selection/$email/$name"
    }
    object Welcome : Screen("welcome")
    
    // Main Screens
    object Home : Screen("home")
    object CustomerHome : Screen("CustomerHome")
    object ServiceList : Screen("service_list")
    
    // Settings Screens
    object Favorites : Screen("favorites")
    object PaymentMethods : Screen("payment_methods")
    object PrivacySecurity : Screen("privacy_security")
    object HelpSupport : Screen("help_support")
    
    // Provider Settings Screens
    object MyServices : Screen("my_services")
    object Booking : Screen("booking/{providerId}/{providerName}/{rating}/{serviceType}") {
        fun createRoute(providerId: String, providerName: String, rating: Float, serviceType: String) =
            "booking/$providerId/$providerName/$rating/$serviceType"
    }
    object Chat : Screen("chat")
    object Inbox : Screen("inbox/{chatId}/{participantId}/{contactName}") {
        fun createRoute(chatId: String, participantId: String, contactName: String) = 
            "inbox/$chatId/$participantId/$contactName"
    }
    object ProviderProfile : Screen("provider_profile")
    object ProviderHome : Screen("provider_home")
    object ServiceSelection : Screen("service_selection")
    object SetLocation : Screen("set_location")
    object CreateServiceBill : Screen("create_service_bill")
    object BookingDetails : Screen("booking_details/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_details/$bookingId"
    }
    object BookingConfirmation : Screen("booking_confirmation/{bookingId}/{providerName}/{serviceName}") {
        fun createRoute(bookingId: String, providerName: String, serviceName: String) =
            "booking_confirmation/$bookingId/$providerName/$serviceName"
    }
}
