package com.fixmate.presentation.provider.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.data.models.ServiceProvider
import com.fixmate.data.repositories.ImageRepository
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
            auth.currentUser?.let { user ->
                try {
                    // Get the provider document
                    val providerDoc = firestore.collection("service_providers")
                        .document(user.uid)
                        .get()
                        .await()
                    
                    val provider = providerDoc.data?.let { ServiceProvider.fromMap(it) }
                        ?: throw Exception("Provider data not found")

                    // Get the user document for additional details
                    val userDoc = firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .await()

                    _providerProfile.value = ProviderProfile(
                        name = provider.businessName,
                        memberSince = formatDate(provider.createdAt),
                        completedJobs = provider.completedJobs,
                        totalJobs = provider.totalJobs,
                        location = "${provider.serviceLocation.country}",
                        totalEarnings = formatCurrency(provider.totalEarnings),
                        email = auth.currentUser?.email ?: "",
                        phoneNumber = userDoc.getString("phoneNumber") ?: "",
                        isAvailable = provider.isAvailable,
                        responseTime = provider.responseTime,
                        profileImageUrl = providerDoc.getString("profileImageUrl") // Get from service_providers collection
                    )
                } catch (e: Exception) {
                    Log.e("ProviderProfileVM", "Error loading profile", e)
                }
            }
        }
    }

    fun updateProviderProfile(name: String, phoneNumber: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val userUpdates = mapOf(
                    "phoneNumber" to phoneNumber,
                    "updatedAt" to System.currentTimeMillis()
                )

                val providerUpdates = mapOf(
                    "businessName" to name,
                    "updatedAt" to System.currentTimeMillis()
                )

                // Update user document
                firestore.collection("users").document(currentUser.uid)
                    .update(userUpdates)
                    .await()

                // Update provider document
                firestore.collection("service_providers").document(currentUser.uid)
                    .update(providerUpdates)
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
                    // Update service_providers collection with new profile image URL
                    firestore.collection("service_providers").document(currentUser.uid)
                        .update("profileImageUrl", imageUrl)
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
                firestore.collection("service_providers")
                    .document(currentUser.uid)
                    .update("isAvailable", isAvailable)
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
