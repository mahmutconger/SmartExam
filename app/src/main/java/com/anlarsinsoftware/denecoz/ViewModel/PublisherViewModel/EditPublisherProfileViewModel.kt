package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditPublisherProfileEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditPublisherProfileNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditPublisherProfileUiState
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
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
class EditPublisherProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val examRepository: ExamRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPublisherProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<EditPublisherProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()
    private var originalName: String = ""

    init {
        loadCurrentPublisherProfile()
    }

    private fun loadCurrentPublisherProfile() {
        val publisherId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            examRepository.getPublisherProfile(publisherId).onSuccess { profile ->
                originalName = profile.name
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = profile.name,
                        logoUrl = profile.logoUrl
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }

    fun onEvent(event: EditPublisherProfileEvent) {
        when (event) {
            is EditPublisherProfileEvent.OnNameChanged -> {
                _uiState.update { it.copy(name = event.name) }
                checkForChanges()
            }
            is EditPublisherProfileEvent.OnLogoSelected -> {
                _uiState.update { it.copy(newLogoUri = event.uri) }
                checkForChanges()
            }
            is EditPublisherProfileEvent.OnSaveClicked -> saveProfile()
        }
    }

    private fun checkForChanges() {
        val currentState = _uiState.value
        val hasChanges = (currentState.name != originalName) || (currentState.newLogoUri != null)
        _uiState.update { it.copy(isSaveButtonEnabled = hasChanges) }
    }

    private fun saveProfile() {
        val publisherId = auth.currentUser?.uid ?: return
        val state = _uiState.value

        if (!state.isSaveButtonEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.updatePublisherProfile(
                publisherId = publisherId,
                name = state.name,
                newLogoUri = state.newLogoUri
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _navigationEvent.emit(EditPublisherProfileNavigationEvent.NavigateBack)
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }
}