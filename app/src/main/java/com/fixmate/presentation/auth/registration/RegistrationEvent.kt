package com.fixmate.presentation.auth.registration

import com.fixmate.data.models.UserType

sealed class RegistrationEvent {
    data class FullNameChanged(val fullName: String) : RegistrationEvent()
    data class EmailChanged(val email: String) : RegistrationEvent()
    data class VerificationCodeChanged(val index: Int, val code: String) : RegistrationEvent()
    data class UserTypeChanged(val type: UserType) : RegistrationEvent()
    data class PasswordChanged(val password: String) : RegistrationEvent()
    data class ConfirmPasswordChanged(val password: String) : RegistrationEvent()
    object NextStep : RegistrationEvent()
    object PreviousStep : RegistrationEvent()
    object TogglePasswordVisibility : RegistrationEvent()
    object ToggleConfirmPasswordVisibility : RegistrationEvent()
    object SubmitRegistration : RegistrationEvent()
    data class SubmitServiceProviderRegistration(val onNavigateToServiceSelection: () -> Unit) : RegistrationEvent()
    data class SubmitCustomerRegistration(val onNavigateToHome: () -> Unit) : RegistrationEvent()
    data class GoogleSignIn(val onNavigateToUserTypeSelection: (String, String) -> Unit) : RegistrationEvent()
}