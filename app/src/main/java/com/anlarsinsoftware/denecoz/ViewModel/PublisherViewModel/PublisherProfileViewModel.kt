package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherProfileEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherProfileNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherProfileUiState
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
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
class PublisherProfileViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublisherProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PublisherProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        loadProfile()
    }

    /**
     * View katmanından gelen tüm kullanıcı eylemlerini yönetir.
     */
    fun onEvent(event: PublisherProfileEvent) {
        when (event) {
            is PublisherProfileEvent.OnEditProfileClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(PublisherProfileNavigationEvent.NavigateToEditProfile)
                }
            }
            is PublisherProfileEvent.OnLogoutClicked -> {
                _uiState.update { it.copy(showLogoutDialog = true) }
            }
            is PublisherProfileEvent.OnConfirmLogout -> {
                logout()
            }
            is PublisherProfileEvent.OnDismissLogoutDialog -> {
                _uiState.update { it.copy(showLogoutDialog = false) }
            }
            is PublisherProfileEvent.OnRefresh -> {
                loadProfile()
            }
        }
    }

    /**
     * Yayıncının profil bilgilerini Firestore'dan çeker.
     */
    private fun loadProfile() {
        val publisherId = auth.currentUser?.uid
        if (publisherId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Oturum bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            examRepository.getPublisherProfile(publisherId)
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoading = false, profile = profile) }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    /**
     * Kullanıcının oturumunu kapatır ve navigasyon olayını tetikler.
     */
    private fun logout() {
        auth.signOut()
        viewModelScope.launch {
            _uiState.update { it.copy(showLogoutDialog = false) }
            _navigationEvent.emit(PublisherProfileNavigationEvent.NavigateToWelcome)
        }
    }
}