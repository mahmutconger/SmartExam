package com.anlarsinsoftware.denecoz.Model.State

data class QuestionState(
    val index: Int,
    val assignedSubjectName: String? = null,
    val selectedAnswerIndex: Int? = null,
    val selectedSubSubject: String? = null,
    val selectedTopic: String? = null,
    val isDropdownExpanded: Boolean = false
)

data class SubjectDef(
    val id: String = "",
    val name: String,
    val totalQuestions: Int,
    val topics: List<String>,
    val isExpanded: Boolean = false,
    val assignedCount: Int = 0,
    val subSubjects: List<SubjectDef>? = null
)

data class AnswerKeyEditorUiState(
    val isLoading: Boolean = true,
    val examId: String? = null,
    val mode: EditorMode = EditorMode.ANSWER_KEY,
    val subjects: List<SubjectDef> = emptyList(),
    val questions: List<QuestionState> = emptyList(),
    val errorMessage: String? = null,
    val isConfirmButtonEnabled: Boolean = false
)

sealed class AnswerKeyEditorEvent {
    data class OnAnswerSelected(val questionIndex: Int, val answerIndex: Int) : AnswerKeyEditorEvent()
    data class OnSubSubjectSelected(val questionIndex: Int, val subSubject: String) : AnswerKeyEditorEvent()
    data class OnTopicSelected(val questionIndex: Int, val topic: String) : AnswerKeyEditorEvent()
    data class OnToggleDropdown(val questionIndex: Int, val isExpanded: Boolean) : AnswerKeyEditorEvent()
    data class OnSubjectToggled(val subjectName: String) : AnswerKeyEditorEvent()
    object OnConfirmClicked : AnswerKeyEditorEvent()
}
sealed class AnswerKeyEditorNavigationEvent {
    data class NavigateToPreview(val examId: String) : AnswerKeyEditorNavigationEvent()
}

