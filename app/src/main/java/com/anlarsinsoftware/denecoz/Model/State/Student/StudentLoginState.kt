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



// Bu ViewModel'dan çıkabilecek navigasyon komutları
sealed class StudentLoginNavigationEvent {
    object NavigateToHome : StudentLoginNavigationEvent()
    object NavigateToRegister : StudentLoginNavigationEvent()
}