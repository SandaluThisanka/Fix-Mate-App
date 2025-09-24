package com.fixmate.domain.entities

object BusinessRules {
    const val MIN_SERVICE_PRICE = 100.0
    const val MAX_SERVICE_PRICE = 50000.0
    const val COMMISSION_RATE = 0.1
    
    fun isValidServicePrice(price: Double): Boolean {
        return price in MIN_SERVICE_PRICE..MAX_SERVICE_PRICE
    }
}
