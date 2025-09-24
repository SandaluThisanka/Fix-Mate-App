package com.fixmate.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.R

@Composable
fun ProgressIndicator(
    currentStep: Int = 2,
    totalSteps: Int = 3,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            // Step Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(if (step == currentStep) 30.dp else 24.dp)
            ) {
                // Outer ring for current step only
                if (step == currentStep) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF1D4EDA),
                                shape = CircleShape
                            )
                    )
                }

                // Inner circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = when {
                                step <= currentStep -> Color(0xFF1D4EDA)
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                step <= currentStep -> Color.Transparent
                                else -> Color.Gray
                            },
                            shape = CircleShape
                        )
                ) {
                    if (step < currentStep) {
                        // Checkmark for completed steps
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    } else if (step == currentStep) {
                        // Current step number in white
                        Text(
                            text = step.toString(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        // Future step number in gray
                        Text(
                            text = step.toString(),
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Connecting line (except after last step)
            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(42.dp)
                        .background(
                            color = if (step < currentStep) Color(0xFF1D4EDA) else Color.Gray
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressIndicatorPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text("Step 1 - Current")
        ProgressIndicator(currentStep = 1)

        Text("Step 2 - Current")
        ProgressIndicator(currentStep = 2)

        Text("Step 3 - Completed")
        ProgressIndicator(currentStep = 3)

        Text("Step 4 of 5 - Current")
        ProgressIndicator(currentStep = 4, totalSteps = 5)
    }
}