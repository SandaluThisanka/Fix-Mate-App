package com.fixmate.presentation.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ServiceProviderCheckState(
    val isLoading: Boolean = false,
    val hasServiceProviderAccount: Boolean? = null,
    val showJoinDialog: Boolean = false,
    val error: String? = null,
    val serviceProviderCreated: Boolean = false,
    val shouldNavigateToProvider: Boolean = false
)

@HiltViewModel
class ServiceProviderCheckViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ServiceProviderCheckState())
    val uiState: StateFlow<ServiceProviderCheckState> = _uiState.asStateFlow()
    
    fun checkServiceProviderAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }
                
                val result = authRepository.checkServiceProviderExists(userId)
                result.fold(
                    onSuccess = { exists ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasServiceProviderAccount = exists,
                            showJoinDialog = !exists,
                            shouldNavigateToProvider = exists,
                            error = null
                        )
                        Timber.d("Service provider check completed. Exists: $exists")
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to check service provider account"
                        )
                        Timber.e(exception, "Failed to check service provider account")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
                Timber.e(e, "Unexpected error during service provider check")
            }
        }
    }
    
    fun createServiceProviderAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }
                
                val result = authRepository.createServiceProviderFromCustomer(userId)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasServiceProviderAccount = true,
                            showJoinDialog = false,
                            serviceProviderCreated = true,
                            error = null
                        )
                        Timber.d("Service provider account created successfully")
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create service provider account"
                        )
                        Timber.e(exception, "Failed to create service provider account")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
                Timber.e(e, "Unexpected error during service provider creation")
            }
        }
    }
    
    fun dismissJoinDialog() {
        _uiState.value = _uiState.value.copy(showJoinDialog = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearNavigationFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateToProvider = false)
    }
    
    fun resetState() {
        _uiState.value = ServiceProviderCheckState()
    }
}
