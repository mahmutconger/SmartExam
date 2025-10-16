package com.anlarsinsoftware.denecoz.Model.State.Student

import com.anlarsinsoftware.denecoz.View.Common.AuthUiState

data class StudentRegisterUiState(
    override val name: String = "",
    override val email: String = "",
    override val password: String = "",
    override val isLoading: Boolean = false,
    val errorMessage: String? = null
) : AuthUiState

sealed class StudentRegisterNavigationEvent {
    object NavigateToHome : StudentRegisterNavigationEvent()
    object NavigateToLogin : StudentRegisterNavigationEvent()
}