package com.anlarsinsoftware.denecoz.Model.State.Publisher

import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamDetails


data class PreviewUiState(
    val examDetails: ExamDetails? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val answerKeys: Map<String, Map<String, String>> = emptyMap(),
    val topicDistributions: Map<String, Map<String, String>> = emptyMap()
)

sealed class PreviewEvent {
    object OnPublishClicked : PreviewEvent()
}

sealed class PreviewNavigationEvent {
    object NavigateToPublisherHome : PreviewNavigationEvent()
}