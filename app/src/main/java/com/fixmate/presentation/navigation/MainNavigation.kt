package com.fixmate.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fixmate.data.models.Booking
import com.fixmate.presentation.auth.AuthState
import com.fixmate.presentation.auth.AuthViewModel
import com.fixmate.presentation.chat.ChatScreen
import com.fixmate.presentation.customer.booking.MyBookingsScreen
import com.fixmate.presentation.customer.home.HomeScreen
import com.fixmate.presentation.customer.search.ServiceProviderMapScreen
import com.fixmate.presentation.customer.profile.CustomerProfileScreen
import com.fixmate.presentation.provider.home.ProviderHomeScreen
import com.fixmate.presentation.provider.jobs.CreateServiceBillScreen
import com.fixmate.presentation.provider.jobs.JobsScreen
import com.fixmate.presentation.provider.profile.ProviderProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    initialTab: String? = null,
) {
    val authState by remember { derivedStateOf { authViewModel.authState } }
    val isProviderMode by remember { derivedStateOf { authViewModel.isProviderMode } }
    var handledInitialTab by remember { mutableStateOf(false) }
    var customerSelectedTab by remember { mutableStateOf(CustomerNavigationTab.HOME) }
    var providerSelectedTab by remember { mutableStateOf(ProviderNavigationTab.DASHBOARD) }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.UNAUTHENTICATED -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
            AuthState.FIRST_TIME_USER -> {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
            else -> {
                // Stay on current screen
            }
        }
    }

    LaunchedEffect(initialTab, authState, isProviderMode) {
        // Only process if we have an initial tab, haven't handled it yet, and authenticated
        if (!handledInitialTab && initialTab != null && authState == AuthState.AUTHENTICATED) {
            when (initialTab.uppercase()) {
                "MESSAGE" -> {
                    if (isProviderMode) {
                        providerSelectedTab = ProviderNavigationTab.MESSAGES
                    } else {
                        customerSelectedTab = CustomerNavigationTab.MESSAGES
                    }
                    handledInitialTab = true
                }
                // Add other cases here if needed
            }
        }
    }

    var showCreateBillScreen by remember { mutableStateOf(false) }
    var selectedBookingForBill by remember { mutableStateOf<Booking?>(null) }
    var selectedServiceType by remember { mutableStateOf<com.fixmate.presentation.components.map.ServiceType?>(null) }
    
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // Function to switch to provider mode
    val switchToProviderMode = {
        authViewModel.updateProviderMode(true)
        // If switching from customer profile, go to provider profile (BUSINESS tab)
        providerSelectedTab = if (customerSelectedTab == CustomerNavigationTab.PROFILE) {
            ProviderNavigationTab.BUSINESS
        } else {
            ProviderNavigationTab.DASHBOARD
        }
    }
    
    // Function to switch to customer mode
    val switchToCustomerMode = {
        authViewModel.updateProviderMode(false)
        customerSelectedTab = if (providerSelectedTab == ProviderNavigationTab.BUSINESS) {
            CustomerNavigationTab.PROFILE
        } else {
            CustomerNavigationTab.HOME
        }
    }

    // Show CreateServiceBillScreen if needed
    if (showCreateBillScreen && selectedBookingForBill != null) {
        CreateServiceBillScreen(
            booking = selectedBookingForBill!!,
            onBackClick = {
                showCreateBillScreen = false
                selectedBookingForBill = null
            },
            onConfirmBill = {
                // Handle bill confirmation
                showCreateBillScreen = false
                selectedBookingForBill = null
            }
        )
        return
    }

    Scaffold(
        bottomBar = {
            if (isProviderMode) {
                ProviderBottomNavigationBar(
                    selectedTab = providerSelectedTab,
                    onTabSelected = { tab ->
                        providerSelectedTab = tab
                        when (tab) {
                            ProviderNavigationTab.DASHBOARD -> {
                                // Stay on current screen, just update tab
                            }
                            ProviderNavigationTab.JOBS -> {
                                // Navigate to provider jobs screen
                            }
                            ProviderNavigationTab.SCHEDULE -> {
                                // Navigate to provider schedule screen
                            }
                            ProviderNavigationTab.MESSAGES -> {
                                // Navigate to provider messages screen
                            }
                            ProviderNavigationTab.BUSINESS -> {
                                // Navigate to provider business/profile screen
                            }
                        }
                    }
                )
            } else {
                CustomerBottomNavigationBar(
                    selectedTab = customerSelectedTab,
                    onTabSelected = { tab ->
                        if (tab == CustomerNavigationTab.SEARCH) {
                            selectedServiceType = null // Reset filter if user taps tab
                        }
                        customerSelectedTab = tab
                        when (tab) {
                            CustomerNavigationTab.HOME -> {
                                // Stay on current screen, just update tab
                            }
                            CustomerNavigationTab.SEARCH -> {
                                // Navigate to search screen
                            }
                            CustomerNavigationTab.BOOKINGS -> {
                                // Navigate to bookings screen
                            }
                            CustomerNavigationTab.MESSAGES -> {
                                // Navigate to messages screen
                            }
                            CustomerNavigationTab.PROFILE -> {
                                // Navigate to profile screen
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isProviderMode) {
                when (providerSelectedTab) {
                    ProviderNavigationTab.DASHBOARD -> {
                        ProviderHomeScreen(navController = navController, onSwitchToCustomer = switchToCustomerMode)
                    }
                    ProviderNavigationTab.JOBS -> {
                        JobsScreen(
                            onNavigateToCreateBill = { booking ->
                                selectedBookingForBill = booking
                                showCreateBillScreen = true
                            }
                        )
                    }
                    ProviderNavigationTab.SCHEDULE -> {
                        // Schedule screen placeholder
                        ServiceProviderMapScreen(navController = navController)
                    }
                    ProviderNavigationTab.MESSAGES -> {
                        ChatScreen(
                            onChatItemClick = { chatItem ->
                                navController.navigate(
                                    Screen.Inbox.createRoute(
                                        chatItem.chatId,
                                        chatItem.participantId,
                                        chatItem.name
                                    )
                                )
                            }
                        )
                    }
                    ProviderNavigationTab.BUSINESS -> {
                        ProviderProfileScreen(
                            navController = navController,
                            onLogoutClick = {
                                authViewModel.signOut()
                            },
                            onSwitchToCustomerClick = {
                                authViewModel.updateProviderMode(false)
                            }
                        )
                    }
                }
            } else {
                when (customerSelectedTab) {
                    CustomerNavigationTab.HOME -> {
                        HomeScreen(
                            navController = navController,
                            onSwitchToProvider = switchToProviderMode,
                            onServiceSelected = { serviceType ->
                                selectedServiceType = serviceType
                                customerSelectedTab = CustomerNavigationTab.SEARCH
                            }
                        )
                    }
                    CustomerNavigationTab.SEARCH -> {
                        ServiceProviderMapScreen(
                            navController = navController,
                            initialServiceType = selectedServiceType,
                            onNavigateToMessages = { customerSelectedTab = CustomerNavigationTab.MESSAGES }
                        )
                    }
                    CustomerNavigationTab.BOOKINGS -> {
                        MyBookingsScreen(navController = navController)
                    }
                    CustomerNavigationTab.MESSAGES -> {
                        ChatScreen(
                            onChatItemClick = { chatItem ->
                                navController.navigate(
                                    Screen.Inbox.createRoute(
                                        chatItem.chatId,
                                        chatItem.participantId,
                                        chatItem.name
                                    )
                                )
                            }
                        )
                    }
                    CustomerNavigationTab.PROFILE -> {
                        CustomerProfileScreen(
                            navController = navController,
                            onSwitchToProviderClick = switchToProviderMode,
                            onLogoutClick = {
                                authViewModel.signOut()
                            },
                            onFavoritesClick = {},
                            onPaymentMethodsClick = {  },
                            onPrivacySecurityClick = {  },
                            onHelpSupportClick = {  }
                        )
                    }
                }
            }
        }
    }
}