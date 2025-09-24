package com.fixmate.presentation.customer.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingPricing
import com.fixmate.data.models.BookingStatus
import com.fixmate.data.models.ServiceLocation
import com.fixmate.data.models.ServiceProvider
import com.fixmate.data.repositories.AuthRepository
import com.fixmate.data.repositories.BookingRepository
import com.fixmate.data.repositories.ServiceProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val serviceProviderRepository: ServiceProviderRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _serviceProvider = MutableStateFlow<ServiceProvider?>(null)
    val serviceProvider: StateFlow<ServiceProvider?> = _serviceProvider.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _bookingCreated = MutableStateFlow<String?>(null)
    val bookingCreated: StateFlow<String?> = _bookingCreated.asStateFlow()
    
    fun loadServiceProvider(providerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Timber.d("Loading provider with ID: $providerId")
                val provider = serviceProviderRepository.getServiceProviderById(providerId)
                if (provider != null) {
                    _serviceProvider.value = provider
                    Timber.d("Provider loaded successfully: ${provider.businessName}")
                } else {
                    _error.value = "Provider not found"
                    Timber.w("Provider not found for ID: $providerId")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load provider details"
                Timber.e(e, "Error loading provider $providerId")
                _serviceProvider.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createBooking(
        selectedService: String,
        bookingTitle: String,
        description: String,
        selectedDate: String,
        selectedTime: String,
        latitude: Double?,
        longitude: Double?,
        address: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Validate inputs
                if (bookingTitle.isBlank()) {
                    onError("Please enter a booking title")
                    return@launch
                }
                
                if (description.isBlank()) {
                    onError("Please enter a description")
                    return@launch
                }
                
                if (selectedDate.isBlank()) {
                    onError("Please select a date")
                    return@launch
                }
                
                if (selectedTime.isBlank()) {
                    onError("Please select a time")
                    return@launch
                }
                
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId == null) {
                    onError("User not found. Please log in again.")
                    return@launch
                }
                
                val provider = _serviceProvider.value
                if (provider == null) {
                    onError("Provider information not available")
                    return@launch
                }
                
                val selectedServiceObj = provider.services.find { it.name == selectedService }
                if (selectedServiceObj == null) {
                    onError("Selected service not found")
                    return@launch
                }
                
                // Parse date string to timestamp
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val scheduledDate = try {
                    dateFormat.parse(selectedDate)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing date: $selectedDate")
                    System.currentTimeMillis()
                }

                val customerName = authRepository.getCustomerName() ?: "Unknown Customer"
                
                // Create booking object
                val booking = Booking(
                    customerId = currentUserId,
                    providerId = provider.id,
                    providerName = provider.businessName, // Add provider business name
                    serviceId = selectedServiceObj.id.toString(),
                    customerName = customerName,
                    serviceName = selectedService,
                    description = description,
                    serviceLocation = provider.serviceLocation ?: ServiceLocation(),
                    scheduledDate = scheduledDate,
                    scheduledTime = selectedTime,
                    estimatedDuration = "TBD",
                    pricing = BookingPricing(
                        basePrice = selectedServiceObj.price?.toDoubleOrNull() ?: 0.0,
                        totalAmount = selectedServiceObj.price?.toDoubleOrNull() ?: 0.0
                    ),
                    status = BookingStatus.PENDING,
                    specialInstructions = bookingTitle,
                    // Add location data
                    serviceLatitude = latitude,
                    serviceLongitude = longitude,
                    serviceAddress = address
                )
                
                // Save booking to database
                val result = bookingRepository.createBooking(booking)
                
                result.fold(
                    onSuccess = { bookingId ->
                        _bookingCreated.value = bookingId
                        Timber.d("Booking created successfully with ID: $bookingId")
                        onSuccess(bookingId)
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Failed to create booking"
                        _error.value = errorMessage
                        Timber.e(exception, "Failed to create booking")
                        onError(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unexpected error occurred"
                _error.value = errorMessage
                Timber.e(e, "Exception while creating booking")
                onError(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearBookingCreated() {
        _bookingCreated.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
}
