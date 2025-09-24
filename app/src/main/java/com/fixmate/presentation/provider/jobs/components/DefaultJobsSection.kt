package com.fixmate.presentation.provider.jobs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.fixmate.data.models.Booking

@Composable
fun DefaultJobsSection(
    bookings: List<Booking>,
    onViewDetails: (String) -> Unit,
    onQuickAccept: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(bookings) { booking ->
            JobCard(
                booking = booking,
                onViewDetails = { onViewDetails(booking.id) },
                onQuickAccept = { onQuickAccept(booking.id) }
            )
        }
    }
}
