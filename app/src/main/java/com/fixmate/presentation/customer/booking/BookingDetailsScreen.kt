package com.fixmate.presentation.customer.booking

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fixmate.R
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingStatus
import com.fixmate.ui.theme.SevaLKTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.painter.Painter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    navController: NavController,
    bookingId: String,
    modifier: Modifier = Modifier,
    viewModel: BookingDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(bookingId) {
        viewModel.loadBookingDetails(bookingId)
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFFC107))
        }
        return
    }

    if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error!!, color = Color.Red)
        }
        return
    }

    val booking = state.booking
    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Booking not found", color = Color.Red)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Status Card
                BookingStatusCard(booking.status)

                Spacer(modifier = Modifier.height(24.dp))

                // Provider Info - Show for all statuses
                ProviderInfoSection(
                    booking = booking,
                    providerDetails = state.providerDetails
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Service Details - Show for all statuses
                ServiceDetailsSection(booking)

                // Only show payment summary for IN_PROGRESS or later statuses
                if (booking.status != BookingStatus.PENDING) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Payment Summary
                    PaymentSummarySection(booking)
                }

                // Add bottom padding for scrollable content
                Spacer(modifier = Modifier.height(240.dp))
            }
        }

        // Bottom fixed content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Rating Section - Show for all statuses
            RatingSection()
            Spacer(modifier = Modifier.height(16.dp))

            // Payment Button - Only show for IN_PROGRESS
            if (booking.status == BookingStatus.IN_PROGRESS) {
                PaymentButton(bookingId, navController)
            }
        }
    }
}

@Composable
private fun BookingStatusCard(status: BookingStatus) {
    val (icon, title, description, color) = when (status) {
        BookingStatus.COMPLETED -> StatusInfo(
            icon = painterResource(id = R.drawable.check_circle1),
            title = "Service Completed",
            description = "Your service has been completed successfully",
            color = Color(0xFF10B981)
        )
        BookingStatus.PENDING -> StatusInfo(
            icon = painterResource(id = R.drawable.clock1),
            title = "Service Pending",
            description = "Waiting for service provider to respond to your request",
            color = Color(0xFFF59E0B)
        )
        BookingStatus.IN_PROGRESS -> StatusInfo(
            icon = painterResource(id = R.drawable.credit_card1),
            title = "Service Unpaid",
            description = "Payment is pending for the completed service",
            color = Color(0xFFEA580C)
        )
        BookingStatus.ACCEPTED, BookingStatus.CONFIRMED -> StatusInfo(
            icon = painterResource(id = R.drawable.clipboard_check),
            title = "Service In Progress",
            description = "Service provider has accepted your request",
            color = Color(0xFF3B82F6)
        )
        BookingStatus.REJECTED -> StatusInfo(
            icon = painterResource(id = R.drawable.circle_x),
            title = "Service Rejected",
            description = "Your request was declined by the service provider",
            color = Color(0xFFEF4444)
        )
        else -> StatusInfo(
            icon = painterResource(id = R.drawable.circle_x),
            title = status.name,
            description = "Status: ${status.name.lowercase()}",
            color = Color.Gray
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(
                description,
                fontSize = 14.sp,
                color = color.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class StatusInfo(
    val icon: Painter,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun ProviderInfoSection(
    booking: Booking,
    providerDetails: ProviderDetails?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFF3F4F6), CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = providerDetails?.name ?: booking.providerName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = providerDetails?.serviceType ?: booking.serviceName,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", providerDetails?.rating ?: 0f),
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${providerDetails?.reviewCount ?: 0})",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        IconButton(
            onClick = { 
                providerDetails?.phoneNumber?.let { number ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$number")
                    }
                    context.startActivity(intent)
                }
            }
        ) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.Gray)
        }

        IconButton(onClick = { /* Handle message */ }) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFC107),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.message_circle),
                    contentDescription = "Message",
                    tint = Color.Black,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun ServiceDetailsSection(booking: Booking) {
    Text(
        "Service Details",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(16.dp))

    ServiceDetailRow("Service", booking.serviceName)
    
    // Different duration display based on status
    val durationText = when (booking.status) {
        BookingStatus.PENDING -> "Based on Work Type"
        BookingStatus.ACCEPTED, BookingStatus.CONFIRMED -> "Based on Work Type"
        BookingStatus.IN_PROGRESS -> booking.estimatedDuration
        else -> booking.estimatedDuration
    }
    ServiceDetailRow("Duration", durationText)
    ServiceDetailRow(
        "Date & Time", 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(booking.scheduledDate)) + ", " + booking.scheduledTime
    )
    ServiceDetailRow(
        "Location", 
        booking.serviceAddress.ifEmpty { 
            "${booking.serviceLocation.city} ${booking.serviceLocation.country}"
        }
    )
}

@Composable
fun PaymentSummarySection(booking: Booking) {
    Text(
        "Payment Summary",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(16.dp))

    PaymentRow("Base Price", "LKR ${booking.pricing.basePrice}")
    booking.pricing.additionalCharges.forEach { charge ->
        PaymentRow(charge.description, "LKR ${charge.amount}")
    }
    if (booking.pricing.travelFee > 0) {
        PaymentRow("Travel Fee", "LKR ${booking.pricing.travelFee}")
    }
    PaymentRow("Platform Fee", "LKR ${booking.pricing.tax}")

    Spacer(modifier = Modifier.height(8.dp))
    Divider(color = Color(0xFFE5E7EB))
    Spacer(modifier = Modifier.height(8.dp))

    PaymentRow("Total", "LKR ${booking.pricing.totalAmount}", isTotal = true)
}

@Composable
fun RatingSection(
    modifier: Modifier = Modifier
) {
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }

    // Rating Section
    Text(
        "Rate this service",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE5E7EB),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { rating = index + 1 }
                    .padding(2.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = reviewText,
        onValueChange = { reviewText = it },
        placeholder = { Text("Share your experience (Optional)", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFE5E7EB),
            unfocusedBorderColor = Color(0xFFE5E7EB)
        )
    )
}

@Composable
fun PaymentButton(bookingId: String, navController: NavController) {
    // Proceed to Payment Button
    Button(
        onClick = { 
            navController.navigate("stripe_payment/$bookingId")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
    ) {
        Icon(
            painter = painterResource(id = R.drawable.dollar_sign), // Use dollar sign icon
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Proceed to Payment",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun ServiceDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PaymentRow(
    label: String,
    amount: String,
    isTotal: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = if (isTotal) 18.sp else 16.sp,
            color = Color.Black,
            fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            amount,
            fontSize = if (isTotal) 18.sp else 16.sp,
            color = Color.Black,
            fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Preview
@Composable
fun BookingDetailsScreenPreview() {
    SevaLKTheme {
        BookingDetailsScreen(
            navController = NavController(context = LocalContext.current),
            bookingId = "1"
        )
    }
}


