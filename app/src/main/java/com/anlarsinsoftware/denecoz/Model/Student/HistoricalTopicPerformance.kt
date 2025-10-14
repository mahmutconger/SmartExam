package com.anlarsinsoftware.denecoz.Model.Student

data class HistoricalTopicPerformance(
    val totalCorrect: Int = 0,
    val totalIncorrect: Int = 0,
    val totalEmpty: Int = 0
) {
    val totalQuestions: Int
        get() = totalCorrect + totalIncorrect + totalEmpty

    val net: Double
        get() = totalCorrect - (totalIncorrect / 4.0)
}