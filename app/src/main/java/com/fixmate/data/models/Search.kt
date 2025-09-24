package com.fixmate.data.models

data class SearchFilters(
    val query: String = "",
    val category: String = "",
    val location: LocationFilter? = null,
    val priceRange: PriceRange? = null,
    val rating: Float = 0.0f, // Minimum rating
    val availability: AvailabilityFilter? = null,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val isVerifiedOnly: Boolean = false,
    val maxDistance: Double = 50.0 // in kilometers
)

data class LocationFilter(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 10.0, // in kilometers
    val city: String = "",
    val area: String = ""
)

data class PriceRange(
    val min: Double = 0.0,
    val max: Double = Double.MAX_VALUE
)

data class AvailabilityFilter(
    val date: Long? = null,
    val timeSlot: TimeSlot? = null,
    val isEmergency: Boolean = false
)

data class TimeSlot(
    val startTime: String = "", // HH:mm format
    val endTime: String = ""
)

data class SearchResult(
    val providers: List<ServiceProvider> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val suggestions: List<String> = emptyList(),
    val filters: SearchFilters = SearchFilters()
)

data class SearchHistory(
    val id: String = "",
    val userId: String = "",
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val resultCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SortOption {
    RELEVANCE,
    RATING_HIGH_TO_LOW,
    RATING_LOW_TO_HIGH,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    DISTANCE,
    MOST_REVIEWED,
    NEWEST
}
