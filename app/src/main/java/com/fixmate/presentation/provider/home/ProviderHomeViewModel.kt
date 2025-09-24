package com.fixmate.presentation.provider.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingStatus
import com.fixmate.data.models.PaymentStatus
import com.fixmate.data.repositories.AuthRepository
import com.fixmate.data.repositories.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

data class ProviderHomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val upcomingBookings: List<Booking> = emptyList(),
    val todayBookings: Int = 0,
    val thisWeekIncome: Double = 0.0,
    val totalCompleteJobs: Int = 0,
    val providerRating: Double = 0.0
)

@HiltViewModel
class ProviderHomeViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderHomeUiState())
    val uiState: StateFlow<ProviderHomeUiState> = _uiState.asStateFlow()

    init {
        loadProviderData()
    }

    private fun loadProviderData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val providerId = authRepository.getCurrentUserId()
                if (providerId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }

                // Fetch bookings for the provider
                val bookingsResult = bookingRepository.getBookingsByProviderId(providerId)
                
                bookingsResult.fold(
                    onSuccess = { bookings ->
                        Timber.d("Loaded ${bookings.size} bookings for provider: $providerId")
                        
                        // Calculate metrics
                        val upcomingBookings = getUpcomingBookings(bookings)
                        val todayBookings = getTodayBookingsCount(bookings)
                        val thisWeekIncome = getThisWeekIncome(bookings)
                        val totalCompleteJobs = bookings.count { it.status == BookingStatus.COMPLETED }
                        val providerRating = calculateProviderRating(bookings)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            upcomingBookings = upcomingBookings,
                            todayBookings = todayBookings,
                            thisWeekIncome = thisWeekIncome,
                            totalCompleteJobs = totalCompleteJobs,
                            providerRating = providerRating,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to load provider bookings")
                        val errorMessage = when {
                            exception.message?.contains("network", ignoreCase = true) == true -> 
                                "No internet connection. Please check your network and try again."
                            exception.message?.contains("permission", ignoreCase = true) == true -> 
                                "Access denied. Please check your account permissions."
                            exception.message?.contains("not found", ignoreCase = true) == true -> 
                                "Provider data not found. Please contact support."
                            else -> exception.message ?: "Failed to load data"
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading provider data")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    private fun getUpcomingBookings(bookings: List<Booking>): List<Booking> {
        val currentTime = System.currentTimeMillis()
        return bookings.filter { booking ->
            booking.status in listOf(BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.CONFIRMED) &&
            booking.scheduledDate >= currentTime
        }.sortedBy { it.scheduledDate }.take(5) // Show top 5 upcoming bookings
    }

    private fun getTodayBookingsCount(bookings: List<Booking>): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis

        return bookings.count { booking ->
            booking.scheduledDate >= startOfDay && booking.scheduledDate < startOfNextDay
        }
    }

    private fun getThisWeekIncome(bookings: List<Booking>): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val startOfNextWeek = calendar.timeInMillis

        return bookings.filter { booking ->
            booking.pricing.paymentStatus == PaymentStatus.COMPLETED &&
            booking.scheduledDate >= startOfWeek && 
            booking.scheduledDate < startOfNextWeek
        }.sumOf { it.pricing.totalAmount }
    }

    private fun calculateProviderRating(bookings: List<Booking>): Double {
        // For now, return a default rating
        // This could be expanded to include actual customer ratings
        val completedBookings = bookings.count { it.status == BookingStatus.COMPLETED }
        return if (completedBookings > 0) {
            // Simple calculation: start with 4.0 and adjust based on completion rate
            val completionRate = completedBookings.toDouble() / bookings.size
            4.0 + (completionRate * 1.0) // Max 5.0 rating
        } else {
            0.0
        }
    }

    fun refreshData() {
        loadProviderData()
    }
}
