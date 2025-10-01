package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.GeneralInfoEvent
import com.anlarsinsoftware.denecoz.Model.State.GeneralInfoUiState
import com.anlarsinsoftware.denecoz.Model.State.NavigationEvent
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralInfoViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneralInfoUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: GeneralInfoEvent) {
        when (event) {
            is GeneralInfoEvent.OnExamNameChanged -> _uiState.update { it.copy(examName = event.name) }
            is GeneralInfoEvent.OnPublicationDateSelected -> _uiState.update { it.copy(publicationDate = event.date) }
            is GeneralInfoEvent.OnBookletTypeSelected -> _uiState.update { it.copy(bookletType = event.type) }
            is GeneralInfoEvent.OnExamTypeSelected -> _uiState.update { it.copy(examType = event.type) }
            is GeneralInfoEvent.OnCoverImageSelected -> _uiState.update { it.copy(coverImageUri = event.uri) }
            is GeneralInfoEvent.OnContinueClicked -> createDraftExam()
        }
        validateForm()
    }

    private fun validateForm() {
        val currentState = _uiState.value
        val isValid = currentState.examName.isNotBlank() &&
                currentState.publicationDate.isNotBlank() &&
                currentState.bookletType.isNotBlank() &&
                currentState.examType.isNotBlank()
        _uiState.update { it.copy(isFormValid = isValid) }
    }

    private fun createDraftExam() {
        if (!_uiState.value.isFormValid || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val examDetails = mapOf(
                "name" to _uiState.value.examName,
                "publicationDate" to _uiState.value.publicationDate,
                "bookletType" to _uiState.value.bookletType,
                "examType" to _uiState.value.examType
                // Image URI'yi burada göndermiyoruz, onu ayrı bir adımda yükleyeceğiz.
            )

            examRepository.createDraftExam(examDetails)
                .onSuccess { newExamId ->
                    _uiState.update { it.copy(isLoading = false) }
                    _navigationEvent.emit(NavigationEvent.NavigateToAnswerKey(newExamId))
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }
}