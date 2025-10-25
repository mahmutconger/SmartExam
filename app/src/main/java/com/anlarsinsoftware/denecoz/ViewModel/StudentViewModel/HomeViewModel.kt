package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary
import com.anlarsinsoftware.denecoz.Model.State.Student.HomeUiState
import com.anlarsinsoftware.denecoz.Model.State.Student.UserProfile
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
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
    private val authRepository: AuthRepository,
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
        val studentId = auth.currentUser?.uid
        if (studentId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Kullanıcı bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true,) }

            val examsDeferred = async { examRepository.getPublishedExams() }
            val publishersDeferred = async { examRepository.getPublishers() }
            val profileDeferred = async { authRepository.getUserProfile(studentId) }

            val examsResult = examsDeferred.await()
            val publishersResult = publishersDeferred.await()
            val profileResult = profileDeferred.await()

            _uiState.update { currentState ->
                var updatedState = currentState.copy(isLoading = false)

                examsResult.onSuccess { exams ->
                    allExams = exams
                    updatedState = updatedState.copy(exams = exams)
                }.onFailure { e -> updatedState = updatedState.copy(errorMessage = e.message) }

                publishersResult.onSuccess { publishers ->
                    updatedState = updatedState.copy(publishers = publishers)
                }.onFailure { e -> updatedState = updatedState.copy(errorMessage = e.message) }

                profileResult.onSuccess { profile ->
                    updatedState = updatedState.copy(userProfile = profile)
                }.onFailure { e -> updatedState = updatedState.copy(errorMessage = e.message) }

                updatedState
            }

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