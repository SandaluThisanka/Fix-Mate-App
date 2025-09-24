package com.fixmate.presentation.customer.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingStatus
import com.fixmate.data.repositories.AuthRepository
import com.fixmate.data.repositories.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    init {
        loadBookings()
    }
    
    fun loadBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val result = bookingRepository.getBookingsByCustomerId(currentUserId)
                    result.fold(
                        onSuccess = { bookingsList ->
                            _bookings.value = bookingsList
                            Timber.d("Loaded ${bookingsList.size} bookings for user: $currentUserId")
                        },
                        onFailure = { exception ->
                            _error.value = exception.message ?: "Failed to load bookings"
                            Timber.e(exception, "Failed to load bookings")
                        }
                    )
                } else {
                    _error.value = "User not found. Please log in again."
                    Timber.w("Current user ID is null")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
                Timber.e(e, "Exception while loading bookings")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
    
    fun getFilteredBookings(): List<Booking> {
        val allBookings = _bookings.value
        return when (_selectedFilter.value) {
            "Pending" -> allBookings.filter { it.status == BookingStatus.PENDING }
            "Accepted" -> allBookings.filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.CONFIRMED }
            "Unpaid" -> allBookings.filter { it.status == BookingStatus.COMPLETED && it.pricing.paymentStatus.name == "PENDING" }
            "Completed" -> allBookings.filter { it.status == BookingStatus.COMPLETED }
            else -> allBookings
        }
    }
    
    fun refreshBookings() {
        loadBookings()
    }
    
    fun clearError() {
        _error.value = null
    }
}
