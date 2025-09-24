package com.fixmate.data.models

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = true,
    val preferredLanguage: String = "en",
    val maxSearchRadius: Int = 50 // in kilometers
)

