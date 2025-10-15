package com.anlarsinsoftware.denecoz.Model.State.Student

import java.util.Date

data class UserProfile(
    val uid: String = "",
    val name: String = "Öğrenci Adı",
    val email: String = "",
    val bestScores: Map<String, BestNetResult> = emptyMap()
)
// Profil ekranındaki bir deneme satırını temsil eder
data class PastAttemptSummary(
    val attemptId: String,
    val examId: String,
    val examName: String,
    val examType: String,
    val coverImageUrl: String?,
    val completedAt: Date
)

// Bir sınav türündeki en iyi neti temsil eder
data class BestNetResult(
    val correct: Long = 0,
    val incorrect: Long = 0,
    val net: Double = 0.0
)

// Model/State/ klasörüne eklenebilir
data class ProfileUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userProfile: UserProfile? = null,
    val pastAttempts: List<PastAttemptSummary> = emptyList(),
    val totalAttemptsCount: Int = 0
)

