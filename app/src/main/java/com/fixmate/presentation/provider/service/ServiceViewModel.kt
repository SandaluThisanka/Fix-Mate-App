package com.fixmate.presentation.provider.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.PricingModel
import com.fixmate.data.models.Service
import com.fixmate.data.models.ServiceCategory
import com.fixmate.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Toggles the selection state of a service.
     * If a service is selected, its price input field becomes visible.
     */
    fun onServiceSelected(categoryId: String, serviceId: Int, isSelected: Boolean) {
        _uiState.update { currentState ->
            val updatedCategories = currentState.serviceCategories.map { category ->
                if (category.name == categoryId) {
                    category.copy(services = category.services.map { service ->
                        if (service.id == serviceId) {
                            service.copy(isSelected = isSelected, isExpanded = isSelected)
                        } else {
                            service
                        }
                    })
                } else {
                    category
                }
            }
            currentState.copy(serviceCategories = updatedCategories)
        }
    }

    /**
     * Updates the price for a given service.
     */
    fun onPriceChanged(categoryId: String, serviceId: Int, newPrice: String) {
        // Allow only numeric input for price
        if (newPrice.all { it.isDigit() }) {
            _uiState.update { currentState ->
                val updatedCategories = currentState.serviceCategories.map { category ->
                    if (category.name == categoryId) {
                        category.copy(services = category.services.map { service ->
                            if (service.id == serviceId) {
                                service.copy(price = newPrice)
                            } else {
                                service
                            }
                        })
                    } else {
                        category
                    }
                }
                currentState.copy(serviceCategories = updatedCategories)
            }
        }
    }

    /**
     * Toggles the expanded/collapsed state of a service category.
     */
    fun onCategoryToggled(categoryId: String) {
        _uiState.update { currentState ->
            val updatedCategories = currentState.serviceCategories.map {
                if (it.name == categoryId) it.copy(isExpanded = !it.isExpanded) else it
            }
            currentState.copy(serviceCategories = updatedCategories)
        }
    }

    /**
     * Updates the search query.
     * This triggers a filter on the displayed services.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Updates the service provider name.
     */
    fun onProviderNameChanged(name: String) {
        _uiState.update { it.copy(serviceProviderName = name) }
    }

    /**
     * Saves the selected services to Firebase for the current service provider.
     */
    fun saveSelectedServices(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val currentState = _uiState.value
        val selectedServices = getSelectedServices()
        
        // Validation
        if (selectedServices.isEmpty()) {
            onError("Please select at least one service")
            return
        }
        
        // Check if all selected services have prices
        val servicesWithoutPrice = selectedServices.filter { it.price.isBlank() }
        if (servicesWithoutPrice.isNotEmpty()) {
            onError("Please enter prices for all selected services")
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "User not found. Please log in again."
                        ) 
                    }
                    onError("User not found. Please log in again.")
                    return@launch
                }
                
                // Create services list with only selected services and their prices
                val servicesToSave = selectedServices.map { service ->
                    service.copy(
                        isSelected = false, // Reset selection state for storage
                        isExpanded = false  // Reset expanded state for storage
                    )
                }
                
                val result = authRepository.updateServiceProviderServices(
                    providerId = userId,
                    services = servicesToSave
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isServicesSubmitted = true,
                                error = null
                            ) 
                        }
                        Timber.d("Services saved successfully")
                        onSuccess()
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Failed to save services"
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = errorMessage
                            ) 
                        }
                        Timber.e(exception, "Failed to save services")
                        onError(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unexpected error occurred"
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = errorMessage
                    ) 
                }
                Timber.e(e, "Exception while saving services")
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Gets all selected services with their prices.
     */
    fun getSelectedServices(): List<Service> {
        return _uiState.value.serviceCategories.flatMap { category ->
            category.services.filter { it.isSelected }
        }
    }
    
    /**
     * Clears any error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Loads the initial list of services and categories.
     * In a real app, this would come from a repository (network/database).
     */
    private fun loadInitialData() {
        val initialCategories = listOf(
            ServiceCategory(
                name = "Mechanical",
                services = listOf(
                    Service(1, "General Service", "Routine vehicle servicing and maintenance.", PricingModel.FIXED),
                    Service(2, "Engine Repair", "Engine diagnosis and repair.", PricingModel.FIXED),
                    Service(3, "Gearbox & Clutch", "Transmission and clutch repair.", PricingModel.FIXED),
                    Service(4, "Fuel System Repair", "Fuel pump and injector repair.", PricingModel.FIXED)
                )
            ),
            ServiceCategory(
                name = "Electrical",
                services = listOf(
                    Service(5, "Auto Electrical Repair", "Electrical system fault repairs.", PricingModel.FIXED),
                    Service(6, "Battery Replacement", "Battery testing and replacement.", PricingModel.FIXED),
                    Service(7, "ECU Scanning", "Computer-based diagnostics for vehicle faults.", PricingModel.FIXED),
                    Service(8, "Lighting & Wiring", "Lighting and wiring issue repairs.", PricingModel.FIXED)
                )
            ),
            ServiceCategory(
                name = "Suspension",
                services = listOf(
                    Service(9, "Suspension Repair", "Suspension system inspections and repairs.", PricingModel.FIXED),
                    Service(10, "Shock Absorbers", "Shock absorber replacement services.", PricingModel.FIXED),
                    Service(11, "Steering Repair", "Power steering maintenance and repairs.", PricingModel.FIXED),
                    Service(12, "Wheel Alignment", "Wheel alignment and balancing services.", PricingModel.FIXED)
                )
            ),
            ServiceCategory(
                name = "Brakes",
                services = listOf(
                    Service(13, "Brake Repair", "Brake system inspection and repairs.", PricingModel.FIXED),
                    Service(14, "ABS Repair", "ABS system diagnostics and repairs.", PricingModel.FIXED),
                    Service(15, "Brake Pads & Discs", "Brake pad and disc replacement.", PricingModel.FIXED)
                )
            ),
            ServiceCategory(
                name = "Body & Care",
                services = listOf(
                    Service(16, "Body Painting", "Vehicle body painting and touch-ups.", PricingModel.FIXED),
                    Service(17, "Denting & Welding", "Accident dent repair and welding.", PricingModel.FIXED),
                    Service(18, "Interior Cleaning", "Interior detailing and cleaning.", PricingModel.FIXED),
                    Service(19, "Full Detailing", "Complete vehicle detailing service.", PricingModel.FIXED)
                )
            ),
            ServiceCategory(
                name = "Tires",
                services = listOf(
                    Service(20, "Tire Replacement", "New tire fitting and rotation.", PricingModel.FIXED),
                    Service(21, "Puncture Repair", "Tire puncture repairs.", PricingModel.FIXED),
                    Service(22, "Wheel Balancing", "Precision wheel balancing service.", PricingModel.FIXED)
                )
            ),
            ServiceCategory(
                name = "Emergency",
                services = listOf(
                    Service(23, "Towing Service", "Vehicle towing assistance.", PricingModel.FIXED),
                    Service(24, "Jump Start", "On-site battery jump start.", PricingModel.FIXED),
                    Service(25, "Roadside Assistance", "Emergency roadside assistance.", PricingModel.FIXED),
                    Service(26, "Mobile Mechanic", "Mechanic dispatched to your location.", PricingModel.HOURLY)
                )
            )
        )
        _uiState.value = ServiceUiState(serviceCategories = initialCategories)
    }
}