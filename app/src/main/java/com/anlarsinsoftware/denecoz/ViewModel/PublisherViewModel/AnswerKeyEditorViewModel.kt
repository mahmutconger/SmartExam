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

    private val compositeTestRules = mapOf(
        "Sosyal Bilimler" to listOf(
            Pair("tarih", 5), Pair("cografya", 5), Pair("felsefe", 5), Pair("din_kulturu", 5)
        ),
        "Temel Matematik" to listOf(
            Pair("matematik", 35), Pair("geometri", 5) // Tahmini
        ),
        "Fen Bilimleri" to listOf(
            Pair("fizik", 7), Pair("kimya", 7), Pair("biyoloji", 6)
        ),
        "Türk Dili ve Edebiyatı-Sosyal Bilimler-1" to listOf(
            Pair("edebiyat", 24), Pair("tarih", 10), Pair("cografya", 6)
        ),
        "Sosyal Bilimler-2" to listOf(
            Pair("tarih", 11), Pair("cografya", 11), Pair("felsefe", 12), Pair("din_kulturu", 6)
        ),
        "AYT Matematik" to listOf(
            Pair("matematik", 35), Pair("geometri", 5) // Tahmini
        ),
        "AYT Fen Bilimleri" to listOf(
            Pair("fizik", 14), Pair("kimya", 13), Pair("biyoloji", 13)
        )
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val examDetailsResult = repository.getExamDetails(examId)
            val curriculumResult = repository.getCurriculum(examDetailsResult.getOrNull()?.examType ?: "")

            if (examDetailsResult.isSuccess && curriculumResult.isSuccess) {
                val examDetails = examDetailsResult.getOrThrow()
                val allCurriculumSubjects = curriculumResult.getOrThrow()

                val finalSubjectsForUI = mutableListOf<SubjectDef>()
                val questionsForUI = mutableListOf<QuestionState>()
                var questionCounter = 0

                (examDetails.subjects as? List<Map<String, Any>>)?.forEach { testMap ->
                    val testName = testMap["testName"] as? String ?: ""
                    val totalQuestionCount = (testMap["questionCount"] as? Long)?.toInt() ?: 0

                    if (compositeTestRules.containsKey(testName)) {
                        val rules = compositeTestRules[testName]!!
                        val subSubjectsForUI = rules.mapNotNull { (subjectId, _) ->
                            allCurriculumSubjects.find { it.id == subjectId }
                        }

                        finalSubjectsForUI.add(SubjectDef(name = testName, totalQuestions = totalQuestionCount, topics = emptyList(), subSubjects = subSubjectsForUI))

                        // --- TODO ÇÖZÜLDÜ: Soruları alt derslere göre otomatik ata ---
                        rules.forEach { (subjectId, questionCount) ->
                            val subjectName = allCurriculumSubjects.find { it.id == subjectId }?.name ?: ""
                            for (i in 1..questionCount) {
                                questionsForUI.add(QuestionState(index = questionCounter + i, assignedSubjectName = subjectName))
                            }
                            questionCounter += questionCount
                        }
                    } else {
                        // Bu tekil bir test (örn: Türkçe)
                        allCurriculumSubjects.find { it.name == testName }?.let { subjectDef ->
                            finalSubjectsForUI.add(subjectDef.copy(totalQuestions = totalQuestionCount))
                            for (i in 1..totalQuestionCount) {
                                questionsForUI.add(QuestionState(index = questionCounter + i, assignedSubjectName = testName))
                            }
                            questionCounter += totalQuestionCount
                        }
                    }
                }
                _uiState.update { it.copy(isLoading = false, subjects = finalSubjectsForUI, questions = questionsForUI) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Veriler yüklenemedi.") }
            }
        }
    }

    fun getTopicsForSubject(subjectName: String?): List<String> {
        if (subjectName == null) return emptyList()

        // uiState'deki tüm ders listesini gezerek doğru konu listesini bul ve döndür.
        _uiState.value.subjects.forEach { test ->
            if (test.subSubjects != null) { // Bu bir birleşik test
                test.subSubjects.find { it.name == subjectName }?.let { return it.topics }
            } else { // Bu tekil bir test
                if (test.name == subjectName) return test.topics
            }
        }
        return emptyList()
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

    /*
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
     */

    private fun recalculateAssignedCounts() {
        _uiState.update { currentState ->
            val updatedSubjects = currentState.subjects.map { subject ->
                val count = if (subject.subSubjects != null) {
                    // Bu bir birleşik test ise, tüm alt derslerin konularını içeren tek bir liste oluştur
                    val allTopicsInCompositeTest = subject.subSubjects.flatMap { it.topics }
                    currentState.questions.count { q ->
                        allTopicsInCompositeTest.contains(q.selectedTopic)
                    }
                } else {
                    // Bu tekil bir test
                    currentState.questions.count { q ->
                        subject.topics.contains(q.selectedTopic)
                    }
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