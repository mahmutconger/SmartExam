package com.anlarsinsoftware.denecoz.Model.State

import android.net.Uri

enum class TabOption { ANSWER_KEY, TOPICS }
enum class FileType { PDF, EXCEL, MANUAL }

data class UploadedFile(val name: String, val type: FileType, val uri: Uri?)

data class BookletStatus(
    val hasAnswerKey: Boolean = false,
    val hasTopicDistribution: Boolean = false
)

data class AnswerKeyUiState(
    val examId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bookletStatus: Map<String, BookletStatus> = emptyMap()
)

sealed class AnswerKeyEvent {
    data class OnNavigateToAnswerKeyEditor(val bookletName: String) : AnswerKeyEvent()
    data class OnNavigateToTopicEditor(val bookletName: String) : AnswerKeyEvent()
    object OnNavigateToPreview : AnswerKeyEvent()
    object OnAddNewBooklet : AnswerKeyEvent()
}

sealed class AnswerKeyNavigationEvent {
    data class NavigateToEditor(
        val examId: String,
        val mode: EditorMode,
        val bookletName: String
    ) : AnswerKeyNavigationEvent()

    data class NavigateToPreview(val examId: String) : AnswerKeyNavigationEvent()

}
enum class EditorMode { ANSWER_KEY, TOPIC_DISTRIBUTION }