package com.anlarsinsoftware.denecoz.Model.State.Student

import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val exams: List<PublishedExamSummary> = emptyList(),
    // val publishers: List<Publisher> = emptyList() // Yayınevleri için
)