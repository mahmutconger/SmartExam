package com.anlarsinsoftware.denecoz.Model.State.Student

import com.anlarsinsoftware.denecoz.View.Common.AuthUiState

// Ortak AuthUiState arayüzünü uyguluyoruz.
// Giriş ekranında 'name' alanı olmadığı için onu boş bırakıyoruz.
data class StudentLoginUiState(
    override val email: String = "",
    override val password: String = "",
    override val isLoading: Boolean = false,
    val errorMessage: String? = null,
    override val name: String = ""
) : AuthUiState

data class PublisherLoginUiState(
    override val email: String = "",
    override val password: String = "",
    override val isLoading: Boolean = false,
    val errorMessage: String? = null,
    override val name: String = ""
) : AuthUiState

interface AuthUiState {
    val name: String
    val email: String
    val password: String
    val isLoading: Boolean
}


// Bu ViewModel'dan çıkabilecek navigasyon komutları
sealed class StudentLoginNavigationEvent {
    object NavigateToHome : StudentLoginNavigationEvent()
    object NavigateToRegister : StudentLoginNavigationEvent()
}