package com.fixmate.presentation.provider.location

import android.R
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.fixmate.data.models.LocationMethod
import com.fixmate.data.models.ServiceLocation
import com.fixmate.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SetLocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SetLocationUiState())
    val uiState: StateFlow<SetLocationUiState> = _uiState.asStateFlow()
    
    private val _serviceRadius = MutableStateFlow(5f)
    val serviceRadius: StateFlow<Float> = _serviceRadius.asStateFlow()
    
    private var fusedLocationClient: FusedLocationProviderClient? = null
    
    init {
        context.let {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(it)
        }
        initializeLocationMethods()
        // Remove loadCurrentLocation() call from here
    }
    
    private fun initializeLocationMethods() {
        val methods = listOf(
            LocationMethod(
                id = "current_location",
                title = "Use Current Location",
                description = "Get location from GPS",
                iconRes = R.drawable.ic_menu_mylocation,
                isSelected = true
            ),
            LocationMethod(
                id = "select_on_map",
                title = "Select on Map",
                description = "Choose your location manually",
                iconRes = R.drawable.ic_dialog_map
            )
        )
        _uiState.value = _uiState.value.copy(locationMethods = methods)
    }
    
    private fun loadCurrentLocation() {
        // This will be called after permission is granted
        getCurrentLocation()
    }
    
    fun initializeWithPermission() {
        // Called from UI when permissions are granted initially
        getCurrentLocationWithPermission()
    }
    
    fun selectLocationMethod(methodId: String) {
        val updatedMethods = _uiState.value.locationMethods.map { method ->
            method.copy(isSelected = method.id == methodId)
        }
        _uiState.value = _uiState.value.copy(locationMethods = updatedMethods)
        
        if (methodId == "select_on_map") {
            openMapSelection()
        }
        // Remove getCurrentLocation() call from here
    }
    
    fun getCurrentLocationWithPermission() {
        // Update the selected method first
        val updatedMethods = _uiState.value.locationMethods.map { method ->
            method.copy(isSelected = method.id == "current_location")
        }
        _uiState.value = _uiState.value.copy(locationMethods = updatedMethods)
        
        // Then get location
        getCurrentLocation()
    }
    
    fun onLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "Location permission is required to use current location"
        )
    }
    
    private fun getCurrentLocation() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        try {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    _uiState.value = _uiState.value.copy(selectedMapLocation = latLng)
                    
                    // Convert coordinates to address using Geocoder
                    getAddressFromLocation(it.latitude, it.longitude)
                } ?: run {
                    // If last location is null, request fresh location
                    requestFreshLocation()
                }
            }?.addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get current location: ${exception.message}"
                )
            }
        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Location permission not granted"
            )
        }
    }
    
    private fun requestFreshLocation() {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000L
            ).apply {
                setMaxUpdates(1)
                setIntervalMillis(5000L)
            }.build()
            
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        _uiState.value = _uiState.value.copy(selectedMapLocation = latLng)
                        getAddressFromLocation(location.latitude, location.longitude)
                        fusedLocationClient?.removeLocationUpdates(this)
                    } ?: run {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Unable to get current location"
                        )
                    }
                }
                
                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (!locationAvailability.isLocationAvailable) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Location services are not available"
                        )
                    }
                }
            }
            
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Location permission not granted"
            )
        }
    }
    
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                context.let { ctx ->
                    val geocoder = Geocoder(ctx, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val location = ServiceLocation(
                            address = address.getAddressLine(0) ?: "Unknown Address",
                            city = address.locality ?: "Unknown City",
                            province = address.adminArea ?: "Unknown Province",
                            country = address.countryName ?: "Unknown Country",
                            latitude = latitude,
                            longitude = longitude
                        )
                        _uiState.value = _uiState.value.copy(
                            primaryLocation = location,
                            isLoading = false
                        )
                    } else {
                        // Fallback if geocoding fails
                        val location = ServiceLocation(
                            address = "Current Location",
                            city = "Unknown City",
                            province = "Unknown Province",
                            country = "Sri Lanka",
                            latitude = latitude,
                            longitude = longitude
                        )
                        _uiState.value = _uiState.value.copy(
                            primaryLocation = location,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get address from location"
                )
            }
        }
    }
    
    private fun openMapSelection() {
        // Implement map selection navigation
    }
    
    fun updateServiceRadius(radius: Float) {
        _serviceRadius.value = radius
    }
    
    fun changeLocation() {
        _uiState.value = _uiState.value.copy(isChangingLocation = true)
    }
    
    fun setLocationFromMap(latitude: Double, longitude: Double) {
        // Update the selected map location
        _uiState.value = _uiState.value.copy(selectedMapLocation = LatLng(latitude, longitude))
        
        // Convert coordinates to address using Geocoder
        getAddressFromLocation(latitude, longitude)
    }
    
    fun completeSetup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not found. Please log in again."
                    )
                    return@launch
                }
                
                val currentState = _uiState.value
                val serviceLocation = currentState.primaryLocation
                
                if (serviceLocation == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location not selected. Please select a location first."
                    )
                    return@launch
                }
                
                // Save location and radius to Firebase
                val result = authRepository.updateServiceProviderLocation(
                    providerId = userId,
                    serviceLocation = serviceLocation,
                    serviceRadius = _serviceRadius.value.toDouble()
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            setupCompleted = true,
                            error = null
                        )
                        Timber.d("Service provider location saved successfully")
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Failed to save location"
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                        Timber.e(exception, "Failed to save service provider location")
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unexpected error occurred"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                Timber.e(e, "Exception while saving location")
            }
        }
    }
    
    /**
     * Clears any error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SetLocationUiState(
    val locationMethods: List<LocationMethod> = emptyList(),
    val primaryLocation: ServiceLocation? = null,
    val selectedMapLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val isChangingLocation: Boolean = false,
    val setupCompleted: Boolean = false,
    val error: String? = null
)

