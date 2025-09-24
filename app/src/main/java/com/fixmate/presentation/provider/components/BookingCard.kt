package com.fixmate.presentation.provider.components

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BookingCard(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Text("Booking Card")
    }
}
