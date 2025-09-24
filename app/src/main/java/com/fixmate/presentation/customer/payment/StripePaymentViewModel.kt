package com.fixmate.presentation.customer.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.StripePaymentMethod
import com.fixmate.data.repositories.BookingRepository
import com.fixmate.data.repositories.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StripePaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StripePaymentState())
    val state: StateFlow<StripePaymentState> = _state.asStateFlow()

    fun fetchBookingDetails(bookingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingBooking = true, error = null)
            
            try {
                val result = bookingRepository.getBookingById(bookingId)
                
                result.fold(
                    onSuccess = { booking ->
                        if (booking != null) {
                            _state.value = _state.value.copy(
                                isLoadingBooking = false,
                                booking = booking,
                                amount = booking.pricing.totalAmount,
                                customerId = booking.customerId,
                                providerId = booking.providerId
                            )
                            Timber.d("Booking details fetched successfully for booking: $bookingId")
                        } else {
                            _state.value = _state.value.copy(
                                isLoadingBooking = false,
                                error = "Booking not found"
                            )
                        }
                    },
                    onFailure = { exception ->
                        _state.value = _state.value.copy(
                            isLoadingBooking = false,
                            error = exception.message ?: "Failed to fetch booking details"
                        )
                        Timber.e(exception, "Failed to fetch booking details")
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingBooking = false,
                    error = e.message ?: "Unknown error occurred"
                )
                Timber.e(e, "Error in fetchBookingDetails")
            }
        }
    }

    fun createPaymentIntent(bookingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val currentState = _state.value
                val booking = currentState.booking
                
                if (booking == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Booking details not loaded. Please try again."
                    )
                    return@launch
                }
                
                val result = paymentRepository.createPaymentIntent(
                    bookingId = bookingId,
                    amount = booking.pricing.totalAmount,
                    customerId = booking.customerId,
                    providerId = booking.providerId
                )
                
                result.fold(
                    onSuccess = { response ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            clientSecret = response.clientSecret,
                            paymentIntentId = response.paymentIntentId,
                            publishableKey = response.publishableKey,
                            isPaymentIntentCreated = true
                        )
                        Timber.d("Payment intent created successfully")
                    },
                    onFailure = { exception ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create payment intent"
                        )
                        Timber.e(exception, "Failed to create payment intent")
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
                Timber.e(e, "Error in createPaymentIntent")
            }
        }
    }

    fun confirmStripePayment(bookingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val paymentIntentId = _state.value.paymentIntentId
                if (paymentIntentId.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Payment intent ID is missing"
                    )
                    return@launch
                }
                
                val result = paymentRepository.confirmStripePayment(
                    paymentIntentId = paymentIntentId,
                    bookingId = bookingId
                )
                
                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                paymentSuccess = true,
                                paymentStatus = response.paymentStatus,
                                bookingStatus = response.bookingStatus
                            )
                            Timber.d("Payment confirmed successfully")
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = response.message
                            )
                        }
                    },
                    onFailure = { exception ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to confirm payment"
                        )
                        Timber.e(exception, "Failed to confirm payment")
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
                Timber.e(e, "Error in confirmStripePayment")
            }
        }
    }

    fun processCashPayment(bookingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val currentState = _state.value
                val booking = currentState.booking
                
                if (booking == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Booking details not loaded. Please try again."
                    )
                    return@launch
                }
                
                val result = paymentRepository.processCashPayment(
                    bookingId = bookingId,
                    amount = booking.pricing.totalAmount,
                    customerId = booking.customerId,
                    providerId = booking.providerId
                )
                
                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                paymentSuccess = true,
                                paymentId = response.paymentId,
                                bookingStatus = response.bookingStatus
                            )
                            Timber.d("Cash payment processed successfully")
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = response.message
                            )
                        }
                    },
                    onFailure = { exception ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to process cash payment"
                        )
                        Timber.e(exception, "Failed to process cash payment")
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
                Timber.e(e, "Error in processCashPayment")
            }
        }
    }

    fun setSelectedPaymentMethod(method: StripePaymentMethod) {
        _state.value = _state.value.copy(selectedPaymentMethod = method)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetPaymentState() {
        _state.value = StripePaymentState()
    }
}

data class StripePaymentState(
    val isLoading: Boolean = false,
    val isLoadingBooking: Boolean = false,
    val isPaymentIntentCreated: Boolean = false,
    val clientSecret: String = "",
    val paymentIntentId: String = "",
    val publishableKey: String = "",
    val paymentSuccess: Boolean = false,
    val paymentId: String = "",
    val paymentStatus: String = "",
    val bookingStatus: String = "",
    val selectedPaymentMethod: StripePaymentMethod = StripePaymentMethod.CARD,
    val booking: com.fixmate.data.models.Booking? = null,
    val amount: Double = 0.0,
    val customerId: String = "",
    val providerId: String = "",
    val error: String? = null
)
