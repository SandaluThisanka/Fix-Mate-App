package com.fixmate.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.data.models.ServiceProvider
import com.fixmate.data.models.Service
import com.fixmate.presentation.components.map.ServiceProvider as MapServiceProvider
import com.fixmate.presentation.components.map.ServiceType
import com.fixmate.presentation.components.map.calculateDistance
import com.fixmate.utils.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import timber.log.Timber

class ServiceProviderRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun numberToDouble(value: Any?): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun getServiceType(service: Service?): ServiceType {
        return when (service?.name?.uppercase()) {
            "MECHANICAL", "ENGINE REPAIR", "OIL CHANGE" -> ServiceType.MECHANICAL
            "ELECTRICAL", "BATTERY", "ALTERNATOR" -> ServiceType.ELECTRICAL
            "SUSPENSION", "SHOCK ABSORBERS" -> ServiceType.SUSPENSION
            "BRAKES", "BRAKE SERVICE", "BRAKE PAD REPLACEMENT" -> ServiceType.BRAKES
            "BODY & CARE", "CAR WASH", "DETAILING" -> ServiceType.BODY_CARE
            "TIRES", "TIRE ROTATION", "WHEEL ALIGNMENT" -> ServiceType.TIRES
            "EMERGENCY", "TOWING", "ROADSIDE ASSISTANCE" -> ServiceType.EMERGENCY
            else -> ServiceType.ALL
        }
    }

    suspend fun getServiceProviders(): List<MapServiceProvider> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                .get()
                .await()
            
            Timber.d("Fetched ${snapshot.documents.size} provider documents from Firestore")

            snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null
                    val businessName = data["businessName"] as? String ?: "Unknown"

                    val serviceLocation = data["serviceLocation"] as? Map<String, Any>
                    val locationLat = numberToDouble(serviceLocation?.get("latitude"))
                    val locationLng = numberToDouble(serviceLocation?.get("longitude"))
                    // Fall back to legacy top-level latitude/longitude if serviceLocation is missing
                    val latitude = if (locationLat != 0.0) locationLat else numberToDouble(data["latitude"])
                    val longitude = if (locationLng != 0.0) locationLng else numberToDouble(data["longitude"])

                    // Skip providers without valid location (check if both are zero OR if location object is missing)
                    if (latitude == 0.0 && longitude == 0.0) {
                        Timber.w("⚠️ Provider '$businessName' (${document.id}) skipped - NO LOCATION SET. Provider needs to set service location.")
                        return@mapNotNull null
                    }

                    val services = data["services"] as? List<Map<String, Any>> ?: emptyList()
                    
                    // Skip providers without services
                    if (services.isEmpty()) {
                        Timber.w("⚠️ Provider '$businessName' (${document.id}) skipped - NO SERVICES ADDED. Provider needs to add services.")
                        return@mapNotNull null
                    }
                    
                    val firstService = services.firstOrNull()
                    val serviceName = firstService?.get("name") as? String
                    val additionalServicesCount = if (services.size > 1) services.size - 1 else 0
                    
                    Timber.d("Provider ${document.id} service: $serviceName, additional: $additionalServicesCount, location: ($latitude, $longitude)")

                    val price = firstService?.get("price") as? String ?: "0"
                    
                    // Fetch user details for phone number and profile image
                    val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                        .document(document.id)
                        .get()
                        .await()
                    val userData = userDoc.data
                    
                    MapServiceProvider(
                        id = document.id,
                        name = data["businessName"] as? String ?: "Unknown",
                        type = getServiceTypeFromName(serviceName),
                        latitude = latitude,
                        longitude = longitude,
                        rating = (data["rating"] as? Double)?.toFloat() ?: 0f,
                        description = "${serviceName ?: "Service"}${if (additionalServicesCount > 0) " +$additionalServicesCount more" else ""}",
                        hourlyRate = price.toDoubleOrNull() ?: 0.0,
                        phone = userData?.get("phoneNumber") as? String ?: "",
                        completedJobs = (data["completedJobs"] as? Long)?.toInt() ?: 0,
                        profileImageUrl = userData?.get("profileImageUrl") as? String ?: ""
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error mapping provider document ${document.id}")
                    null
                }
            }.also { providers ->
                Timber.d("✅ Successfully loaded ${providers.size} providers with location and services set")
                if (providers.isEmpty()) {
                    Timber.w("⚠️ NO PROVIDERS AVAILABLE ON MAP. Providers need to: 1) Set service location, 2) Add services")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting providers")
            emptyList()
        }
    }

    private fun getServiceTypeFromName(serviceName: String?): ServiceType {
        return when (serviceName?.uppercase()) {
            "MECHANICAL", "ENGINE REPAIR", "OIL CHANGE" -> ServiceType.MECHANICAL
            "ELECTRICAL", "BATTERY", "ALTERNATOR" -> ServiceType.ELECTRICAL
            "SUSPENSION", "SHOCK ABSORBERS" -> ServiceType.SUSPENSION
            "BRAKES", "BRAKE SERVICE", "BRAKE PAD REPLACEMENT" -> ServiceType.BRAKES
            "BODY & CARE", "CAR WASH", "DETAILING" -> ServiceType.BODY_CARE
            "TIRES", "TIRE ROTATION", "WHEEL ALIGNMENT" -> ServiceType.TIRES
            "EMERGENCY", "TOWING", "ROADSIDE ASSISTANCE" -> ServiceType.EMERGENCY
            else -> {
                Timber.d("Unknown service type: $serviceName")
                ServiceType.ALL
            }
        }
    }

    suspend fun getServiceProviderById(providerId: String): ServiceProvider? {
        return try {
            Timber.d("Fetching provider with ID: $providerId")
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(providerId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    val providerProfile = data["providerProfile"] as? Map<String, Any?>
                    val provider = if (providerProfile != null) {
                        ServiceProvider.fromMap(providerProfile)
                    } else {
                        null
                    }
                    Timber.d("Provider found: ${provider?.businessName}")
                    provider
                } else {
                    Timber.w("Document exists but data is null for provider $providerId")
                    null
                }
            } else {
                Timber.w("Document does not exist for provider $providerId")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching provider $providerId")
            null
        }
    }

    suspend fun searchServiceProviders(query: String): List<MapServiceProvider> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                .get()
                .await()
            
            Timber.d("Searching ${snapshot.documents.size} providers for query: '$query'")

            snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    val businessName = data["businessName"] as? String ?: return@mapNotNull null
                    val description = data["description"] as? String ?: ""
                    val services = data["services"] as? List<Map<String, Any>> ?: emptyList()

                    val matchesQuery = query.isEmpty() || 
                        businessName.contains(query, ignoreCase = true) ||
                        description.contains(query, ignoreCase = true) ||
                        services.any { service -> 
                            (service["name"] as? String)?.contains(query, ignoreCase = true) == true
                        }
                    
                    if (!matchesQuery) return@mapNotNull null

                    val serviceLocation = data["serviceLocation"] as? Map<String, Any>
                    val locationLat = numberToDouble(serviceLocation?.get("latitude"))
                    val locationLng = numberToDouble(serviceLocation?.get("longitude"))
                    val latitude = if (locationLat != 0.0) locationLat else numberToDouble(data["latitude"])
                    val longitude = if (locationLng != 0.0) locationLng else numberToDouble(data["longitude"])

                    // Skip providers without location
                    if (latitude == 0.0 && longitude == 0.0) return@mapNotNull null
                    
                    // Skip providers without services
                    if (services.isEmpty()) return@mapNotNull null

                    val firstService = services.firstOrNull()
                    val serviceName = firstService?.get("name") as? String
                    val additionalServicesCount = if (services.size > 1) services.size - 1 else 0

                    val price = firstService?.get("price") as? String ?: "0"
                    
                    // Fetch user details for phone number and profile image
                    val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                        .document(document.id)
                        .get()
                        .await()
                    val userData = userDoc.data
                    
                    MapServiceProvider(
                        id = document.id,
                        name = businessName,
                        type = getServiceTypeFromName(serviceName),
                        latitude = latitude,
                        longitude = longitude,
                        rating = (data["rating"] as? Double)?.toFloat() ?: 0f,
                        description = "${serviceName ?: "Service"}${if (additionalServicesCount > 0) " +$additionalServicesCount more" else ""}",
                        phone = userData?.get("phoneNumber") as? String ?: "",
                        hourlyRate = price.toDoubleOrNull() ?: 0.0,
                        completedJobs = (data["completedJobs"] as? Long)?.toInt() ?: 0,
                        profileImageUrl = userData?.get("profileImageUrl") as? String ?: ""
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error mapping provider document ${document.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching providers")
            emptyList()
        }
    }

    private fun isProviderInRange(
        providerLat: Double,
        providerLng: Double,
        userLat: Double,
        userLng: Double,
        radiusKm: Double
    ): Boolean {
        val distance = calculateDistance(
            userLat, userLng,
            providerLat, providerLng
        )
        return distance <= radiusKm * 1000
    }

    suspend fun getNearbyServiceProviders(
        userLat: Double,
        userLng: Double,
        radiusKm: Double = Constants.NEARBY_PROVIDER_RADIUS_KM
    ): List<MapServiceProvider> {
        return try {
            val allProviders = getServiceProviders()
            allProviders.filter { provider ->
                isProviderInRange(
                    provider.latitude,
                    provider.longitude,
                    userLat,
                    userLng,
                    radiusKm
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting nearby providers")
            emptyList()
        }
    }
}