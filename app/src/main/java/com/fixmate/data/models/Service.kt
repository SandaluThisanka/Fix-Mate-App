package com.fixmate.data.models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Service(
    val id: Int,
    val name: String,
    val description: String,
    val pricingModel: PricingModel,
    val isSelected: Boolean = false,
    val price: String = "",
    val isExpanded: Boolean = false
) {
    companion object {
        private val gson = Gson()
        
        // Convert Service object to JSON string
        fun toJson(service: Service): String {
            return gson.toJson(service)
        }
        
        // Convert JSON string to Service object
        fun fromJson(json: String): Service? {
            return try {
                gson.fromJson(json, Service::class.java)
            } catch (e: Exception) {
                null
            }
        }
        
        // Convert Service object to Map (for Firebase)
        fun toMap(service: Service): Map<String, Any?> {
            val json = toJson(service)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            return gson.fromJson(json, type)
        }
        
        // Convert Map to Service object (from Firebase)
        fun fromMap(map: Map<String, Any?>): Service? {
            return try {
                val json = gson.toJson(map)
                fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
    }
}
