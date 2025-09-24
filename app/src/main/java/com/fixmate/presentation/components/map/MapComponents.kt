package com.fixmate.presentation.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search providers..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(25.dp))
            .background(Color.White, RoundedCornerShape(25.dp)),
        shape = RoundedCornerShape(25.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun ServiceTypeFilters(
    selectedType: ServiceType,
    onTypeSelected: (ServiceType) -> Unit,
    modifier: Modifier = Modifier,
    showAllOption: Boolean = true
) {
    LazyRow(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val types = if (showAllOption) {
            ServiceType.values().toList()
        } else {
            ServiceType.values().filter { it != ServiceType.ALL }
        }
        
        items(types) { type ->
            ServiceTypeChip(
                type = type,
                isSelected = type == selectedType,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
fun ServiceTypeChip(
    type: ServiceType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) type.color else Color.White
    val contentColor = if (isSelected) Color.White else Color.Gray

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = type.displayName,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
