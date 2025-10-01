package com.anlarsinsoftware.denecoz.Model.State

import android.net.Uri

enum class TabOption { ANSWER_KEY, TOPICS }
enum class FileType { PDF, EXCEL, MANUAL }

data class UploadedFile(val name: String, val type: FileType, val uri: Uri?)

data class AnswerKeyUiState(
    val examId: String? = null,
    val selectedTab: TabOption = TabOption.ANSWER_KEY,
    val uploadedFile: UploadedFile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class AnswerKeyEvent {
    data class OnTabSelected(val tab: TabOption) : AnswerKeyEvent()
    data class OnFileSelected(val uri: Uri?, val fileName: String, val fileType: FileType) : AnswerKeyEvent()
    object OnManualEntrySelected : AnswerKeyEvent()
    object OnChangeFileClicked : AnswerKeyEvent()
    object OnContinueClicked : AnswerKeyEvent()
}

sealed class AnswerKeyNavigationEvent {
    data class NavigateToEditor(val examId: String, val mode: EditorMode) : AnswerKeyNavigationEvent()
}

enum class EditorMode { ANSWER_KEY, TOPIC_DISTRIBUTION }