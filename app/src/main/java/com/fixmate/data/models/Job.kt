package com.fixmate.data.models

import java.text.SimpleDateFormat
import java.util.*

// Extension functions to convert Booking to Job-like properties for UI compatibility
fun Booking.toJobTitle(): String = serviceName.ifEmpty { "Service Request" }
fun Booking.toJobDescription(): String = description.ifEmpty { specialInstructions }
fun Booking.toJobDate(): String {
    return if (scheduledDate > 0) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(scheduledDate))
    } else ""
}
fun Booking.toJobTime(): String = scheduledTime
fun Booking.toJobDistance(): String = "N/A" // Can be calculated if needed
fun Booking.toJobTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - createdAt
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)
    
    return when {
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days < 7 -> "$days days ago"
        else -> "Over a week ago"
    }
}

// Convert BookingStatus to JobStatus for backward compatibility
fun BookingStatus.toJobStatus(): JobStatus = when (this) {
    BookingStatus.PENDING -> JobStatus.NEW
    BookingStatus.ACCEPTED, BookingStatus.CONFIRMED -> JobStatus.ACCEPTED
    BookingStatus.COMPLETED -> JobStatus.DONE
    BookingStatus.IN_PROGRESS -> JobStatus.UNPAID
    BookingStatus.REJECTED, BookingStatus.CANCELLED, BookingStatus.DISPUTED, BookingStatus.REFUNDED -> JobStatus.NEW
}

// Convert JobStatus to BookingStatus
fun JobStatus.toBookingStatus(): BookingStatus = when (this) {
    JobStatus.NEW -> BookingStatus.PENDING
    JobStatus.ACCEPTED -> BookingStatus.ACCEPTED
    JobStatus.DONE -> BookingStatus.COMPLETED
    JobStatus.UNPAID -> BookingStatus.IN_PROGRESS
}

enum class JobStatus {
    NEW, ACCEPTED, DONE, UNPAID
}

data class JobsState(
    val todaysEarnings: String = "LKR 0",
    val jobsToday: Int = 0,
    val currentFilter: JobStatus = JobStatus.NEW,
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false
)
