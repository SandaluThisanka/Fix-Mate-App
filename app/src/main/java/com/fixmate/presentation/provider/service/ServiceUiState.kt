package com.fixmate.presentation.provider.service

import com.fixmate.data.models.ServiceCategory

data class ServiceUiState(
    val serviceCategories: List<ServiceCategory> = emptyList(),
    val searchQuery: String = "",
    val serviceProviderName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isServicesSubmitted: Boolean = false
) {
    val selectedServiceCount: Int
        get() = serviceCategories.sumOf { category -> category.services.count { it.isSelected } }

    val filteredCategories: List<ServiceCategory>
        get() {
            if (searchQuery.isBlank()) {
                return serviceCategories
            }
            return serviceCategories.mapNotNull { category ->
                val filteredServices = category.services.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                }
                if (filteredServices.isNotEmpty()) {
                    category.copy(services = filteredServices)
                } else {
                    null
                }
            }
        }
}
