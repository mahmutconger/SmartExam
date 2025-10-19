package com.anlarsinsoftware.denecoz.Model.State.Student

import com.anlarsinsoftware.denecoz.Model.Student.HistoricalTopicPerformance

data class QuestionDetail(
    val number: Int,
    val correctAnswer: String
)

data class AnalysisDetailsUiState(
    val isLoading: Boolean = true,
    val topicName: String = "",
    val errorMessage : Any? = null,
    val correctQuestions: List<QuestionDetail> = emptyList(),
    val incorrectQuestions: List<QuestionDetail> = emptyList(),
    val emptyQuestions: List<QuestionDetail> = emptyList(),
    val topicSuccessRate: Float = 0f,
    val totalTopicQuestions: Int = 0,
    val historicalPerformance: HistoricalTopicPerformance? = null
)