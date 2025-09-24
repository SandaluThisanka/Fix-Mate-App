package com.fixmate.presentation.auth.registration

import com.fixmate.data.models.UserType

data class RegistrationState (
    val currentStep: Int = 1,
    val fullName: String = "",
    val email: String = "",
    val verificationCode: List<String> = List(6) { "" },
    val userType: UserType = UserType.CUSTOMER,
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)