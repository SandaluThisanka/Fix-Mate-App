package com.fixmate.data.models

data class Review(
    val id: String = "",
    val bookingId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerImageUrl: String = "",
    val providerId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val rating: Float = 0.0f,
    val comment: String = "",
    val images: List<String> = emptyList(),
    val aspectRatings: AspectRatings = AspectRatings(),
    val isVerified: Boolean = false, // Based on completed booking
    val isAnonymous: Boolean = false,
    val helpfulVotes: Int = 0,
    val reportCount: Int = 0,
    val response: ProviderResponse? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: ReviewStatus = ReviewStatus.ACTIVE
)

data class AspectRatings(
    val quality: Float = 0.0f,          // Quality of work
    val timeliness: Float = 0.0f,       // On-time arrival/completion
    val communication: Float = 0.0f,     // Responsiveness and clarity
    val professionalism: Float = 0.0f,   // Behavior and presentation
    val valueForMoney: Float = 0.0f      // Price vs quality ratio
)

data class ProviderResponse(
    val message: String = "",
    val respondedAt: Long = System.currentTimeMillis()
)

data class ReviewSummary(
    val providerId: String = "",
    val totalReviews: Int = 0,
    val averageRating: Float = 0.0f,
    val ratingDistribution: RatingDistribution = RatingDistribution(),
    val aspectAverages: AspectRatings = AspectRatings(),
    val recentReviews: List<Review> = emptyList()
)

data class RatingDistribution(
    val oneStar: Int = 0,
    val twoStar: Int = 0,
    val threeStar: Int = 0,
    val fourStar: Int = 0,
    val fiveStar: Int = 0
)

enum class ReviewStatus {
    ACTIVE, HIDDEN, REPORTED, DELETED
}
