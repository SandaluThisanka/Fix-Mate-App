package com.fixmate.presentation.auth.google

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.models.UserType
import com.fixmate.data.repositories.AuthRepository
import com.fixmate.presentation.auth.AuthStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserTypeSelectionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserTypeSelectionState())
    val uiState: StateFlow<UserTypeSelectionState> = _uiState.asStateFlow()
    
    fun onEvent(event: UserTypeSelectionEvent) {
        when (event) {
            is UserTypeSelectionEvent.UserTypeChanged -> {
                _uiState.update { 
                    it.copy(selectedUserType = event.userType, error = null) 
                }
            }
            is UserTypeSelectionEvent.CreateAccount -> {
                createGoogleAccount(
                    email = event.email,
                    fullName = event.fullName,
                    userType = _uiState.value.selectedUserType,
                    onNavigateToServiceSelection = event.onNavigateToServiceSelection,
                    onNavigateToHome = event.onNavigateToHome
                )
            }
        }
    }
    
    private fun createGoogleAccount(
        email: String,
        fullName: String,
        userType: UserType,
        onNavigateToServiceSelection: () -> Unit,
        onNavigateToHome: () -> Unit
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                if (userType == UserType.SERVICE_PROVIDER) {
                    val result = authRepository.createGoogleServiceProvider(
                        email = email,
                        fullName = fullName,
                        userType = userType
                    )
                    
                    result.fold(
                        onSuccess = { 
                            _uiState.update { it.copy(isLoading = false, error = null) }
                            authStateManager.handleSuccessfulAuthentication() // <-- Mark onboarding completed
                            onNavigateToServiceSelection()
                        },
                        onFailure = { exception ->
                            Timber.e(exception, "Google service provider registration failed")
                            _uiState.update { 
                                it.copy(
                                    error = "Registration failed: ${exception.message ?: "Unknown error"}",
                                    isLoading = false
                                )
                            }
                        }
                    )
                } else {
                    val result = authRepository.createGoogleUser(
                        email = email,
                        fullName = fullName,
                        userType = userType
                    )
                    
                    result.fold(
                        onSuccess = { 
                            _uiState.update { it.copy(isLoading = false, error = null) }
                            authStateManager.handleSuccessfulAuthentication() // <-- Mark onboarding completed
                            onNavigateToHome()
                        },
                        onFailure = { exception ->
                            Timber.e(exception, "Google user registration failed")
                            _uiState.update { 
                                it.copy(
                                    error = "Registration failed: ${exception.message ?: "Unknown error"}",
                                    isLoading = false
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Google account creation failed")
                _uiState.update { 
                    it.copy(
                        error = "Registration failed: ${e.message ?: "Unknown error"}",
                        isLoading = false
                    )
                }
            }
        }
    }
}
