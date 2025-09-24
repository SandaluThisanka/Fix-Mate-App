package com.fixmate.presentation.provider.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import android.location.Geocoder
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.fixmate.data.models.Booking
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Date
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.fixmate.presentation.components.common.PrimaryButtonStyle

data class Job(
    val title: String,
    val customer: String,
    val time: String,
    val status: String,
    val icon: ImageVector? = null,
    val iconText: String? = null
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

fun formatCurrency(amount: Double): String {
    return if (amount >= 1000) {
        "LKR ${NumberFormat.getNumberInstance().format(amount.toLong())}"
    } else {
        "LKR ${String.format("%.0f", amount)}"
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProviderHomeScreen(
    navController: NavController,
    onSwitchToCustomer: (() -> Unit)? = null,
    viewModel: ProviderHomeViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(1) } // 1 = My Business selected by default
    val uiState by viewModel.uiState.collectAsState()
    
    // Pull to refresh setup
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.refreshData() }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
        item {
            // Loading State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFC107))
                }
                return@item
            }

            // Error State
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading data",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.refreshData() }) {
                            Text("Retry", color = Color(0xFFD32F2F))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
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
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                Row {
                    IconButton(onClick = { /* Handle notifications */ }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = { /* Handle profile */ }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Tab-like Buttons Row
            Row(modifier = Modifier.fillMaxWidth()) {
                com.fixmate.presentation.components.common.PrimaryButton(
                    text = "Find Services",
                    onClick = { 
                       onSwitchToCustomer?.invoke()
                    },
                    modifier = Modifier.weight(1f),
                    style = PrimaryButtonStyle.OUTLINE,
                    backgroundColor = Color.Gray.copy(alpha = 0.4f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                com.fixmate.presentation.components.common.PrimaryButton(
                    text = "My Business",
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    style = if (selectedTab == 1) com.fixmate.presentation.components.common.PrimaryButtonStyle.TEXT else com.fixmate.presentation.components.common.PrimaryButtonStyle.OUTLINE,
                    backgroundColor = if (selectedTab == 1) Color(0xFFFFC107) else Color(0xFFF5F5F5),
                    foregroundColor = if (selectedTab == 1) Color.White else Color(0xFFFFC107)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Today's Overview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Today's Overview",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${uiState.todayBookings}", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 24.sp
                            )
                            Text("Today's Bookings", color = Color.Gray, fontSize = 12.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                formatCurrency(uiState.thisWeekIncome), 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 18.sp
                            )
                            Text("This Week", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            // Upcoming Jobs Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Upcoming Jobs",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                TextButton(onClick = { /* View all */ }) {
                    Text("View all", color = Color(0xFFFFC107), fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Show upcoming bookings or empty state
        if (uiState.upcomingBookings.isEmpty() && !uiState.isLoading && uiState.error == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "📅",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No upcoming bookings",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            "New booking requests will appear here when customers book your services",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        com.fixmate.presentation.components.common.PrimaryButton(
                            text = "Refresh",
                            onClick = { viewModel.refreshData() },
                            style = com.fixmate.presentation.components.common.PrimaryButtonStyle.OUTLINE,
                            modifier = Modifier.fillMaxWidth(0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            // Job Cards from bookings
            items(uiState.upcomingBookings) { booking ->
                BookingJobCard(
                    booking = booking,
                    onBookingClick = { bookingId ->
                        // Navigate to booking details or show bottom sheet
                        // For now, you can add navigation to booking details
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Your Performance
            Text(
                "Your Performance",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Performance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (uiState.providerRating > 0) 
                                    String.format("%.1f", uiState.providerRating) 
                                else "0.0",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.Black
                            )
                            Text(
                                if (uiState.providerRating > 0) "⭐⭐⭐⭐⭐" else "Getting started",
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            "${uiState.totalCompleteJobs} complete jobs",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Progress Bar
                    val progressValue = if (uiState.totalCompleteJobs > 0) {
                        (uiState.totalCompleteJobs.toFloat() / (uiState.totalCompleteJobs + 10).toFloat())
                    } else {
                        0f
                    }
                    LinearProgressIndicator(
                        progress = progressValue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFFC107),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    } // End LazyColumn
        
        // Pull to refresh indicator
        PullRefreshIndicator(
            refreshing = uiState.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color(0xFFFFC107),
            contentColor = Color.White
        )
    } // End Box
}

@Composable
fun BookingJobCard(
    booking: Booking, 
    onBookingClick: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onBookingClick != null) {
                    Modifier.clickable { onBookingClick(booking.id) }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Icon based on service name
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon = when {
                    booking.serviceName.lowercase().contains("plumb") -> "🔧"
                    booking.serviceName.lowercase().contains("tutor") || 
                    booking.serviceName.lowercase().contains("teach") -> "📚"
                    booking.serviceName.lowercase().contains("clean") -> "🧹"
                    booking.serviceName.lowercase().contains("electric") -> "⚡"
                    booking.serviceName.lowercase().contains("paint") -> "🎨"
                    booking.serviceName.lowercase().contains("garden") -> "🌱"
                    booking.serviceName.lowercase().contains("repair") -> "🔨"
                    else -> "🛠️"
                }
                Text(
                    icon,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Job Details
            Column(modifier = Modifier.weight(1f)) {
                // Show service name with count if multiple services
                val serviceDisplayText = booking.serviceName
                
                Text(
                    serviceDisplayText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    "Customer: ${booking.customerName}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                val scheduledDateTime = if (booking.scheduledDate > 0) {
                    try {
                        val date = Date(booking.scheduledDate)
                        dateFormat.format(date)
                    } catch (e: Exception) {
                        booking.scheduledTime.ifEmpty { "Time TBD" }
                    }
                } else {
                    booking.scheduledTime.ifEmpty { "Time TBD" }
                }
                Text(
                    scheduledDateTime,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            // Status Badge
            val (statusColor, statusBgColor) = when (booking.status) {
                com.fixmate.data.models.BookingStatus.PENDING -> Pair(Color(0xFFB8860B), Color(0xFFFFF3CD))
                com.fixmate.data.models.BookingStatus.ACCEPTED -> Pair(Color(0xFF1976D2), Color(0xFFE3F2FD))
                com.fixmate.data.models.BookingStatus.CONFIRMED -> Pair(Color(0xFF388E3C), Color(0xFFE8F5E8))
                com.fixmate.data.models.BookingStatus.IN_PROGRESS -> Pair(Color(0xFFF57C00), Color(0xFFFFF3E0))
                else -> Pair(Color(0xFF616161), Color(0xFFF5F5F5))
            }
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = statusBgColor
            ) {
                Text(
                    booking.status.name.lowercase().replaceFirstChar { it.uppercase() }
                        .replace("_", " "),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun JobCard(job: Job) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Job Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (job.icon != null) {
                    Icon(
                        job.icon,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                } else if (job.iconText != null) {
                    Text(
                        job.iconText,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Fallback icon
                    Text(
                        "🔧",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Job Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    job.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    job.customer,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    job.time,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            // Status Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF3CD)
            ) {
                Text(
                    job.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color(0xFFB8860B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}