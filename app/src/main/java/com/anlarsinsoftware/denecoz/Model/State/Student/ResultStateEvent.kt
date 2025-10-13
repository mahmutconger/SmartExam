package com.anlarsinsoftware.denecoz.Model.State.Student

// --- ANALİZ SONUÇLARINI TUTACAK MODELLER ---

data class TopicResult(
    val topicName: String,
    val correct: Int,
    val incorrect: Int,
    val empty: Int
)

data class SubjectResult(
    val subjectName: String,
    val correct: Int,
    val incorrect: Int,
    val empty: Int,
    val net: Double,
    val topicResults: List<TopicResult>
)

data class ResultsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val examName: String = "",
    val overallCorrect: Int = 0,
    val overallIncorrect: Int = 0,
    val overallEmpty: Int = 0,
    val overallNet: Double = 0.0,
    val subjectResults: List<SubjectResult> = emptyList()
)