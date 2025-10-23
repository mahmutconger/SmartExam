package com.anlarsinsoftware.denecoz.Model.Student

data class HistoricalTopicPerformance(
    val studentId: String = "",
    val topicId: String = "",
    val topicName: String = "",
    val totalCorrect: Int = 0,
    val totalIncorrect: Int = 0,
    val totalEmpty: Int = 0
) {
    val totalQuestions: Int
        get() = totalCorrect + totalIncorrect + totalEmpty

    val net: Double
        get() = totalCorrect - (totalIncorrect / 4.0)

    val successRate: Float
        get() = if (totalQuestions > 0) totalCorrect.toFloat() / totalQuestions else 0f
}