package com.fixmate.data.models

data class Booking(
    val id: String = "",
    val customerId: String = "",
    val providerId: String = "",
    val providerName: String = "", 
    val customerName: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val description: String = "",
    val serviceLocation: ServiceLocation = ServiceLocation(),
    val scheduledDate: Long = 0L,
    val scheduledTime: String = "",
    val estimatedDuration: String = "",
    val pricing: BookingPricing = BookingPricing(),
    val status: BookingStatus = BookingStatus.PENDING,
    val priority: BookingPriority = BookingPriority.NORMAL,
    val specialInstructions: String = "",
    val attachments: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String = "",
    val timeline: List<BookingTimelineEvent> = emptyList(),
    val serviceLatitude: Double? = null,
    val serviceLongitude: Double? = null,
    val serviceAddress: String = ""
)

data class BookingPricing(
    val basePrice: Double = 0.0,
    val additionalCharges: List<AdditionalCharge> = emptyList(),
    val discount: Double = 0.0,
    val travelFee: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING
)

data class AdditionalCharge(
    val description: String = "",
    val amount: Double = 0.0,
    val isApproved: Boolean = false
)

data class BookingTimelineEvent(
    val id: String = "",
    val event: BookingEvent = BookingEvent.CREATED,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val performedBy: String = "" // User ID
)

enum class BookingStatus {
    PENDING,           // Waiting for provider response
    ACCEPTED,          // Provider accepted
    REJECTED,          // Provider rejected
    CONFIRMED,         // Customer confirmed after acceptance
    IN_PROGRESS,       // Work started
    COMPLETED,         // Work finished
    CANCELLED,         // Cancelled by customer or provider
    DISPUTED,          // Issue raised
    REFUNDED           // Payment refunded
}

enum class BookingPriority {
    LOW, NORMAL, HIGH, URGENT
}

enum class BookingEvent {
    CREATED, ACCEPTED, REJECTED, CONFIRMED, STARTED, PAUSED, RESUMED, COMPLETED, CANCELLED, DISPUTED, REFUNDED
}
