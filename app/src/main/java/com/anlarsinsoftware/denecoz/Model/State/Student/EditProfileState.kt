package com.anlarsinsoftware.denecoz.Model.State.Student

import android.net.Uri

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val profileImageUrl: String? = null,
    val newProfilePhotoUri: Uri? = null,
    val educationLevel: String = "",
    val city: String = "",
    val district: String = "",
    val school: String = "",
    val educationLevels: List<String> = listOf("Lise", "Mezun", "Ortaokul"),
    val cities: List<String> = listOf("Manisa", "İzmir", "Ankara"), // TODO: Bu veriyi Firestore'dan çek
    val districts: List<String> = emptyList(),
    val schools: List<String> = emptyList()
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