package com.anlarsinsoftware.denecoz.Model.State

import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamDetails


data class PreviewUiState(
    val isLoading: Boolean = true,
    val examDetails: ExamDetails? = null,
    val answerKey: Map<String, String> = emptyMap(),
    val topicDistribution: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
)

sealed class PreviewEvent {
    object OnPublishClicked : PreviewEvent()
}

sealed class PreviewNavigationEvent {
    object NavigateToPublisherHome : PreviewNavigationEvent()
}