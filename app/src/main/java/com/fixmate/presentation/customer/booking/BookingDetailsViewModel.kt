package com.fixmate.presentation.customer.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.Booking
import com.fixmate.data.repositories.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookingDetailsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookingDetailsState())
    val state: StateFlow<BookingDetailsState> = _state.asStateFlow()

    fun loadBookingDetails(bookingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val bookingResult = bookingRepository.getBookingById(bookingId)
                
                bookingResult.fold(
                    onSuccess = { booking ->
                        if (booking != null) {
                            val providerResult = bookingRepository.getProviderById(booking.providerId)
                            
                            providerResult.fold(
                                onSuccess = { providerData ->
                                    _state.value = _state.value.copy(
                                        booking = booking,
                                        providerDetails = ProviderDetails(
                                            name = booking.providerName,
                                            serviceType = booking.serviceName,
                                            rating = (providerData?.get("rating") as? Double)?.toFloat() ?: 0f,
                                            reviewCount = (providerData?.get("totalReviews") as? Long)?.toInt() ?: 0,
                                            phoneNumber = providerData?.get("phoneNumber") as? String ?: ""
                                        ),
                                        isLoading = false,
                                        error = null
                                    )
                                },
                                onFailure = { exception ->
                                    _state.value = _state.value.copy(
                                        booking = booking,
                                        isLoading = false,
                                        error = "Failed to load provider details"
                                    )
                                    Timber.e(exception, "Error loading provider details")
                                }
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Booking not found"
                            )
                        }
                    },
                    onFailure = { exception ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Failed to load booking details"
                        )
                        Timber.e(exception, "Error loading booking")
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
                Timber.e(e, "Error in loadBookingDetails")
            }
        }
    }
}

data class ProviderDetails(
    val name: String,
    val serviceType: String,
    val rating: Float,
    val reviewCount: Int,
    val phoneNumber: String
)

data class BookingDetailsState(
    val booking: Booking? = null,
    val providerDetails: ProviderDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
