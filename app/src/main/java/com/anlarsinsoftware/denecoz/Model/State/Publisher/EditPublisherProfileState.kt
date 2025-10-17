package com.anlarsinsoftware.denecoz.Model.State.Publisher

import android.net.Uri

data class EditPublisherProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val logoUrl: String? = null,
    val newLogoUri: Uri? = null,
    val isSaveButtonEnabled: Boolean = false
)


sealed class EditPublisherProfileEvent {
    data class OnNameChanged(val name: String) : EditPublisherProfileEvent()
    data class OnLogoSelected(val uri: Uri?) : EditPublisherProfileEvent()
    object OnSaveClicked : EditPublisherProfileEvent()
}
sealed class EditPublisherProfileNavigationEvent {
    object NavigateBack : EditPublisherProfileNavigationEvent()
}