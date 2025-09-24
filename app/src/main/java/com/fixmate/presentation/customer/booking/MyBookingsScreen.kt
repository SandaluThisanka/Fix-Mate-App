package com.fixmate.presentation.customer.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavController,
    viewModel: MyBookingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val filters = listOf("All", "Pending", "Accepted", "In Progress", "Completed")

    // Filter bookings based on selected filter
    val filteredBookings = remember(bookings, selectedFilter) {
        when (selectedFilter) {
            "All" -> bookings
            "Pending" -> bookings.filter { it.status == BookingStatus.PENDING }
            "Accepted" -> bookings.filter { 
                it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.CONFIRMED 
            }
            "In Progress" -> bookings.filter {
                it.status == BookingStatus.IN_PROGRESS
            }
            "Completed" -> bookings.filter { 
                it.status == BookingStatus.COMPLETED 
            }
            else -> bookings
        }
    }

    // Show error snackbar
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // You can show a snackbar here if needed
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Bookings",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            IconButton(onClick = { viewModel.refreshBookings() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isLoading) Color.Gray else Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        // Filter Tabs
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    text = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { viewModel.setFilter(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC107))
            }
        }
        
        // Show error message
        error?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bookings List
        if (filteredBookings.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No bookings found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = if (selectedFilter == "All") "You haven't made any bookings yet" 
                               else "No ${selectedFilter.lowercase()} bookings",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBookings) { booking ->
                    BookingCard(
                        booking = booking,
                        onBookingClick = { bookingId ->
                            navController.navigate("booking_details/$bookingId")
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFFFC107) else Color(0xFFF5F5F5)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.Black else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

private fun getBookingStatusInfo(status: BookingStatus): BookingStatusInfo {
    return when (status) {
        BookingStatus.PENDING -> BookingStatusInfo("Pending", Color(0xFFF59E0B), Color(0xFFFEF3C7))
        BookingStatus.ACCEPTED -> BookingStatusInfo("Accepted", Color(0xFF10B981), Color(0xFFD1FAE5))
        BookingStatus.CONFIRMED -> BookingStatusInfo("Accepted", Color(0xFF10B981), Color(0xFFD1FAE5))
        BookingStatus.IN_PROGRESS -> BookingStatusInfo("In Progress", Color(0xFF3B82F6), Color(0xFFDBEAFE))
        BookingStatus.COMPLETED -> BookingStatusInfo("Completed", Color(0xFF10B981), Color(0xFFD1FAE5))
        BookingStatus.CANCELLED -> BookingStatusInfo("Cancelled", Color(0xFFEF4444), Color(0xFFFEE2E2))
        BookingStatus.REJECTED -> BookingStatusInfo("Rejected", Color(0xFFEF4444), Color(0xFFFEE2E2))
        else -> BookingStatusInfo("Unknown", Color.Gray, Color(0xFFF3F4F6))
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onBookingClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusInfo = getBookingStatusInfo(booking.status)
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = try {
        dateFormatter.format(Date(booking.scheduledDate))
    } catch (e: Exception) {
        "Date not available"
    }

    // Map serviceName to icon and color (match HomeScreen)
    val (categoryIcon, categoryColor) = when (booking.serviceName) {
        "Plumbing" -> Pair(Icons.Default.Build, Color(0xFF6366F1))
        "Electrical" -> Pair(Icons.Default.Settings, Color(0xFFF59E0B))
        "Cleaning" -> Pair(Icons.Default.Home, Color(0xFF10B981))
        else -> Pair(Icons.Default.AccountBox, Color(0xFFFFC107))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onBookingClick(booking.id) },
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
            // Category Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = booking.serviceName,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Booking Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    booking.serviceName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                // Display provider name instead of booking ID
                Text(
                    if (booking.providerName.isNotEmpty()) "Provider: ${booking.providerName}" else "Provider: Not available",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        " $formattedDate, ${booking.scheduledTime}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusInfo.backgroundColor
                ) {
                    Text(
                        statusInfo.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusInfo.color
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Price
                Text(
                    "LKR ${booking.pricing.totalAmount.toInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

data class BookingStatusInfo(
    val name: String,
    val color: Color,
    val backgroundColor: Color
)


