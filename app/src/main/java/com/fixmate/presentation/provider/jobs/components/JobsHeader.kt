package com.fixmate.presentation.provider.jobs.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmate.data.models.JobStatus

@Composable
fun JobsHeader(
    currentFilter: JobStatus,
    onFilterChanged: (JobStatus) -> Unit
) {
    Column {
        // Header
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "My Jobs",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Gray.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            JobStatus.values().forEach { status ->
                FilterTab(
                    text = status.name.lowercase().replaceFirstChar { it.uppercase() },
                    isSelected = currentFilter == status,
                    onClick = { onFilterChanged(status) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
