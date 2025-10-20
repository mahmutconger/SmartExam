package com.anlarsinsoftware.denecoz.Model.State.Student

import android.net.Uri
import com.anlarsinsoftware.denecoz.Model.Province

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val profileImageUrl: String? = null,
    val newProfilePhotoUri: Uri? = null,
    val educationLevel: String = "",
    val city: String = "",
    val district: String = "",
    val schools: String = "",
    val educationLevels: List<String> = listOf("Lise", "Mezun", "Ortaokul"),
    val allProvincesData: List<Province> = emptyList(),
    val cities: List<String> = emptyList(),
    val districts: List<String> = emptyList(),
    val isSaveButtonEnabled: Boolean = false
)

sealed class EditProfileEvent {
    data class OnNameChanged(val name: String) : EditProfileEvent()
    data class OnPhotoSelected(val uri: Uri?) : EditProfileEvent()
    data class OnEducationLevelSelected(val level: String) : EditProfileEvent()
    data class OnCitySelected(val city: String) : EditProfileEvent()
    data class OnDistrictSelected(val district: String) : EditProfileEvent()
    data class OnSchoolSelected(val school: String) : EditProfileEvent()
    object OnSaveClicked : EditProfileEvent()
}

sealed class EditProfileNavigationEvent {
    object NavigateBack : EditProfileNavigationEvent()
}