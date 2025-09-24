package com.fixmate.presentation.provider.jobs.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingStatus
import com.fixmate.data.models.toJobDate
import com.fixmate.data.models.toJobDescription
import com.fixmate.data.models.toJobTime
import com.fixmate.data.models.toJobTitle
import com.fixmate.presentation.components.CustomerAvatar

@Composable
fun CompletedJobCard(
    booking: Booking,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Customer info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Customer Avatar using the reusable component
                    CustomerAvatar(
                        customerId = booking.customerId ?: "",
                        size = 40.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = booking.customerName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "4.8", // Static rating for now
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                
                // Completed status badge
                Surface(
                    color = Color.Green.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Completed",
                        color = Color.Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Job title
            Text(
                text = booking.toJobTitle(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            
            // Job description
            Text(
                text = booking.toJobDescription(),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Job details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📅 ${booking.toJobDate()}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "🕒 ${booking.toJobTime()}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = "📍 ${booking.serviceLocation.city}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // View Details button (centered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Details")
                }
            }
        }
    }
}

@Preview
@Composable
fun CompletedJobCardPreview() {
    CompletedJobCard(
        booking = Booking(
            id = "1",
            customerId = "sample_customer_id", // Added for CustomerAvatar functionality
            customerName = "Sarah Johnson",
            serviceName = "Kitchen Plumbing Repair",
            description = "Kitchen sink is leaking from the pipes underneath. Water...",
            scheduledDate = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // Yesterday (completed)
            scheduledTime = "10:00 AM",
            status = BookingStatus.COMPLETED,
            createdAt = System.currentTimeMillis() - (48 * 60 * 60 * 1000) // 2 days ago
        ),
        onViewDetails = {}
    )
}
