package com.anlarsinsoftware.denecoz.Model.State.Student

import androidx.compose.ui.graphics.Color

data class SmartTopicReport(
    val topicId: String,
    val topicName: String,
    val correct: Int,
    val incorrect: Int,
    val empty: Int,
    val total: Int,
    val successRate: Float,
    val feedbackMessage: String,
    val feedbackColor: Color
)

data class SmartSubjectReport(
    val subjectName: String,
    val topicReports: List<SmartTopicReport>
)

data class HistoricalReportUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val examType: String = "", // "TYT", "AYT" vb.
    val subjectReports: List<SmartSubjectReport> = emptyList()
)

sealed class HistoricalReportEvent {
    object OnRefresh : HistoricalReportEvent()
    // Şimdilik bu ekran sadece okuma amaçlı, tıklama eklemiyoruz
}