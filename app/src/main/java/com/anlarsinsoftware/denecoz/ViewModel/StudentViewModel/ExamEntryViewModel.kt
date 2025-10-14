package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.*
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamEntryEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamEntryNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamEntryUiState
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamEntryViewModel @Inject constructor(
    private val repository: ExamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamEntryUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ExamEntryNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val examId: String = savedStateHandle.get("examId")!!

    init {
        loadExamDetails()
    }

    private fun loadExamDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getExamDetails(examId)
                .onSuccess { details ->
                    // Toplam soru sayısını hesapla ve cevap haritasını başlat
                    val totalQuestions = details.subjects.sumOf { (it["questionCount"] as? Long)?.toInt() ?: 0 }
                    val initialAnswers = (1..totalQuestions).associateWith { null }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            examDetails = details,
                            studentAnswers = initialAnswers
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun onEvent(event: ExamEntryEvent) {
        when (event) {
            is ExamEntryEvent.OnBookletSelected -> {
                _uiState.update { it.copy(selectedBooklet = event.bookletName) }
            }
            is ExamEntryEvent.OnAnswerSelected -> {
                val updatedAnswers = _uiState.value.studentAnswers.toMutableMap()
                val currentAnswer = updatedAnswers[event.questionIndex]
                if (currentAnswer == event.answerIndex) {
                    updatedAnswers[event.questionIndex] = null
                } else {
                    updatedAnswers[event.questionIndex] = event.answerIndex
                }
                _uiState.update { it.copy(studentAnswers = updatedAnswers) }
            }
            is ExamEntryEvent.OnAlternativeSelected -> {
                _uiState.update { it.copy(alternativeChoice = event.choiceId) }
            }
            is ExamEntryEvent.OnSubmitClicked -> {
                checkForUnansweredQuestions()
            }
            is ExamEntryEvent.OnConfirmSubmit -> {
                submitAttempt()
            }
            is ExamEntryEvent.OnDismissDialog -> {
                _uiState.update { it.copy(showConfirmationDialog = false) }
            }
        }
    }

    fun isAlternativeTrigger(testName: String, questionIndexInTest: Int): Boolean {
        // Bu mantığı deneme yapısına göre özelleştirebilirsin
        if (testName == "Sosyal Bilimler" && questionIndexInTest == 15) {
            return true
        }
        if (testName == "Sosyal Bilimler-2" && questionIndexInTest == 34) { // 11+11+12 = 34
            return true
        }
        return false
    }

    private fun checkForUnansweredQuestions() {
        val currentState = _uiState.value
        val unansweredIndices = currentState.studentAnswers.filter { it.value == null }.keys

        // Eğer hiç boş soru yoksa, direkt kaydet ve devam et
        if (unansweredIndices.isEmpty()) {
            submitAttempt()
            return
        }

        // Boş sorular varsa, hangi derse ait olduklarını bulalım
        val summary = mutableMapOf<String, Int>()
        var questionCounter = 0
        currentState.examDetails?.subjects?.forEach { subjectMap ->
            val testName = subjectMap["testName"] as String
            val questionCount = (subjectMap["questionCount"] as Long).toInt()

            val unansweredInThisSubject = unansweredIndices.count {
                it > questionCounter && it <= questionCounter + questionCount
            }

            if (unansweredInThisSubject > 0) {
                summary[testName] = unansweredInThisSubject
            }
            questionCounter += questionCount
        }

        // State'i güncelleyerek UI'a diyalog göstermesini söyle
        _uiState.update {
            it.copy(
                showConfirmationDialog = true,
                unansweredSummary = summary
            )
        }
    }


    private fun submitAttempt() {

        val currentState = _uiState.value
        if (currentState.selectedBooklet == null) {
            _uiState.update { it.copy(errorMessage = "Lütfen bir kitapçık türü seçin.") }
            return
        }
        _uiState.update { it.copy(showConfirmationDialog = false) }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.saveStudentAttempt(
                examId = examId,
                bookletChoice = currentState.selectedBooklet,
                answers = currentState.studentAnswers,
                alternativeChoice = currentState.alternativeChoice
            ).onSuccess { attemptId ->
                _navigationEvent.emit(ExamEntryNavigationEvent.NavigateToResults(examId, attemptId))
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }
}