package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEditorEvent
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEditorNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEditorUiState
import com.anlarsinsoftware.denecoz.Model.State.EditorMode
import com.anlarsinsoftware.denecoz.Model.State.QuestionState
import com.anlarsinsoftware.denecoz.Model.State.SubjectDef
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
class AnswerKeyEditorViewModel @Inject constructor(
    private val repository: ExamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnswerKeyEditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<AnswerKeyEditorNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        val examId = savedStateHandle.get<String>("examId")
        val mode = EditorMode.valueOf(savedStateHandle.get<String>("mode") ?: EditorMode.ANSWER_KEY.name)

        if (examId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Deneme ID'si bulunamadı.") }
        } else {
            _uiState.update { it.copy(examId = examId, mode = mode) }
            loadExamDetails(examId)
        }
    }

    private fun loadExamDetails(examId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getExamDetails(examId)
                .onSuccess { examDetails ->
                    val subjects = examDetails.subjects.map {
                        SubjectDef(
                            name = it["subjectName"] as? String ?: "",
                            totalQuestions = (it["questionCount"] as? Long)?.toInt() ?: 0,
                            topics = emptyList() // TODO: Konuları da curriculum'dan çek
                        )
                    }
                    val totalQuestions = subjects.sumOf { it.totalQuestions }
                    val questions = List(totalQuestions) { QuestionState(index = it + 1) }

                    _uiState.update {
                        it.copy(isLoading = false, subjects = subjects, questions = questions)
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun onEvent(event: AnswerKeyEditorEvent) {
        when (event) {
            is AnswerKeyEditorEvent.OnAnswerSelected -> {
                updateQuestionState(event.questionIndex) { it.copy(selectedAnswerIndex = event.answerIndex) }
            }
            is AnswerKeyEditorEvent.OnTopicSelected -> {
                updateQuestionState(event.questionIndex) { it.copy(selectedTopic = event.topic, isDropdownExpanded = false) }
            }
            is AnswerKeyEditorEvent.OnToggleDropdown -> {
                updateQuestionState(event.questionIndex) { it.copy(isDropdownExpanded = event.isExpanded) }
            }
            is AnswerKeyEditorEvent.OnConfirmClicked -> saveData()
        }
    }

    private fun updateQuestionState(questionIndex: Int, update: (QuestionState) -> QuestionState) {
        _uiState.update { currentState ->
            val updatedQuestions = currentState.questions.toMutableList().also { list ->
                // Soru index'i 1'den başladığı için -1
                list[questionIndex - 1] = update(list[questionIndex - 1])
            }
            currentState.copy(questions = updatedQuestions)
        }
        validateForm()
    }

    private fun validateForm() {
        val state = _uiState.value
        val allAnswered = when(state.mode) {
            EditorMode.ANSWER_KEY -> state.questions.all { it.selectedAnswerIndex != null }
            EditorMode.TOPIC_DISTRIBUTION -> state.questions.all { it.selectedTopic != null }
        }
        _uiState.update { it.copy(isConfirmButtonEnabled = allAnswered) }
    }

    private fun saveData() {
        val state = _uiState.value
        if (!state.isConfirmButtonEnabled || state.isLoading || state.examId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = when (state.mode) {
                EditorMode.ANSWER_KEY -> {
                    val answers = state.questions.associate {
                        it.index.toString() to ('A' + (it.selectedAnswerIndex ?: 0)).toString()
                    }
                    repository.saveAnswerKey(state.examId, "A", answers)
                }
                EditorMode.TOPIC_DISTRIBUTION -> {
                    val topics = state.questions.associate {
                        it.index.toString() to (it.selectedTopic ?: "")
                    }
                    // repository.saveTopicDistribution(...) çağrısı yapılacak
                    Result.success(Unit) // Geçici
                }
            }

            if (result.isSuccess) {
                _navigationEvent.emit(AnswerKeyEditorNavigationEvent.NavigateToPreview(state.examId))
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }
}