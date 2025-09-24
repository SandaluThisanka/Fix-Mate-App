package com.fixmate.data.models

// Simplified UI models for billing
data class BillServiceItem(
    val serviceId: Int,
    val serviceName: String,
    val pricingModel: PricingModel,
    val basePrice: String, // Price per unit from provider settings
    val quantity: String = "", // Hours/Days/SqFt/etc based on pricing model
    val calculatedAmount: Double = 0.0
) {
    fun getInputLabel(): String {
        return when (pricingModel) {
            PricingModel.HOURLY -> "Hours worked"
            PricingModel.DAILY_FIXED -> "Number of days"
            PricingModel.PER_SQ_FT -> "Square feet"
            PricingModel.FIXED -> "Fixed amount"
        }
    }
    
    fun getInputPlaceholder(): String {
        return when (pricingModel) {
            PricingModel.HOURLY -> "Enter hours worked"
            PricingModel.DAILY_FIXED -> "Enter number of days"
            PricingModel.PER_SQ_FT -> "Enter square feet"
            PricingModel.FIXED -> "Fixed fee"
        }
    }
    
    fun calculateAmount(): Double {
        if (quantity.isBlank()) return 0.0
        
        val quantityValue = quantity.toDoubleOrNull() ?: 0.0
        val priceValue = basePrice.toDoubleOrNull() ?: 0.0
        
        return when (pricingModel) {
            PricingModel.HOURLY -> quantityValue * priceValue
            PricingModel.DAILY_FIXED -> quantityValue * priceValue
            PricingModel.PER_SQ_FT -> quantityValue * priceValue
            PricingModel.FIXED -> priceValue // Ignore quantity for fixed pricing
        }
    }
}

data class BillAdditionalCost(
    val name: String,
    val amount: Double
)
