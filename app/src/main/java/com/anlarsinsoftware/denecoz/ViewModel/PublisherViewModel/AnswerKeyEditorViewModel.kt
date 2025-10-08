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

    private val aytCompositeTestRules = mapOf(
        "Türk Dili ve Edebiyatı-Sosyal Bilimler-1" to listOf("edebiyat", "tarih", "cografya"),
        "Sosyal Bilimler-2" to listOf("tarih", "cografya", "felsefe", "din_kulturu"),
        "Fen Bilimleri" to listOf("fizik", "kimya", "biyoloji"),
        "Matematik" to listOf("matematik", "geometri"),

        "Temel Matematik" to listOf("matematik", "geometri"),
        "Fen Bilimleri" to listOf("fizik", "kimya", "biyoloji"), // AYT ile aynı anahtarı kullanabiliriz.
        "Sosyal Bilimler" to listOf("tarih", "cografya", "felsefe", "din_kulturu")
    )

    init {
        val examId = savedStateHandle.get<String>("examId")
        val mode = EditorMode.valueOf(savedStateHandle.get<String>("mode") ?: EditorMode.ANSWER_KEY.name)

        if (examId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Deneme ID'si bulunamadı.") }
        } else {
            _uiState.update { it.copy(examId = examId, mode = mode) }
            loadInitialData(examId)
        }
    }

    private fun loadInitialData(examId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val examDetailsResult = repository.getExamDetails(examId)
            val curriculumResult = repository.getCurriculum(examDetailsResult.getOrNull()?.examType ?: "")

            if (examDetailsResult.isSuccess && curriculumResult.isSuccess) {
                val examDetails = examDetailsResult.getOrThrow()
                val curriculumSubjects = curriculumResult.getOrThrow()

                // 1. Denemenin testlerini ('subjects' alanı) al
                val testsInExam = examDetails.subjects.map { it["subjectName"] as? String ?: "" }

                // 2. Bu test listesini, kuralları kullanarak zenginleştirilmiş bir ders listesine dönüştür
                val finalSubjects = testsInExam.map { testName ->
                    val questionCount = (examDetails.subjects.find { it["subjectName"] == testName }
                        ?.get("questionCount") as? Long)?.toInt() ?: 0

                    if (aytCompositeTestRules.containsKey(testName)) {
                        // Bu bir birleşik test (örn: "Sosyal Bilimler-2")
                        val subSubjectIds = aytCompositeTestRules[testName]!!
                        val subSubjects = curriculumSubjects.filter { it.id in subSubjectIds } // id kullandığımızı varsayalım

                        SubjectDef(
                            name = testName,
                            totalQuestions = questionCount,
                            topics = emptyList(),
                            subSubjects = subSubjects // Alt dersleri buraya ekle
                        )
                    } else {
                        // Bu tekil bir test (örn: "Matematik")
                        curriculumSubjects.find { it.name == testName }?.copy(totalQuestions = questionCount)
                            ?: SubjectDef(name = testName, totalQuestions = questionCount, topics = emptyList())
                    }
                }

                val totalQuestions = finalSubjects.sumOf { it.totalQuestions }
                val questions = List(totalQuestions) { QuestionState(index = it + 1) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        subjects = finalSubjects,
                        questions = questions
                    )
                }

            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Veriler yüklenemedi.") }
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
                recalculateAssignedCounts()
            }
            is AnswerKeyEditorEvent.OnSubSubjectSelected -> {
                // Soru state'ini güncelle: seçilen alt dersi kaydet ve konuyu sıfırla.
                updateQuestionState(event.questionIndex) {
                    it.copy(selectedSubSubject = event.subSubject, selectedTopic = null)
                }
            }
            is AnswerKeyEditorEvent.OnSubjectToggled -> {
                _uiState.update { currentState ->
                    val updatedSubjects = currentState.subjects.map { subject ->
                        if (subject.name == event.subjectName) {
                            subject.copy(isExpanded = !subject.isExpanded)
                        } else {
                            subject
                        }
                    }
                    currentState.copy(subjects = updatedSubjects)
                }
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

    private fun recalculateAssignedCounts() {
        _uiState.update { currentState ->
            val updatedSubjects = currentState.subjects.map { subject ->
                val count = currentState.questions.count { q ->
                    subject.topics.contains(q.selectedTopic)
                }
                subject.copy(assignedCount = count)
            }
            currentState.copy(subjects = updatedSubjects)
        }
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