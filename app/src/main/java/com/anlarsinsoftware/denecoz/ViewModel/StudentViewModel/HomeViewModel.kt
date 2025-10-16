package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary
import com.anlarsinsoftware.denecoz.Model.State.Student.HomeUiState
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val auth: FirebaseAuth // Kullanıcı adını almak için
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    // Filtrelenmemiş orijinal deneme listesini burada saklayacağız
    private var allExams: List<PublishedExamSummary> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, username = auth.currentUser?.displayName ?: "Kullanıcı") }

            // Denemeleri ve yayınevlerini AYNI ANDA (paralel) çek
            val examsDeferred = async { examRepository.getPublishedExams() }
            val publishersDeferred = async { examRepository.getPublishers() }

            val examsResult = examsDeferred.await()
            val publishersResult = publishersDeferred.await()

            examsResult.onSuccess { exams ->
                allExams = exams
                _uiState.update { it.copy(exams = exams) }
            }.onFailure { exception ->

                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.message)
                }
            }

            publishersResult.onSuccess { publishers ->
                _uiState.update { it.copy(publishers = publishers) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.message)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // Arama için event fonksiyonu
    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        val filteredExams = if (query.isBlank()) {
            allExams
        } else {
            allExams.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.publisherName.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(exams = filteredExams) }
    }
}