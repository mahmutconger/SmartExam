package com.anlarsinsoftware.denecoz.Model.State.Publisher

import java.util.Date

data class PublisherProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val logoUrl: String? = null,
    val verificationStatus: String = "PENDING"
)

data class ExamSummaryItem(
    val id: String,
    val name: String,
    val status: String, // "draft", "published"
    val coverImageUrl : String?,
    val creationDate: Date?
)

data class PubHomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val publisherProfile: PublisherProfile? = null,
    val exams: List<ExamSummaryItem> = emptyList(),
    val searchQuery: String = ""
)

sealed class PubHomeEvent {
    data class OnExamClicked(val exam: ExamSummaryItem) : PubHomeEvent()
    object OnAddNewExamClicked : PubHomeEvent()
    object OnRefresh : PubHomeEvent()
    data class OnQueryChanged(val query: String) : PubHomeEvent()
}

sealed class PubHomeNavigationEvent {
    object NavigateToGeneralInfo : PubHomeNavigationEvent()
    data class NavigateToAnswerKey(val examId: String) : PubHomeNavigationEvent()
    // data class NavigateToExamStats(val examId: String) : PubHomeNavigationEvent() // Gelecekteki özellik
}