package com.fixmate.data.models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.fixmate.utils.Constants
import java.util.Locale

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val phoneNumber: String = "",
    val userType: UserType = UserType.CUSTOMER,
    val address: Address? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val fcmToken: String = "", // For push notifications
    val preferences: UserPreferences = UserPreferences()
) {
    companion object {
        private val gson = Gson()
        
        // Convert User object to JSON string
        fun toJson(user: User): String {
            return gson.toJson(user)
        }
        
        // Convert JSON string to User object
        fun fromJson(json: String): User? {
            return try {
                gson.fromJson(json, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
        
        // Convert User object to Map (for Firebase)
        fun toMap(user: User): Map<String, Any?> {
            val json = toJson(user)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val map = gson.fromJson<MutableMap<String, Any?>>(json, type)

            // Persist lowercase value to stay compatible with Firestore rules
            map["userType"] = when (user.userType) {
                UserType.SERVICE_PROVIDER -> Constants.USER_TYPE_PROVIDER
                UserType.CUSTOMER -> Constants.USER_TYPE_CUSTOMER
            }

            return map
        }

        // Convert Map to User object (from Firebase)
        fun fromMap(map: Map<String, Any?>): User? {
            return try {
                val mutableMap = map.toMutableMap()
                val userTypeString = (mutableMap["userType"] as? String)?.lowercase(Locale.US)
                mutableMap["userType"] = when (userTypeString) {
                    "provider", "service_provider", UserType.SERVICE_PROVIDER.name.lowercase(Locale.US) -> UserType.SERVICE_PROVIDER.name
                    "customer", UserType.CUSTOMER.name.lowercase(Locale.US) -> UserType.CUSTOMER.name
                    else -> UserType.CUSTOMER.name
                }

                val json = gson.toJson(mutableMap)
                fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
    }
}
