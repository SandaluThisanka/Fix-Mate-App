package com.fixmate.data.models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ServiceProvider(
    val id: String = "",
    val userId: String = "", // Reference to User
    val businessName: String = "",
    val description: String = "",
    val services: List<Service> = emptyList(),
    val price: String = "", // Add price field for service provider pricing
    val serviceRadius: Double = 10.0, // in kilometers
    val experience: Int = 0, // Years of experience
    val rating: Float = 0.0f,
    val totalReviews: Int = 0,
    val completedJobs: Int = 0,
    val responseTime: String = "", // e.g., "Within 1 hour"
    val isVerified: Boolean = false,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: ProviderStatus = ProviderStatus.PENDING,
    val totalEarnings: Double = 0.0,
    val totalJobs: Int = 0,
    val city: String = "",
    val province: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val profileImageUrl : String = "",
    val serviceLocation: ServiceLocation = ServiceLocation()
) {
    companion object {
        private val gson = Gson()
        
        // Convert ServiceProvider object to JSON string
        fun toJson(serviceProvider: ServiceProvider): String {
            return gson.toJson(serviceProvider)
        }
        
        // Convert JSON string to ServiceProvider object
        fun fromJson(json: String): ServiceProvider? {
            return try {
                gson.fromJson(json, ServiceProvider::class.java)
            } catch (e: Exception) {
                null
            }
        }
        
        // Convert ServiceProvider object to Map (for Firebase)
        fun toMap(serviceProvider: ServiceProvider): Map<String, Any?> {
            val json = toJson(serviceProvider)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            return gson.fromJson(json, type)
        }
        
        // Convert Map to ServiceProvider object (from Firebase)
        fun fromMap(map: Map<String, Any?>): ServiceProvider? {
            return try {
                val json = gson.toJson(map)
                fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
    }
}

enum class ProviderStatus {
    PENDING, APPROVED, SUSPENDED, REJECTED
}



