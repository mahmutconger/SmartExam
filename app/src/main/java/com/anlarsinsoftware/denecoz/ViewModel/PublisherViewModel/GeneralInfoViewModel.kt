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
        val currentState = _uiState.value
        if (!currentState.isFormValid || currentState.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val subjectsList = when (currentState.examType) {
                "TYT" -> listOf(
                    mapOf("subjectName" to "Türkçe", "questionCount" to 40L),
                    mapOf("subjectName" to "Sosyal Bilimler", "questionCount" to 20L),
                    mapOf("subjectName" to "Temel Matematik", "questionCount" to 40L),
                    mapOf("subjectName" to "Fen Bilimleri", "questionCount" to 20L)
                )
                "AYT" -> listOf(
                    mapOf("subjectName" to "Türk Dili ve Edebiyatı-Sosyal Bilimler-1", "questionCount" to 40L),
                    mapOf("subjectName" to "Sosyal Bilimler-2", "questionCount" to 40L),
                    mapOf("subjectName" to "Matematik", "questionCount" to 40L),
                    mapOf("subjectName" to "Fen Bilimleri", "questionCount" to 40L)
                    // TODO AYT yapısı daha karmaşık olabilir (örn: Yabancı Dil), bu şimdilik bir başlangıç.
                )
                "LGS" -> {
                    // TODO: LGS için ders yapısı eklenecek
                    emptyList()
                }
                else -> emptyList()
            }

            val examDetails = mapOf(
                "name" to currentState.examName,
                "publicationDate" to currentState.publicationDate,
                "bookletType" to currentState.bookletType,
                "examType" to currentState.examType,
                "subjects" to subjectsList
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