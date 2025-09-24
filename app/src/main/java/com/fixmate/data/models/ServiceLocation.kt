package com.fixmate.data.models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ServiceLocation(
    val address: String = "",
    val city: String = "",
    val province: String = "",
    val country: String = "",  // Add country field
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val formattedAddress: String = "" // For full formatted address from Google Maps
) {
    companion object {
        private val gson = Gson()
        
        // Convert ServiceLocation object to Map (for Firebase)
        fun toMap(serviceLocation: ServiceLocation): Map<String, Any?> {
            val json = gson.toJson(serviceLocation)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            return gson.fromJson(json, type)
        }
        
        // Convert Map to ServiceLocation object (from Firebase)
        fun fromMap(map: Map<String, Any?>): ServiceLocation? {
            return try {
                val json = gson.toJson(map)
                gson.fromJson(json, ServiceLocation::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
