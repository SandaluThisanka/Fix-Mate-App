package com.fixmate.data.models

enum class PricingModel(val displayName: String) {
    HOURLY("Hourly"),
    DAILY_FIXED("Daily Fixed Fee"),
    PER_SQ_FT("Price per Sq. Ft."),
    FIXED("Fixed Fee")
}