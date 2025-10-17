package com.anlarsinsoftware.denecoz.Model.State.Publisher

data class PublisherProfileUiState(
    val isLoading: Boolean = true,
    val profile: PublisherProfile? = null,
    val errorMessage: String? = null,
    val showLogoutDialog: Boolean = false
)

sealed class PublisherProfileEvent {
    object OnEditProfileClicked : PublisherProfileEvent()
    object OnLogoutClicked : PublisherProfileEvent()
    object OnConfirmLogout : PublisherProfileEvent()
    object OnDismissLogoutDialog : PublisherProfileEvent()
    object OnRefresh : PublisherProfileEvent()
}

sealed class PublisherProfileNavigationEvent {
    object NavigateToEditProfile : PublisherProfileNavigationEvent()
    object NavigateToWelcome : PublisherProfileNavigationEvent()
}