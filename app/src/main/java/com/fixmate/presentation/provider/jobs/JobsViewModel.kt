package com.fixmate.presentation.provider.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingStatus
import com.fixmate.data.models.JobStatus
import com.fixmate.data.models.JobsState
import com.fixmate.data.models.toJobStatus
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
class JobsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JobsState())
    val state: StateFlow<JobsState> = _state.asStateFlow()

    init {
        loadBookings()
    }

    fun onFilterChanged(status: JobStatus) {
        _state.value = _state.value.copy(currentFilter = status)
        loadBookingsForFilter(status)
    }
    
    fun refreshBookings() {
        loadBookings()
    }

    fun onAcceptBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                // Update booking status in database
                val result = bookingRepository.updateBookingStatus(bookingId, BookingStatus.ACCEPTED)
                
                result.fold(
                    onSuccess = {
                        // Update local state
                        val updatedBookings = _state.value.bookings.map { booking ->
                            if (booking.id == bookingId) {
                                booking.copy(status = BookingStatus.ACCEPTED)
                            } else {
                                booking
                            }
                        }
                        _state.value = _state.value.copy(bookings = updatedBookings)
                        
                        // Reload bookings to get fresh data
                        loadBookings()
                        
                        Timber.d("Booking $bookingId accepted successfully")
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to accept booking $bookingId")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error accepting booking $bookingId")
            }
        }
    }
    
    fun onDeclineBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                // Update booking status in database
                val result = bookingRepository.updateBookingStatus(bookingId, BookingStatus.REJECTED)
                
                result.fold(
                    onSuccess = {
                        // Remove from current bookings list
                        val updatedBookings = _state.value.bookings.filter { it.id != bookingId }
                        _state.value = _state.value.copy(bookings = updatedBookings)
                        
                        Timber.d("Booking $bookingId declined successfully")
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to decline booking $bookingId")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error declining booking $bookingId")
            }
        }
    }

    private fun loadBookings() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Get current user ID
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId == null) {
                    Timber.e("No current user found")
                    _state.value = _state.value.copy(isLoading = false)
                    return@launch
                }
                
                // Fetch bookings for the current provider
                val result = bookingRepository.getBookingsByProviderId(currentUserId)
                
                result.fold(
                    onSuccess = { bookings ->
                        val filteredBookings = filterBookingsByCurrentFilter(bookings)
                        val earnings = calculateTodaysEarnings(bookings)
                        val jobsToday = countJobsToday(bookings)
                        
                        _state.value = _state.value.copy(
                            bookings = filteredBookings,
                            todaysEarnings = "LKR ${earnings.toInt()}",
                            jobsToday = jobsToday,
                            isLoading = false
                        )
                        
                        Timber.d("Loaded ${bookings.size} bookings for provider $currentUserId")
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to load bookings")
                        // For now, show some sample data when there's an error
                        loadSampleData()
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading bookings")
                // For now, show some sample data when there's an error
                loadSampleData()
            }
        }
    }
    
    private fun loadSampleData() {
        // Sample data for testing - remove this in production
        val sampleBookings = listOf(
            Booking(
                id = "sample_1",
                customerName = "Sarah Johnson",
                serviceName = "Kitchen Plumbing Repair",
                description = "Kitchen sink is leaking from the pipes underneath. Need urgent repair.",
                scheduledDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Tomorrow
                scheduledTime = "10:00 AM",
                status = BookingStatus.PENDING,
                createdAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000), // 2 hours ago
                pricing = com.fixmate.data.models.BookingPricing(
                    basePrice = 2500.0,
                    totalAmount = 2500.0
                )
            ),
            Booking(
                id = "sample_2",
                customerName = "Mike Chen",
                serviceName = "Bathroom Pipe Installation",
                description = "New bathroom renovation - need pipes connected for shower and sink.",
                scheduledDate = System.currentTimeMillis() + (48 * 60 * 60 * 1000), // Day after tomorrow
                scheduledTime = "11:00 AM",
                status = BookingStatus.ACCEPTED,
                createdAt = System.currentTimeMillis() - (3 * 60 * 60 * 1000), // 3 hours ago
                pricing = com.fixmate.data.models.BookingPricing(
                    basePrice = 4500.0,
                    totalAmount = 4500.0
                )
            ),
            Booking(
                id = "sample_3",
                customerName = "Lisa Wong",
                serviceName = "Garden Cleaning",
                description = "Monthly garden maintenance and cleaning service required.",
                scheduledDate = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // Yesterday
                scheduledTime = "9:00 AM",
                status = BookingStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                completedAt = System.currentTimeMillis() - (23 * 60 * 60 * 1000), // Yesterday
                pricing = com.fixmate.data.models.BookingPricing(
                    basePrice = 3000.0,
                    totalAmount = 3000.0
                )
            ),
            Booking(
                id = "sample_4",
                customerName = "John Doe",
                serviceName = "Electrical Wiring",
                description = "Need to fix electrical wiring in the living room.",
                scheduledDate = System.currentTimeMillis(), // Today
                scheduledTime = "2:00 PM",
                status = BookingStatus.IN_PROGRESS,
                createdAt = System.currentTimeMillis() - (4 * 60 * 60 * 1000), // 4 hours ago
                pricing = com.fixmate.data.models.BookingPricing(
                    basePrice = 3500.0,
                    totalAmount = 3500.0
                )
            )
        )
        
        val filteredBookings = filterBookingsByCurrentFilter(sampleBookings)
        val earnings = calculateTodaysEarnings(sampleBookings)
        val jobsToday = countJobsToday(sampleBookings)
        
        _state.value = _state.value.copy(
            bookings = filteredBookings,
            todaysEarnings = "LKR ${earnings.toInt()}",
            jobsToday = jobsToday,
            isLoading = false
        )
    }

    private fun loadBookingsForFilter(status: JobStatus) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId == null) {
                    Timber.e("No current user found")
                    return@launch
                }
                
                val result = bookingRepository.getBookingsByProviderId(currentUserId)
                
                result.fold(
                    onSuccess = { allBookings ->
                        val filteredBookings = allBookings.filter { booking ->
                            booking.status.toJobStatus() == status
                        }
                        _state.value = _state.value.copy(bookings = filteredBookings)
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to load filtered bookings")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading filtered bookings")
            }
        }
    }
    
    private fun filterBookingsByCurrentFilter(bookings: List<Booking>): List<Booking> {
        return bookings.filter { booking ->
            booking.status.toJobStatus() == _state.value.currentFilter
        }
    }
    
    private fun calculateTodaysEarnings(bookings: List<Booking>): Double {
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % (24 * 60 * 60 * 1000))
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000)
        
        return bookings
            .filter { booking ->
                booking.status == BookingStatus.COMPLETED &&
                booking.completedAt != null &&
                booking.completedAt >= startOfDay &&
                booking.completedAt < endOfDay
            }
            .sumOf { it.pricing.totalAmount }
    }
    
    private fun countJobsToday(bookings: List<Booking>): Int {
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % (24 * 60 * 60 * 1000))
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000)
        
        return bookings.count { booking ->
            booking.scheduledDate >= startOfDay && booking.scheduledDate < endOfDay
        }
    }
}