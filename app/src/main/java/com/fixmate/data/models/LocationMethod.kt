package com.fixmate.data.models

data class LocationMethod(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val isSelected: Boolean = false
)
