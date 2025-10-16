package com.anlarsinsoftware.denecoz.Model.State.Student

import java.util.Date

// Model/Student/UserProfile.kt
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val bestScores: Map<String, BestNetResult> = emptyMap(),
    val city: String? = null,
    val district: String? = null,
    val school: String? = null,
    val educationLevel: String? = null,
    val isProfileComplete: Boolean = false
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

