package com.fixmate.presentation.auth.google

import com.fixmate.data.models.UserType

data class UserTypeSelectionState(
    val selectedUserType: UserType = UserType.CUSTOMER,
    val isLoading: Boolean = false,
    val error: String? = null
)
