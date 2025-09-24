package com.fixmate.presentation.chat.components

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MessageBubble(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Text(message)
    }
}
