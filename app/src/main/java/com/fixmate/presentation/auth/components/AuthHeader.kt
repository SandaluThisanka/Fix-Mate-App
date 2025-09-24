package com.fixmate.presentation.auth.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.ui.theme.S_YELLOW


@Composable
fun AuthHeader(
    title: String,
    showBackButton: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.7f)
                // Creates a quadratic curve for the wave effect
                quadraticBezierTo(
                    x1 = size.width * 0.5f, y1 = size.height, // Control point
                    x2 = 0f, y2 = size.height * 0.7f // End point
                )
                close()
            }
            drawPath(
                path = path,
                color = S_YELLOW
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 50.dp) // Adjust padding to position text
        )
        if (showBackButton) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    tint = Color.White,
                    contentDescription = "Back",
                )
            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun AuthHeaderPreview() {
    Surface {
        AuthHeader(
            title = "Lets!",
            showBackButton = true
        )
    }
}