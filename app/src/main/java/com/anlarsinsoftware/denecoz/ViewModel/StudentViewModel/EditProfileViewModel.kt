package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileUiState
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        val studentId = auth.currentUser?.uid
        if (studentId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Mevcut kullanıcı bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getUserProfile(studentId).onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = profile.name,
                        profileImageUrl = profile.profileImageUrl,
                        educationLevel = profile.educationLevel ?: "",
                        city = profile.city ?: "",
                        district = profile.district ?: "",
                        school = profile.school ?: ""
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }
    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) }
            is EditProfileEvent.OnPhotoSelected -> _uiState.update { it.copy(newProfilePhotoUri = event.uri) }
            is EditProfileEvent.OnEducationLevelSelected -> _uiState.update { it.copy(educationLevel = event.level) }
            is EditProfileEvent.OnCitySelected -> {
                _uiState.update { it.copy(city = event.city, district = "", school = "") } // Şehir değişince alt seçimleri sıfırla
                loadDistrictsForCity(event.city)
            }
            is EditProfileEvent.OnDistrictSelected -> {
                _uiState.update { it.copy(district = event.district, school = "") } // İlçe değişince okul seçimini sıfırla
                loadSchoolsForDistrict(uiState.value.city, event.district)
            }
            is EditProfileEvent.OnSchoolSelected -> _uiState.update { it.copy(school = event.school) }
            is EditProfileEvent.OnSaveClicked -> saveProfile()
        }
    }

    private fun loadDistrictsForCity(city: String) {
        // TODO: Repository'den veya yerel bir kaynaktan bu şehre ait ilçeleri çekip
        // _uiState.update { it.copy(districts = ilceListesi) } ile state'i güncelle.
        Log.d("EditProfileVM", "$city için ilçeler yükleniyor...")
    }

    private fun loadSchoolsForDistrict(city: String, district: String) {
        // TODO: Repository'den veya yerel bir kaynaktan bu ilçeye ait okulları çekip
        // _uiState.update { it.copy(schools = okulListesi) } ile state'i güncelle.
        Log.d("EditProfileVM", "$city - $district için okullar yükleniyor...")
    }

    private fun saveProfile() {
        val studentId = auth.currentUser?.uid ?: return
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.updateUserProfile(
                studentId = studentId,
                name = state.name,
                educationLevel = state.educationLevel,
                city = state.city,
                district = state.district,
                school = state.school,
                newProfilePhotoUri = state.newProfilePhotoUri
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                // Başarılı, bir önceki ekrana (ProfileScreen) geri gitme komutu gönder
                _navigationEvent.emit(EditProfileNavigationEvent.NavigateBack)
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }
}