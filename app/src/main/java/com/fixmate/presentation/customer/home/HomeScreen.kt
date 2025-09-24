package com.fixmate.presentation.customer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.presentation.auth.components.CustomTextField
import com.fixmate.presentation.components.common.PrimaryButton
import com.fixmate.presentation.components.common.PrimaryButtonStyle
import com.fixmate.ui.theme.S_YELLOW
import com.fixmate.ui.theme.S_LIGHT_TEXT
import com.fixmate.ui.theme.S_INPUT_BACKGROUND
import androidx.navigation.NavController
import com.fixmate.presentation.navigation.Screen
import com.fixmate.ui.theme.SevaLKTheme
import com.fixmate.presentation.customer.booking.MyBookingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.fixmate.presentation.customer.booking.BookingCard
import com.fixmate.presentation.components.map.ServiceType
import com.fixmate.presentation.components.map.ServiceProvider
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.fixmate.utils.Constants
import com.fixmate.presentation.components.map.calculateDistance
import android.location.Geocoder
import java.util.Locale

data class ServiceItem(
    val name: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val serviceType: com.fixmate.presentation.components.map.ServiceType
)

fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..11 -> "Good Morning!"
        in 12..16 -> "Good Afternoon!"
        in 17..19 -> "Good Evening!"
        else -> "Good Night!"
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    onSwitchToProvider: (() -> Unit)? = null,
    onServiceSelected: ((com.fixmate.presentation.components.map.ServiceType) -> Unit)? = null,
    modifier: Modifier = Modifier,
    myBookingsViewModel: MyBookingsViewModel = hiltViewModel(),
    serviceProviders: List<ServiceProvider> = emptyList(),
    serviceProviderCheckViewModel: ServiceProviderCheckViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAllBookings by remember { mutableStateOf(false) }

    // Service provider check state
    val serviceProviderCheckState by serviceProviderCheckViewModel.uiState.collectAsState()

    // Handle service provider account creation completion
    LaunchedEffect(serviceProviderCheckState.serviceProviderCreated) {
        if (serviceProviderCheckState.serviceProviderCreated) {
            // Navigate to service selection screen
            navController.navigate(Screen.ServiceSelection.route)
            serviceProviderCheckViewModel.resetState()
        }
    }

    // Sample data with only confirmed available Material Icons
    val popularServices = listOf(
        ServiceItem("Plumbing", Icons.Default.Build, Color(0xFF6366F1), com.fixmate.presentation.components.map.ServiceType.PLUMBING),
        ServiceItem("Electrical", Icons.Default.Settings, Color(0xFFF59E0B), com.fixmate.presentation.components.map.ServiceType.ELECTRICAL),
        ServiceItem("Cleaning", Icons.Default.Home, Color(0xFF10B981), com.fixmate.presentation.components.map.ServiceType.CLEANING),
        ServiceItem("Auto Repair", Icons.Default.Build, Color(0xFFEF4444), com.fixmate.presentation.components.map.ServiceType.ALL),
        ServiceItem("Tutoring", Icons.Default.Person, Color(0xFF8B5CF6), com.fixmate.presentation.components.map.ServiceType.ALL),
        ServiceItem("Beauty", Icons.Default.Face, Color(0xFFEC4899), com.fixmate.presentation.components.map.ServiceType.ALL)
    )

    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentAddress by remember { mutableStateOf<String?>(null) }

    // Request location on first composition
    LaunchedEffect(Unit) {
        // You may want to check permissions here!
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
            }
        }
    }

    // Reverse geocode when currentLocation changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]

                    // Get city and country
                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                    val country = address.countryName

                    // Format as "City, Country"
                    currentAddress = when {
                        city != null && country != null -> "$city, $country"
                        country != null -> country
                        city != null -> city
                        else -> null
                    }
                } else {
                    currentAddress = null
                }
            } catch (e: Exception) {
                currentAddress = null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    getGreeting(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍 ", fontSize = 14.sp)
                    Text(
                        currentAddress ?: "",
                        color = S_LIGHT_TEXT,
                        fontSize = 14.sp
                    )
                }
            }

            IconButton(onClick = { /* TODO: Notifications */ }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Gray
                )
            }

            IconButton(onClick = { /* TODO: Profile */ }) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Find Services & My Business buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            PrimaryButton(
                text = "Find Services",
                onClick = { /* Current screen - no action needed */ },
                modifier = Modifier.weight(1f),
                style = PrimaryButtonStyle.TEXT
            )
            Spacer(modifier = Modifier.width(8.dp))
            PrimaryButton(
                text = "My Business",
                onClick = {
                    // Check if user has service provider account
                    serviceProviderCheckViewModel.checkServiceProviderAccount()
                },
                modifier = Modifier.weight(1f),
                style = PrimaryButtonStyle.OUTLINE,
                backgroundColor = Color.Gray.copy(alpha = 0.4f),
                foregroundColor = S_YELLOW
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        CustomTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search services...",
            backgroundColor = S_INPUT_BACKGROUND,
            focusedBackgroundColor = S_INPUT_BACKGROUND,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Popular Services
        Text(
            "Popular Services",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(popularServices) { service ->
                ServiceCard(service = service, onClick = { onServiceSelected?.invoke(service.serviceType) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Your Bookings
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Your Bookings",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (showAllBookings) "Show Less" else "View All",
                color = S_YELLOW,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { showAllBookings = !showAllBookings }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val bookings by myBookingsViewModel.bookings.collectAsState()
        val isLoading by myBookingsViewModel.isLoading.collectAsState()
        val error by myBookingsViewModel.error.collectAsState()

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFFFC107))
        } else if (bookings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No bookings yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Start exploring services and make your first booking!",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            val bookingsToShow = if (showAllBookings) bookings else bookings.take(2)
            bookingsToShow.forEach { booking ->
                BookingCard(
                    booking = booking,
                    onBookingClick = { bookingId ->
                        navController.navigate("booking_details/$bookingId")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nearby Providers
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nearby Providers",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                "See All",
                color = S_YELLOW,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Only show currently available providers
        val filteredProviders = remember(serviceProviders, currentLocation) {
            serviceProviders.filter { provider ->
                currentLocation?.let { current ->
                    val distance = calculateDistance(
                        current.latitude,
                        current.longitude,
                        provider.latitude,
                        provider.longitude
                    )
                    distance <= Constants.NEARBY_PROVIDER_RADIUS_KM * 1000 // e.g., 5000 for 5km
                } ?: true // If location not available, show all
            }
        }

        if (filteredProviders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No providers nearby",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Try searching different services or check your location settings.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            filteredProviders.forEach { provider ->
                val distance = currentLocation?.let { current ->
                    calculateDistance(
                        current.latitude,
                        current.longitude,
                        provider.latitude,
                        provider.longitude
                    ) / 1000f // Convert to km
                }
                ProviderCard(
                    provider = provider,
                    distanceKm = distance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Show join as provider dialog
    if (serviceProviderCheckState.showJoinDialog) {
        JoinAsProviderDialog(
            isLoading = serviceProviderCheckState.isLoading,
            onJoinClick = {
                serviceProviderCheckViewModel.createServiceProviderAccount()
            },
            onDismiss = {
                serviceProviderCheckViewModel.dismissJoinDialog()
            }
        )
    }

    // Handle existing service provider account - switch to provider mode
    // Only switch when explicitly requested via the shouldNavigateToProvider flag
    LaunchedEffect(serviceProviderCheckState.shouldNavigateToProvider) {
        if (serviceProviderCheckState.shouldNavigateToProvider) {
            onSwitchToProvider?.invoke()
            serviceProviderCheckViewModel.clearNavigationFlag()
        }
    }

    // Handle errors
    serviceProviderCheckState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar or toast here
            // For now, we'll just log the error
            serviceProviderCheckViewModel.clearError()
        }
    }
}

@Composable
fun ServiceCard(
    service: ServiceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 100.dp, height = 70.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(service.backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    service.icon,
                    contentDescription = service.name,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                service.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ProviderCard(
    provider: ServiceProvider,
    distanceKm: Float?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { /* TODO: Handle click */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    provider.name.first().toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    provider.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    provider.type.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        " ${provider.rating} (${provider.completedJobs})" +
                        (distanceKm?.let { " • %.1f km".format(it) } ?: ""),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF10B981).copy(alpha = 0.1f)
            ) {
                Text(
                    "Available", // You can add logic for availability if you have it
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    val sampleProviders = listOf(
        ServiceProvider(
            id = "1",
            name = "Mike's Plumbing",
            type = ServiceType.PLUMBING,
            latitude = 6.0367, // Example: Weligama, Sri Lanka
            longitude = 80.2170,
            rating = 4.8f,
            completedJobs = 25
        ),
        ServiceProvider(
            id = "2",
            name = "Sarah Electronics",
            type = ServiceType.ELECTRICAL,
            latitude = 0.0,
            longitude = 0.0,
            rating = 4.4f,
            completedJobs = 18
        )
    )
    SevaLKTheme {
        HomeScreen(navController = NavController(context = LocalContext.current), serviceProviders = sampleProviders)
    }
}