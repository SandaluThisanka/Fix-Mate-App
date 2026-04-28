package com.fixmate.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.fixmate.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * State representing the account switching flow
 */
data class AccountSwitchingState(
    val isLoading: Boolean = false,
    val currentMode: CurrentMode = CurrentMode.CUSTOMER,
    val hasProviderAccount: Boolean = false,
    val hasCustomerAccount: Boolean = true,
    val canSwitchToProvider: Boolean = false,
    val canSwitchToCustomer: Boolean = false,
    val error: String? = null,
    val shouldRedirectToProviderRegistration: Boolean = false
)

enum class CurrentMode {
    CUSTOMER, PROVIDER
}

sealed class AccountSwitchingEvent {
    object CheckAccountStatus : AccountSwitchingEvent()
    object SwitchToProvider : AccountSwitchingEvent()
    object SwitchToCustomer : AccountSwitchingEvent()
    object InitiateProviderRegistration : AccountSwitchingEvent()
    object ClearError : AccountSwitchingEvent()
}

@HiltViewModel
class AccountSwitchingViewModel @Inject constructor(
    private val authStateManager: AuthStateManager,
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSwitchingState())
    val uiState: StateFlow<AccountSwitchingState> = _uiState.asStateFlow()

    init {
        checkAccountStatus()
    }

    fun onEvent(event: AccountSwitchingEvent) {
        when (event) {
            AccountSwitchingEvent.CheckAccountStatus -> checkAccountStatus()
            AccountSwitchingEvent.SwitchToProvider -> switchToProvider()
            AccountSwitchingEvent.SwitchToCustomer -> switchToCustomer()
            AccountSwitchingEvent.InitiateProviderRegistration -> initiateProviderRegistration()
            AccountSwitchingEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    /**
     * Check the current account status and populate the UI state
     */
    private fun checkAccountStatus() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val hasProvider = authStateManager.hasProviderAccount()
                val hasCustomer = authStateManager.hasCustomerAccount()
                val isCurrentlyProvider = authStateManager.isProviderMode

                val currentMode = if (isCurrentlyProvider) CurrentMode.PROVIDER else CurrentMode.CUSTOMER
                val canSwitchToProvider = hasProvider || !isCurrentlyProvider
                val canSwitchToCustomer = isCurrentlyProvider

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentMode = currentMode,
                        hasProviderAccount = hasProvider,
                        hasCustomerAccount = hasCustomer,
                        canSwitchToProvider = canSwitchToProvider,
                        canSwitchToCustomer = canSwitchToCustomer,
                        error = null
                    )
                }

                Timber.d(
                    "Account status checked - Mode: $currentMode, HasProvider: $hasProvider, " +
                    "HasCustomer: $hasCustomer, CanSwitchToProvider: $canSwitchToProvider"
                )
            } catch (e: Exception) {
                Timber.e(e, "Error checking account status")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to check account status: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Switch to provider mode
     * If the user doesn't have a provider account, they'll be redirected to registration
     */
    private fun switchToProvider() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val hasProvider = authStateManager.hasProviderAccount()

                if (hasProvider) {
                    // Provider account exists, switch to it
                    authStateManager.switchToProvider()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentMode = CurrentMode.PROVIDER,
                            canSwitchToCustomer = true,
                            error = null
                        )
                    }
                    Timber.d("Switched to provider mode")
                } else {
                    // No provider account, redirect to registration
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            shouldRedirectToProviderRegistration = true,
                            error = null
                        )
                    }
                    Timber.d("Redirecting to provider registration as account doesn't exist")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error switching to provider")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to switch to provider mode: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Switch to customer mode
     * Only works if the user is currently in provider mode
     */
    private fun switchToCustomer() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                if (authStateManager.isProviderMode) {
                    authStateManager.switchToCustomer()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentMode = CurrentMode.CUSTOMER,
                            canSwitchToCustomer = false,
                            error = null
                        )
                    }
                    Timber.d("Switched to customer mode")
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Already in customer mode"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error switching to customer")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to switch to customer mode: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Initiate provider registration flow
     * This is called when a customer wants to become a provider
     */
    private fun initiateProviderRegistration() {
        _uiState.update {
            it.copy(
                isLoading = false,
                shouldRedirectToProviderRegistration = true
            )
        }
        Timber.d("Provider registration initiated for current customer user")
    }

    /**
     * Reset the redirect state after navigation has been handled
     */
    fun clearRedirectState() {
        _uiState.update {
            it.copy(shouldRedirectToProviderRegistration = false)
        }
    }
}
