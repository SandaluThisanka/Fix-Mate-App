package com.fixmate.presentation.provider.jobs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.fixmate.data.models.Booking

@Composable
fun DoneJobsSection(
    bookings: List<Booking>,
    onViewDetails: (String) -> Unit = {}
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(bookings) { booking ->
            CompletedJobCard(
                booking = booking,
                onViewDetails = {
                    selectedBooking = booking
                    showBottomSheet = true
                }
            )
        }
    }

    if (showBottomSheet && selectedBooking != null) {
        CompletedJobDetailsBottomSheet(
            booking = selectedBooking,
            onDismiss = {
                showBottomSheet = false
                selectedBooking = null
            }
        )
    }
}
