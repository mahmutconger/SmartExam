package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Publisher.GeneralInfoEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.GeneralInfoUiState
import com.anlarsinsoftware.denecoz.Model.State.Publisher.NavigationEvent
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
class GeneralInfoViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val auth : FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneralInfoUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val publisherName = auth.currentUser?.displayName
    fun onEvent(event: GeneralInfoEvent) {
        when (event) {
            is GeneralInfoEvent.OnExamNameChanged -> _uiState.update { it.copy(examName = event.name) }
            is GeneralInfoEvent.OnPublicationDateSelected -> _uiState.update { it.copy(publicationDate = event.date) }
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
                    mapOf("testName" to "Türkçe", "questionCount" to 40L), // Tekil test
                    mapOf(
                        "testName" to "Sosyal Bilimler",
                        "questionCount" to 25L,
                        "subSubjects" to listOf(
                            mapOf("subjectId" to "tarih", "name" to "Tarih", "questionCount" to 5L),
                            mapOf("subjectId" to "cografya", "name" to "Coğrafya", "questionCount" to 5L),
                            mapOf("subjectId" to "felsefe", "name" to "Felsefe", "questionCount" to 5L),
                            mapOf("subjectId" to "din_kulturu", "name" to "Din Kültürü", "questionCount" to 5L, "isAlternative" to false),
                            mapOf("subjectId" to "ek_felsefe", "name" to "Ek Felsefe", "questionCount" to 5L, "isAlternative" to true)
                        )
                    ),
                    mapOf("testName" to "Temel Matematik", "questionCount" to 40L), // Birleşik test
                    mapOf("testName" to "Fen Bilimleri", "questionCount" to 20L) // Birleşik test
                )
                "AYT" -> listOf(
                    mapOf("testName" to "Türk Dili ve Edebiyatı-Sosyal Bilimler-1", "questionCount" to 40L,
                        "subSubjects" to listOf(
                            mapOf("subjectId" to "edebiyat", "name" to "Edebiyat", "questionCount" to 24L),
                            mapOf("subjectId" to "tarih_1", "name" to "Tarih-1", "questionCount" to 10L),
                            mapOf("subjectId" to "cografya_1", "name" to "Coğrafya-1", "questionCount" to 6L)
                        )
                    ),
                    mapOf(
                        "testName" to "Sosyal Bilimler-2",
                        "questionCount" to 46L,
                        "subSubjects" to listOf(
                            mapOf("subjectId" to "tarih_2", "name" to "Tarih-2", "questionCount" to 11L),
                            mapOf("subjectId" to "cografya_2", "name" to "Coğrafya-2", "questionCount" to 11L),
                            mapOf("subjectId" to "felsefe_2", "name" to "Felsefe Grubu", "questionCount" to 12L),
                            mapOf("subjectId" to "din_kulturu_2", "name" to "Din Kültürü", "questionCount" to 6L, "isAlternative" to false),
                            mapOf("subjectId" to "ek_felsefe_2", "name" to "Ek Felsefe Grubu", "questionCount" to 6L, "isAlternative" to true)
                        )
                    ),
                    mapOf("testName" to "AYT Matematik", "questionCount" to 40L),
                    mapOf("testName" to "AYT Fen Bilimleri", "questionCount" to 40L)
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
                "examType" to currentState.examType,
                "subjects" to subjectsList,
                "difficulty" to currentState.difficulty,
                "solveCount" to 0L,
                "publisherName" to publisherName.toString()
            )

            examRepository.createDraftExam(
                examDetails = examDetails,
                imageUri = currentState.coverImageUri
            )
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