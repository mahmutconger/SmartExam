package com.anlarsinsoftware.denecoz.Model.State.Publisher

import com.anlarsinsoftware.denecoz.View.Common.AuthUiState

data class PublisherRegisterUiState(
    override val name: String = "", // Bu alan "Yayınevi Adı"nı tutacak
    override val email: String = "",
    override val password: String = "",
    override val isLoading: Boolean = false,
    val errorMessage: String? = null
) : AuthUiState

// Bu ViewModel'dan çıkabilecek navigasyon komutları
sealed class PublisherRegisterNavigationEvent {
    object NavigateToPubHome : PublisherRegisterNavigationEvent()
    object NavigateToLogin : PublisherRegisterNavigationEvent()
}