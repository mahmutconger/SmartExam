package com.anlarsinsoftware.denecoz.Model.State.Publisher

import com.anlarsinsoftware.denecoz.View.Common.AuthUiState

data class PublisherLoginUiState(
    override val email: String = "",
    override val password: String = "",
    override val isLoading: Boolean = false,
    val errorMessage: String? = null,
    override val name: String = "" // Bu ekran için kullanılmıyor
) : AuthUiState

// Bu ViewModel'dan çıkabilecek navigasyon komutları
sealed class PublisherLoginNavigationEvent {
    object NavigateToPubHome : PublisherLoginNavigationEvent()
    object NavigateToRegister : PublisherLoginNavigationEvent()
}