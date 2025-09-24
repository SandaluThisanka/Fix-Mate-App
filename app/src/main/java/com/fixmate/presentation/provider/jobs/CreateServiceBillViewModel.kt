package com.fixmate.presentation.provider.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.AdditionalCharge
import com.fixmate.data.models.BillAdditionalCost
import com.fixmate.data.models.BillServiceItem
import com.fixmate.data.models.Booking
import com.fixmate.data.models.BookingPricing
import com.fixmate.data.models.BookingStatus
import com.fixmate.data.models.PricingModel
import com.fixmate.data.repositories.AuthRepository
import com.fixmate.data.repositories.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateServiceBillViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateServiceBillUiState())
    val uiState: StateFlow<CreateServiceBillUiState> = _uiState.asStateFlow()

    fun loadProviderServices(booking: Booking) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = authRepository.getServiceProviderServices(booking.providerId)
                
                result.fold(
                    onSuccess = { services ->
                        // Filter to only show the service that matches the booking's serviceId or serviceName
                        // This ensures we only bill for the specific service that was booked
                        val matchingService = services.find { service -> 
                            service.id.toString() == booking.serviceId || 
                            service.name.equals(booking.serviceName, ignoreCase = true)
                        }
                        
                        val billServiceItems = if (matchingService != null) {
                            val billItem = BillServiceItem(
                                serviceId = matchingService.id,
                                serviceName = matchingService.name,
                                pricingModel = matchingService.pricingModel,
                                basePrice = matchingService.price,
                                quantity = if (matchingService.pricingModel == PricingModel.FIXED) "1" else ""
                            )
                            // Calculate amount immediately for fixed pricing
                            listOf(billItem.copy(calculatedAmount = billItem.calculateAmount()))
                        } else {
                            // If no matching service found, create a service item from booking info
                            // This handles cases where the service might have been updated or removed
                            val fallbackBillItem = BillServiceItem(
                                serviceId = booking.serviceId.toIntOrNull() ?: 0,
                                serviceName = booking.serviceName,
                                pricingModel = PricingModel.HOURLY, // Default to hourly if unknown
                                basePrice = "0",
                                quantity = ""
                            )
                            listOf(fallbackBillItem)
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                serviceItems = billServiceItems,
                                booking = booking
                            ) 
                        }
                        // Calculate initial totals for fixed price services
                        calculateTotals()
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = exception.message ?: "Failed to load services"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }

    fun updateServiceItemQuantity(serviceId: Int, quantity: String) {
        _uiState.update { currentState ->
            val updatedServiceItems = currentState.serviceItems.map { item ->
                if (item.serviceId == serviceId) {
                    val updatedItem = item.copy(quantity = quantity)
                    updatedItem.copy(calculatedAmount = updatedItem.calculateAmount())
                } else {
                    item
                }
            }
            currentState.copy(serviceItems = updatedServiceItems)
        }
        calculateTotals()
    }

    fun addAdditionalCost(name: String, amount: Double) {
        if (name.isBlank() || amount <= 0) return
        
        _uiState.update { currentState ->
            val newCost = BillAdditionalCost(name = name, amount = amount)
            currentState.copy(additionalCosts = currentState.additionalCosts + newCost)
        }
        calculateTotals()
    }

    fun removeAdditionalCost(cost: BillAdditionalCost) {
        _uiState.update { currentState ->
            currentState.copy(additionalCosts = currentState.additionalCosts - cost)
        }
        calculateTotals()
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    private fun calculateTotals() {
        _uiState.update { currentState ->
            val serviceSubtotal = currentState.serviceItems.sumOf { it.calculatedAmount }
            val additionalSubtotal = currentState.additionalCosts.sumOf { it.amount }
            val subtotal = serviceSubtotal + additionalSubtotal
            val platformFee = subtotal * 0.02 // 2% platform fee
            val total = subtotal + platformFee
            
            currentState.copy(
                subtotal = subtotal,
                platformFee = platformFee,
                total = total
            )
        }
    }

    fun confirmBill(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentState = _uiState.value
        
        // Validation
        val serviceItemsWithQuantity = currentState.serviceItems.filter { item ->
            // For fixed pricing, quantity is always "1" and we just need a price
            // For other pricing models, we need a valid quantity > 0
            when (item.pricingModel) {
                PricingModel.FIXED -> item.basePrice.toDoubleOrNull() != null && item.basePrice.toDoubleOrNull()!! > 0
                else -> item.quantity.isNotBlank() && item.quantity.toDoubleOrNull() != null && item.quantity.toDoubleOrNull()!! > 0
            }
        }
        
        if (serviceItemsWithQuantity.isEmpty()) {
            onError("Please add quantities for at least one service or ensure fixed price services have valid prices")
            return
        }
        
        _uiState.update { it.copy(isCreatingBill = true, error = null) }
        
        viewModelScope.launch {
            try {
                val booking = currentState.booking
                if (booking == null) {
                    _uiState.update { it.copy(isCreatingBill = false, error = "Booking not found") }
                    onError("Booking not found")
                    return@launch
                }
                
                // Calculate service base price (sum of all service calculations)
                val serviceBasePrice = serviceItemsWithQuantity.sumOf { it.calculatedAmount }
                
                // Convert BillAdditionalCost to AdditionalCharge
                val additionalCharges = currentState.additionalCosts.map { cost ->
                    AdditionalCharge(
                        description = cost.name,
                        amount = cost.amount,
                        isApproved = true // Auto-approve provider's charges
                    )
                }
                
                // Create updated pricing
                val updatedPricing = BookingPricing(
                    basePrice = serviceBasePrice,
                    additionalCharges = additionalCharges,
                    discount = 0.0,
                    travelFee = 0.0,
                    tax = currentState.platformFee, // Using platform fee as tax
                    totalAmount = currentState.total,
                    paidAmount = 0.0,
                    paymentStatus = com.fixmate.data.models.PaymentStatus.PENDING
                )
                
                // Update booking pricing
                val pricingUpdateResult = bookingRepository.updateBookingPricing(
                    bookingId = booking.id,
                    pricing = updatedPricing
                )
                
                pricingUpdateResult.fold(
                    onSuccess = {
                        // Update booking status to COMPLETED
                        val statusUpdateResult = bookingRepository.updateBookingStatus(
                            bookingId = booking.id,
                            status = BookingStatus.IN_PROGRESS
                        )
                        
                        statusUpdateResult.fold(
                            onSuccess = {
                                _uiState.update { 
                                    it.copy(
                                        isCreatingBill = false,
                                        billCreated = true
                                    ) 
                                }
                                Timber.d("Bill created successfully for booking: ${booking.id}")
                                onSuccess()
                            },
                            onFailure = { exception ->
                                _uiState.update { 
                                    it.copy(
                                        isCreatingBill = false, 
                                        error = "Pricing updated but failed to update booking status: ${exception.message}"
                                    ) 
                                }
                                onError("Pricing updated but failed to update booking status")
                            }
                        )
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Failed to update booking pricing"
                        _uiState.update { 
                            it.copy(
                                isCreatingBill = false, 
                                error = errorMessage
                            ) 
                        }
                        onError(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unexpected error occurred"
                _uiState.update { 
                    it.copy(
                        isCreatingBill = false, 
                        error = errorMessage
                    ) 
                }
                onError(errorMessage)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CreateServiceBillUiState(
    val isLoading: Boolean = false,
    val isCreatingBill: Boolean = false,
    val booking: Booking? = null,
    val serviceItems: List<BillServiceItem> = emptyList(),
    val additionalCosts: List<BillAdditionalCost> = emptyList(),
    val notes: String = "",
    val subtotal: Double = 0.0,
    val platformFee: Double = 0.0,
    val total: Double = 0.0,
    val billCreated: Boolean = false,
    val error: String? = null
)
