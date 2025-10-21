package com.anlarsinsoftware.denecoz.Model.State.Publisher

import android.net.Uri

data class GeneralInfoUiState(
    val examName: String = "",
    val publicationDate: String = "",
    val examType: String = "",
    val coverImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val difficulty: String = "",
    val isFormValid: Boolean = false
)

sealed class GeneralInfoEvent {
    data class OnExamNameChanged(val name: String) : GeneralInfoEvent()
    data class OnPublicationDateSelected(val date: String) : GeneralInfoEvent()
    data class OnExamTypeSelected(val type: String) : GeneralInfoEvent()
    data class OnCoverImageSelected(val uri: Uri?) : GeneralInfoEvent()
    object OnContinueClicked : GeneralInfoEvent()
}

sealed class NavigationEvent {
    data class NavigateToAnswerKey(val examId: String) : NavigationEvent()
}