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
    private fun getServiceType(service: Service?): ServiceType {
        return when (service?.name?.uppercase()) {
            "PLUMBING" -> ServiceType.PLUMBING
            "ELECTRICAL" -> ServiceType.ELECTRICAL
            "CLEANING", "CLEANING (RESIDENTIAL)" -> ServiceType.CLEANING
            else -> ServiceType.ALL
        }
    }

    suspend fun getServiceProviders(): List<MapServiceProvider> {
        return try {
            val snapshot = firestore.collection("service_providers")
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    val serviceLocation = data["serviceLocation"] as? Map<String, Any>
                    val latitude = serviceLocation?.get("latitude") as? Double ?: 0.0
                    val longitude = serviceLocation?.get("longitude") as? Double ?: 0.0

                    val services = data["services"] as? List<Map<String, Any>> ?: emptyList()
                    val firstService = services.firstOrNull()

                    val serviceName = firstService?.get("name") as? String
                    val additionalServicesCount = if (services.size > 1) services.size - 1 else 0
                    
                    Timber.d("Provider ${document.id} service: $serviceName, additional: $additionalServicesCount")

                    val price = firstService?.get("price") as? String ?: "0"
                    
                    MapServiceProvider(
                        id = document.id,
                        name = data["businessName"] as? String ?: "Unknown",
                        type = getServiceTypeFromName(serviceName),
                        latitude = latitude,
                        longitude = longitude,
                        rating = (data["rating"] as? Double)?.toFloat() ?: 0f,
                        description = "${serviceName ?: "Service"}${if (additionalServicesCount > 0) " +$additionalServicesCount more" else ""}",
                        hourlyRate = price.toDoubleOrNull() ?: 0.0,
                        phone = data["phone"] as? String ?: "",
                        completedJobs = (data["completedJobs"] as? Long)?.toInt() ?: 0,
                        profileImageUrl = data["profileImageUrl"] as? String ?: "" // Get directly from service_providers collection
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error mapping provider document ${document.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting providers")
            emptyList()
        }
    }

    private fun getServiceTypeFromName(serviceName: String?): ServiceType {
        return when (serviceName?.uppercase()) {
            "PLUMBING" -> ServiceType.PLUMBING
            "ELECTRICAL", "ELECTRICAL REPAIR" -> ServiceType.ELECTRICAL
            "CLEANING", "CLEANING (RESIDENTIAL)" -> ServiceType.CLEANING
            else -> {
                Timber.d("Unknown service type: $serviceName")
                ServiceType.ALL
            }
        }
    }

    suspend fun getServiceProviderById(providerId: String): ServiceProvider? {
        return try {
            Timber.d("Fetching provider with ID: $providerId")
            val document = firestore.collection("service_providers")
                .document(providerId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    val provider = ServiceProvider.fromMap(data)
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
            val snapshot = firestore.collection("service_providers")
                .get()
                .await()

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
                    val latitude = serviceLocation?.get("latitude") as? Double ?: 0.0
                    val longitude = serviceLocation?.get("longitude") as? Double ?: 0.0

                    val firstService = services.firstOrNull()
                    val serviceName = firstService?.get("name") as? String
                    val additionalServicesCount = if (services.size > 1) services.size - 1 else 0

                    val price = firstService?.get("price") as? Long ?: 0L 
                    
                    MapServiceProvider(
                        id = document.id,
                        name = businessName,
                        type = getServiceTypeFromName(serviceName),
                        latitude = latitude,
                        longitude = longitude,
                        rating = (data["rating"] as? Double)?.toFloat() ?: 0f,
                        description = "${serviceName ?: "Service"}${if (additionalServicesCount > 0) " +$additionalServicesCount more" else ""}",
                        phone = data["phone"] as? String ?: "",
                        hourlyRate = price.toDouble(),
                        completedJobs = (data["completedJobs"] as? Long)?.toInt() ?: 0,
                        profileImageUrl = data["profileImageUrl"] as? String ?: "" // Get directly from service_providers collection
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