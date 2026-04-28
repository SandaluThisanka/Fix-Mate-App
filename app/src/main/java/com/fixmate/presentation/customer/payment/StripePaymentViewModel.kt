package com.fixmate.presentation.customer.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.BuildConfig
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
            
            Log.d("FIXMATE_PAYMENT", "========== FETCH BOOKING DETAILS ==========")
            Log.d("FIXMATE_PAYMENT", "Booking ID: $bookingId")
            Timber.d("========== FETCH BOOKING DETAILS ==========")
            Timber.d("Booking ID: $bookingId")
            
            try {
                val result = bookingRepository.getBookingById(bookingId)
                
                result.fold(
                    onSuccess = { booking ->
                        if (booking != null) {
                            Timber.d("✅ Booking fetched successfully")
                            Timber.d("  - Customer: ${booking.customerId}")
                            Timber.d("  - Provider: ${booking.providerId}")
                            Timber.d("  - Amount: ${booking.pricing.totalAmount}")
                            
                            _state.value = _state.value.copy(
                                isLoadingBooking = false,
                                booking = booking,
                                amount = booking.pricing.totalAmount,
                                customerId = booking.customerId,
                                providerId = booking.providerId
                            )
                        } else {
                            Timber.e("❌ Booking is null")
                            _state.value = _state.value.copy(
                                isLoadingBooking = false,
                                error = "Booking not found. Please try again."
                            )
                        }
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to fetch booking")
                        _state.value = _state.value.copy(
                            isLoadingBooking = false,
                            error = exception.message ?: "Failed to fetch booking details"
                        )
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "❌ Exception in fetchBookingDetails")
                _state.value = _state.value.copy(
                    isLoadingBooking = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun createPaymentIntent(bookingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            Log.d("FIXMATE_PAYMENT", "========== CREATE PAYMENT INTENT CALLED ==========")
            Log.d("FIXMATE_PAYMENT", "Booking ID: $bookingId")
            
            try {
                val currentState = _state.value
                val booking = currentState.booking
                
                Log.d("FIXMATE_PAYMENT", "Booking null?: ${booking == null}")
                Timber.d("========== CREATE PAYMENT INTENT ==========")
                Timber.d("Booking ID from param: $bookingId")
                Timber.d("Booking object null?: ${booking == null}")
                
                if (booking == null) {
                    Timber.e("❌ Cannot create payment intent: Booking is null")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Booking details not loaded. Please go back and try again."
                    )
                    return@launch
                }
                
                // Validate all required fields
                Timber.d("Booking Details:")
                Timber.d("  - Booking ID: ${booking.id}")
                Timber.d("  - Customer ID: ${booking.customerId}")
                Timber.d("  - Provider ID: ${booking.providerId}")
                Timber.d("  - Total Amount: ${booking.pricing.totalAmount}")
                Timber.d("  - Amount in cents: ${(booking.pricing.totalAmount * 100).toLong()}")
                
                // Check for empty/invalid fields
                val validationErrors = mutableListOf<String>()
                if (bookingId.isBlank()) validationErrors.add("Booking ID is empty")
                if (booking.customerId.isBlank()) validationErrors.add("Customer ID is empty")
                if (booking.providerId.isBlank()) validationErrors.add("Provider ID is empty")
                if (booking.pricing.totalAmount <= 0) validationErrors.add("Amount is zero or negative")
                
                if (validationErrors.isNotEmpty()) {
                    val errorMsg = "Invalid booking data: ${validationErrors.joinToString(", ")}"
                    Log.e("FIXMATE_PAYMENT", "VALIDATION FAILED: $errorMsg")
                    Timber.e("❌ $errorMsg")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    return@launch
                }
                
                Log.d("FIXMATE_PAYMENT", "Validation passed - calling backend")
                
                Timber.d("✅ All fields validated. Creating payment intent...")
                
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
                            publishableKey = response.publishableKey.ifBlank { BuildConfig.STRIPE_PUBLISHABLE_KEY },
                            isPaymentIntentCreated = true
                        )
                        Timber.d("✅ Payment intent created - Ready for payment")
                    },
                    onFailure = { exception ->
                        val userMessage = exception.message ?: "Failed to prepare payment. Please try again."
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = userMessage
                        )
                        Timber.e(exception, "❌ Payment intent creation failed")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = "Payment preparation failed: ${e.message ?: "Unknown error"}\nPlease try again."
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMsg
                )
                Timber.e(e, "❌ Exception in createPaymentIntent ViewModel")
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
