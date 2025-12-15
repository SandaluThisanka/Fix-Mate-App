package com.fixmate.presentation.provider.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.data.models.ServiceProvider
import com.fixmate.data.repositories.ImageRepository
import com.fixmate.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class ProviderProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : ViewModel() {
    
    private val _providerProfile = MutableStateFlow<ProviderProfile?>(null)
    val providerProfile: StateFlow<ProviderProfile?> = _providerProfile

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage

    init {
        loadProviderProfile()
    }

    private fun loadProviderProfile() {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            try {
                Log.d("ProviderProfileVM", "Loading profile for user: ${currentUser.uid}")
                
                // Load user document
                val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                    .document(currentUser.uid)
                    .get()
                    .await()

                // Load provider document
                val providerDoc = firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (!providerDoc.exists()) {
                    Log.e("ProviderProfileVM", "Provider document missing for user ${currentUser.uid}")
                    // Create default profile to avoid blank screen
                    _providerProfile.value = ProviderProfile(
                        name = userDoc.getString("displayName") ?: "Provider",
                        memberSince = formatDate(System.currentTimeMillis()),
                        completedJobs = 0,
                        totalJobs = 0,
                        location = "Not set",
                        totalEarnings = "LKR 0",
                        email = currentUser.email ?: "",
                        phoneNumber = userDoc.getString("phoneNumber") ?: "",
                        isAvailable = true,
                        responseTime = "within 1 hour",
                        profileImageUrl = userDoc.getString("profileImageUrl")
                    )
                    return@launch
                }

                val providerData = providerDoc.data ?: emptyMap<String, Any?>()
                Log.d("ProviderProfileVM", "Provider data loaded: ${providerData.keys}")
                
                val provider = ServiceProvider.fromMap(providerData)

                if (provider == null) {
                    Log.e("ProviderProfileVM", "Failed to parse provider data")
                    throw Exception("Provider data not found")
                }

                val locationText = when {
                    provider.serviceLocation.formattedAddress.isNotBlank() -> provider.serviceLocation.formattedAddress
                    provider.serviceLocation.city.isNotBlank() -> "${provider.serviceLocation.city}, ${provider.serviceLocation.province}"
                    else -> "Location not set"
                }

                _providerProfile.value = ProviderProfile(
                    name = provider.businessName.ifBlank { userDoc.getString("displayName") ?: "Provider" },
                    memberSince = formatDate(provider.createdAt),
                    completedJobs = provider.completedJobs,
                    totalJobs = provider.totalJobs,
                    location = locationText,
                    totalEarnings = formatCurrency(provider.totalEarnings),
                    email = currentUser.email ?: "",
                    phoneNumber = userDoc.getString("phoneNumber") ?: "",
                    isAvailable = provider.isAvailable,
                    responseTime = provider.responseTime,
                    profileImageUrl = userDoc.getString("profileImageUrl") ?: provider.profileImageUrl
                )
                
                Log.d("ProviderProfileVM", "Profile loaded successfully: ${_providerProfile.value?.name}")
            } catch (e: Exception) {
                Log.e("ProviderProfileVM", "Error loading profile", e)
                // Set a default profile to avoid blank screen
                _providerProfile.value = ProviderProfile(
                    name = currentUser.displayName ?: "Provider",
                    memberSince = formatDate(System.currentTimeMillis()),
                    completedJobs = 0,
                    totalJobs = 0,
                    location = "Not set",
                    totalEarnings = "LKR 0",
                    email = currentUser.email ?: "",
                    phoneNumber = "",
                    isAvailable = true,
                    responseTime = "within 1 hour",
                    profileImageUrl = null
                )
            }
        }
    }

    fun updateProviderProfile(name: String, phoneNumber: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()

                firestore.collection(Constants.COLLECTION_USERS)
                    .document(currentUser.uid)
                    .update(
                        mapOf(
                            "phoneNumber" to phoneNumber,
                            "updatedAt" to now
                        )
                    )
                    .await()

                firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                    .document(currentUser.uid)
                    .update(
                        mapOf(
                            "businessName" to name,
                            "updatedAt" to now
                        )
                    )
                    .await()

                loadProviderProfile() // Reload profile after update
            } catch (e: Exception) {
                Log.e("ProviderProfileVM", "Error updating profile", e)
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _isUploadingImage.value = true
            try {
                // Get current profile image URL to delete old image if exists
                val currentProfile = _providerProfile.value
                val oldImageUrl = currentProfile?.profileImageUrl
                
                val result = imageRepository.uploadProfileImage(imageUri, currentUser.uid)
                result.onSuccess { imageUrl ->
                    val now = System.currentTimeMillis()

                    // Update provider profile image within service_providers collection
                    firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                        .document(currentUser.uid)
                        .update(
                            mapOf(
                                "profileImageUrl" to imageUrl,
                                "updatedAt" to now
                            )
                        )
                        .await()

                    firestore.collection(Constants.COLLECTION_USERS)
                        .document(currentUser.uid)
                        .update(
                            mapOf(
                                "updatedAt" to now
                            )
                        )
                        .await()

                    // Delete old image if it exists
                    oldImageUrl?.let { oldUrl ->
                        try {
                            val fileName = extractFileNameFromUrl(oldUrl)
                            if (fileName.isNotEmpty()) {
                                imageRepository.deleteProfileImage(fileName)
                            }
                        } catch (e: Exception) {
                            Log.w("ProviderProfileVM", "Could not delete old profile image", e)
                        }
                    }

                    loadProviderProfile() // Reload profile to show new image
                    Log.d("ProviderProfileVM", "Profile image uploaded successfully: $imageUrl")
                }.onFailure { exception ->
                    Log.e("ProviderProfileVM", "Error uploading profile image", exception)
                }
            } catch (e: Exception) {
                Log.e("ProviderProfileVM", "Error uploading profile image", e)
            } finally {
                _isUploadingImage.value = false
            }
        }
    }
    
    private fun extractFileNameFromUrl(url: String): String {
        return try {
            url.substringAfterLast("/")
        } catch (e: Exception) {
            ""
        }
    }

    fun updateAvailabilityStatus(isAvailable: Boolean) {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                firestore.collection(Constants.COLLECTION_SERVICE_PROVIDERS)
                    .document(currentUser.uid)
                    .update(
                        mapOf(
                            "isAvailable" to isAvailable,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

                firestore.collection(Constants.COLLECTION_USERS)
                    .document(currentUser.uid)
                    .update(
                        mapOf(
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                
                loadProviderProfile() // Reload profile after update
            } catch (e: Exception) {
                Log.e("ProviderProfileVM", "Failed to update availability", e)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _providerProfile.value = null
        Log.d("ProviderProfileVM", "User signed out")
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            Log.e("ProviderProfileVM", "Error formatting date", e)
            "Unknown"
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("LKR")
        return format.format(amount)
    }
}
