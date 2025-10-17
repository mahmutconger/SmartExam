package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Publisher.*
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PubHomeViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PubHomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PubHomeNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var allExams: List<ExamSummaryItem> = emptyList()

    init {
        loadPublisherData()
    }

     fun onEvent(event: PubHomeEvent) {
        when (event) {
            is PubHomeEvent.OnAddNewExamClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(PubHomeNavigationEvent.NavigateToGeneralInfo)
                }
            }
            is PubHomeEvent.OnExamClicked -> {
                handleExamClick(event.exam)
            }
            is PubHomeEvent.OnRefresh -> {
                loadPublisherData()
            }
            is PubHomeEvent.OnQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }

                val filteredExams = if (event.query.isBlank()) {
                    allExams
                } else {
                    allExams.filter { exam ->
                        exam.name.contains(event.query, ignoreCase = true)
                    }
                }

                // 3. Filtrelenmiş listeyi state'e ata
                _uiState.update { it.copy(exams = filteredExams) }
            }
        }
    }

    private fun handleExamClick(exam: ExamSummaryItem) {
        viewModelScope.launch {
            if (exam.status == "draft") {
                _navigationEvent.emit(PubHomeNavigationEvent.NavigateToAnswerKey(exam.id))
            }

            else if (exam.status == "published") {
                // TODO: İstatistik ekranı navigasyonu
                // _navigationEvent.emit(PubHomeNavigationEvent.NavigateToExamStats(exam.id))
            }
        }
    }

    private fun loadPublisherData() {
        val publisherId = auth.currentUser?.uid
        if (publisherId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Yayıncı oturumu bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

             val profileDeferred = async { examRepository.getPublisherProfile(publisherId) }
            val examsDeferred = async { examRepository.getExamsForPublisher(publisherId) }

            val profileResult = profileDeferred.await()
            val examsResult = examsDeferred.await()

            var finalState = _uiState.value


            profileResult.onSuccess { profile ->
                finalState = finalState.copy(publisherProfile = profile)
            }.onFailure { exception ->
                finalState = finalState.copy(errorMessage = exception.message)
            }

            examsResult.onSuccess { exams ->
                val examItems = exams.map {
                    ExamSummaryItem(it.id, it.name, it.status,it.coverImageUrl,it.createdAt)
                }
                allExams = examItems
                finalState = finalState.copy(exams = examItems)
            }.onFailure { exception ->
                finalState = finalState.copy(errorMessage = exception.message)
            }

            _uiState.update { finalState.copy(isLoading = false) }
        }
    }
}