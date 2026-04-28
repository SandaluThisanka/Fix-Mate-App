package com.fixmate.presentation.customer.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fixmate.data.repositories.ImageRepository
import com.fixmate.presentation.auth.AuthStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val imageRepository: ImageRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage

    // New state for provider account status
    private val _hasProviderAccount = MutableStateFlow(false)
    val hasProviderAccount: StateFlow<Boolean> = _hasProviderAccount

    private val _isCheckingProviderAccount = MutableStateFlow(false)
    val isCheckingProviderAccount: StateFlow<Boolean> = _isCheckingProviderAccount

    init {
        loadUserProfile()
        checkProviderAccountStatus()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            auth.currentUser?.let { user ->
                try {
                    val document = firestore.collection("users").document(user.uid).get().await()
                    if (document.exists()) {
                        val userData = document.data
                        if (userData != null) {
                            _userProfile.value = UserProfile(
                                name = userData["displayName"] as? String ?: "Guest User",
                                memberSince = formatDate(userData["createdAt"] as? Long ?: System.currentTimeMillis()),
                                location = (userData["address"] as? Map<*, *>)?.get("city") as? String ?: "Sri Lanka",
                                totalBookings = 0, // Will implement later
                                completedBookings = 0, // Will implement later
                                rating = 0.0, // Will implement later
                                email = userData["email"] as? String ?: "",
                                phoneNumber = userData["phoneNumber"] as? String ?: "",
                                joinDate = formatDate(userData["createdAt"] as? Long ?: System.currentTimeMillis()),
                                profileImageUrl = userData["profileImageUrl"] as? String
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileVM", "Error loading profile", e)
                }
            }
        }
    }

    /**
     * Check if the current customer has a provider account registered
     */
    private fun checkProviderAccountStatus() {
        _isCheckingProviderAccount.value = true
        viewModelScope.launch {
            try {
                val hasProvider = authStateManager.hasProviderAccount()
                _hasProviderAccount.value = hasProvider
                Log.d("CustomerProfileVM", "Provider account status checked: $hasProvider")
            } catch (e: Exception) {
                Log.e("CustomerProfileVM", "Error checking provider account status", e)
                _hasProviderAccount.value = false
            } finally {
                _isCheckingProviderAccount.value = false
            }
        }
    }

    /**
     * Update the customer profile name and phone number
     * NAME is shared across both profiles (same person)
     * Phone number is customer-specific
     * This syncs the name to the provider profile if it exists
     */
    fun updateUserProfile(name: String, phoneNumber: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()

                // Update customer profile in 'users' collection
                // displayName is the shared source of truth for the person's name
                val customerUpdates = mapOf(
                    "displayName" to name,
                    "phoneNumber" to phoneNumber,
                    "updatedAt" to now
                )

                firestore.collection("users").document(currentUser.uid)
                    .update(customerUpdates)
                    .await()

                Log.d("CustomerProfileVM", "Customer profile updated with name: $name")

                // IMPORTANT: Also sync the displayName to provider profile if it exists
                // This ensures the same person's name is reflected in both roles
                try {
                    firestore.collection("service_providers").document(currentUser.uid)
                        .update("displayName", name, "updatedAt", now)
                        .await()
                    Log.d("CustomerProfileVM", "Provider profile synced with new name: $name")
                } catch (e: Exception) {
                    // Provider profile might not exist, which is fine
                    Log.d("CustomerProfileVM", "Provider profile not found or update skipped: ${e.message}")
                }

                loadUserProfile() // Reload profile after update
            } catch (e: Exception) {
                Log.e("CustomerProfileVM", "Error updating customer profile", e)
            }
        }
    }

    /**
     * Switch to provider mode if a provider account exists
     * If no provider account exists, the UI should redirect to provider registration
     */
    fun switchToProvider() {
        viewModelScope.launch {
            try {
                if (authStateManager.hasProviderAccount()) {
                    authStateManager.switchToProvider()
                    Log.d("CustomerProfileVM", "Switched to provider mode")
                } else {
                    // No provider account - UI should handle navigation to provider registration
                    Log.d("CustomerProfileVM", "No provider account - should redirect to registration")
                }
            } catch (e: Exception) {
                Log.e("CustomerProfileVM", "Error switching to provider", e)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _userProfile.value = null
        Log.d("ProfileVM", "User signed out")
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            Log.e("ProfileVM", "Error formatting date", e)
            "Unknown"
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _isUploadingImage.value = true
            try {
                android.util.Log.d("ProfileVMDebug", "Starting uploadProfileImage for user=${currentUser.uid}, uri=$imageUri")
                // Get current profile image URL to delete old image if exists
                val currentProfile = _userProfile.value
                val oldImageUrl = currentProfile?.profileImageUrl
                val result = imageRepository.uploadProfileImage(imageUri, currentUser.uid)
                result.onSuccess { imageUrl ->
                    android.util.Log.d("ProfileVMDebug", "ImageRepository returned imageUrl=$imageUrl")
                    try {
                        // Update Firestore with new profile image URL - ONLY the customer profile
                        firestore.collection("users").document(currentUser.uid)
                            .update("profileImageUrl", imageUrl)
                            .await()
                        android.util.Log.d("ProfileVMDebug", "Firestore updated profileImageUrl for user=${currentUser.uid} with URL=$imageUrl")

                        // Refresh the in-memory profile immediately so the UI updates without waiting for a refetch.
                        _userProfile.value = _userProfile.value?.copy(profileImageUrl = imageUrl)
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileVMDebug", "FAILED to update Firestore profileImageUrl: ${e.message}")
                        throw e
                    }

                    // Delete old image if it exists
                    oldImageUrl?.let { oldUrl ->
                        android.util.Log.d("ProfileVMDebug", "Old profile image exists, oldUrl=$oldUrl")
                        try {
                            val fileName = extractFileNameFromUrl(oldUrl)
                            if (fileName.isNotEmpty()) {
                                android.util.Log.d("ProfileVMDebug", "Deleting old profile image file=$fileName")
                                imageRepository.deleteProfileImage(fileName)
                            }
                        } catch (e: Exception) {
                            Log.w("ProfileVM", "Could not delete old profile image", e)
                        }
                    }

                    Log.d("ProfileVM", "Profile image uploaded successfully: $imageUrl")
                }.onFailure { exception ->
                    Log.e("ProfileVM", "Error uploading profile image", exception)
                    android.util.Log.e("ProfileVMDebug", "uploadProfileImage failed: ${exception.message}", exception)
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error uploading profile image", e)
                android.util.Log.e("ProfileVMDebug", "uploadProfileImage outer exception: ${e.message}", e)
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
}


