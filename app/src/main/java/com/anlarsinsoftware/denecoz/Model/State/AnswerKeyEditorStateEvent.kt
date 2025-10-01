package com.anlarsinsoftware.denecoz.Model.State

data class QuestionState(
    val index: Int,
    val selectedAnswerIndex: Int? = null,
    val selectedTopic: String? = null,
    val isDropdownExpanded: Boolean = false
)

data class SubjectDef(val name: String, val totalQuestions: Int, val topics: List<String>)

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
    data class OnTopicSelected(val questionIndex: Int, val topic: String) : AnswerKeyEditorEvent()
    data class OnToggleDropdown(val questionIndex: Int, val isExpanded: Boolean) : AnswerKeyEditorEvent()
    object OnConfirmClicked : AnswerKeyEditorEvent()
}
sealed class AnswerKeyEditorNavigationEvent {
    data class NavigateToPreview(val examId: String) : AnswerKeyEditorNavigationEvent()
}

