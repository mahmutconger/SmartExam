package com.anlarsinsoftware.denecoz.Model.State.Student

import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary
import com.anlarsinsoftware.denecoz.Model.Student.PublisherSummary

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val exams: List<PublishedExamSummary> = emptyList(),
    val username: String = "Kullanıcı", // Kişiselleştirme için
    val searchQuery: String = "", // Arama çubuğunun metni
    val publishers: List<PublisherSummary> = emptyList(),
)