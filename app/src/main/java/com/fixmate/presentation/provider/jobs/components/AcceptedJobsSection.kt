package com.fixmate.presentation.provider.jobs.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fixmate.data.models.Booking

@Composable
fun AcceptedJobsSection(
    todaysEarnings: String,
    jobsToday: Int,
    bookings: List<Booking>,
    onViewDetails: (String) -> Unit,
    onCreateBill: (String) -> Unit
) {
    Column {
        // Status cards row
        StatusCardsRow(
            todaysEarnings = todaysEarnings,
            jobsToday = jobsToday
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Accepted bookings list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(bookings) { booking ->
                AcceptedJobCard(
                    booking = booking,
                    onViewDetails = { onViewDetails(booking.id) },
                    onCreateBill = { onCreateBill(booking.id) }
                )
            }
        }
    }
}
