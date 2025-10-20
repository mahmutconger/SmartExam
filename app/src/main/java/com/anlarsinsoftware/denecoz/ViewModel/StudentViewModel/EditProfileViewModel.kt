package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.Province
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileUiState
import com.anlarsinsoftware.denecoz.Model.State.Student.UserProfile
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async // Import async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<EditProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // Store the original profile data for change detection
    private var originalProfileData: UserProfile? = null
    // Store all province data fetched from the API
    private var allProvincesData: List<Province> = emptyList()

    init {
        loadInitialData() // Changed function name for clarity
    }

    // Fetches both profile and location data concurrently
    private fun loadInitialData() {
        val studentId = auth.currentUser?.uid
        if (studentId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Mevcut kullanıcı bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Start both requests in parallel
            val profileDeferred = async { authRepository.getUserProfile(studentId) }
            val provincesDeferred = async { authRepository.getProvincesAndDistricts() } // Assumes this function exists

            val profileResult = profileDeferred.await()
            val provincesResult = provincesDeferred.await()

            var initialProfile: UserProfile? = null
            var cityList: List<String> = emptyList()

            profileResult.onSuccess { profile ->
                initialProfile = profile
                originalProfileData = profile // Store original data for comparison
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                // Optionally return or handle error differently if profile load fails critically
            }

            provincesResult.onSuccess { provinces ->
                allProvincesData = provinces // Store the full data
                cityList = provinces.map { it.name }.sorted() // Get names for the dropdown
            }.onFailure { exception ->
                // Non-critical error, maybe just log it
                Log.e("EditProfileVM", "İl/İlçe verisi çekilemedi: ${exception.message}")
            }

            // Update state with loaded data
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = initialProfile?.name ?: "",
                    profileImageUrl = initialProfile?.profileImageUrl,
                    educationLevel = initialProfile?.educationLevel ?: "",
                    city = initialProfile?.city ?: "",
                    district = initialProfile?.district ?: "",
                    schools = initialProfile?.school ?: "",
                    // Add other profile fields if necessary
                    cities = cityList,
                    // If a city was loaded, load its districts
                    districts = getDistrictsForCity(initialProfile?.city ?: "")
                )
            }
        }
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) }
            is EditProfileEvent.OnPhotoSelected -> _uiState.update { it.copy(newProfilePhotoUri = event.uri) }
            is EditProfileEvent.OnEducationLevelSelected -> _uiState.update { it.copy(educationLevel = event.level) }
            is EditProfileEvent.OnCitySelected -> {
                val districts = getDistrictsForCity(event.city)
                _uiState.update { it.copy(city = event.city, district = "", schools = "", districts = districts) }
            }
            is EditProfileEvent.OnDistrictSelected -> {
                // Currently, we don't have schools from API, just update state
                _uiState.update { it.copy(district = event.district, schools = "") }
                // If/when school loading is added: loadSchoolsForDistrict(uiState.value.city, event.district)
            }
            is EditProfileEvent.OnSchoolSelected -> _uiState.update { it.copy(schools = event.school) } // Assuming manual input or future API
            is EditProfileEvent.OnSaveClicked -> saveProfile()
        }
        // Check for changes after every event except save
        if (event !is EditProfileEvent.OnSaveClicked) {
            checkForChanges()
        }
    }

    // Helper to get districts from the stored full province data
    private fun getDistrictsForCity(cityName: String): List<String> {
        return allProvincesData
            .find { it.name == cityName } // Find the selected Province object
            ?.districts // Get its list of District objects
            ?.map { it.name } // Extract only the names
            ?.sorted() // Sort alphabetically
            ?: emptyList() // Return empty list if city not found
    }

    // Checks if any form field has changed from the original profile data
    private fun checkForChanges() {
        val currentState = _uiState.value
        val original = originalProfileData ?: return // Cannot check if original data isn't loaded

        val hasChanged = currentState.name != original.name ||
                currentState.newProfilePhotoUri != null || // Only check if a NEW photo was selected
                currentState.educationLevel != (original.educationLevel ?: "") ||
                currentState.city != (original.city ?: "") ||
                currentState.district != (original.district ?: "") ||
                currentState.schools != (original.school ?: "")

        _uiState.update { it.copy(isSaveButtonEnabled = hasChanged) }
    }

    private fun saveProfile() {
        val studentId = auth.currentUser?.uid ?: return
        val state = _uiState.value
        if (!state.isSaveButtonEnabled || state.isSaving) return // Prevent saving if no changes or already saving

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) } // Use isSaving state
            authRepository.updateUserProfile(
                studentId = studentId,
                name = state.name,
                educationLevel = state.educationLevel,
                city = state.city,
                district = state.district,
                school = state.schools, // Include school
                newProfilePhotoUri = state.newProfilePhotoUri
            ).onSuccess {
                // Update original data to prevent immediate re-enabling of save button
                originalProfileData = originalProfileData?.copy(
                    name = state.name,
                    educationLevel = state.educationLevel,
                    city = state.city,
                    district = state.district,
                    school = state.schools
                    // Note: profileImageUrl update needs separate handling if needed immediately
                )
                _uiState.update { it.copy(isSaving = false, newProfilePhotoUri = null, isSaveButtonEnabled = false) } // Clear new URI and disable button
                _navigationEvent.emit(EditProfileNavigationEvent.NavigateBack)
            }.onFailure { exception ->
                _uiState.update { it.copy(isSaving = false, errorMessage = exception.message) }
            }
        }
    }

//     Placeholder for future school loading logic
//    private fun loadSchoolsForDistrict(city: String, district: String) {
//        Log.d("EditProfileVM", "$city - $district için okullar yükleniyor... (API yok)")
//        // TODO: Implement school fetching if an API becomes available
//        _uiState.update { it.copy(schools = emptyList()) } // Clear schools for now
//    }
}