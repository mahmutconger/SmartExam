package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.PreviewEvent
import com.anlarsinsoftware.denecoz.Model.State.PreviewNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.PreviewUiState
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val repository: ExamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PreviewNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val examId: String? = savedStateHandle.get("examId")

    init {
        loadFullExamData()
    }

    private fun loadFullExamData() {
        if (examId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Deneme ID'si bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getFullExamForPreview(examId)
                .onSuccess { fullData ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            examDetails = fullData.details,
                            answerKeys = fullData.answerKeys,
                            topicDistributions = fullData.topicDistributions
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }


    fun onEvent(event: PreviewEvent) {
        when (event) {
            is PreviewEvent.OnPublishClicked -> publishExam()
        }
    }

    private fun publishExam() {
        if (examId == null || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.publishExam(examId)
                .onSuccess {
                    _navigationEvent.emit(PreviewNavigationEvent.NavigateToPublisherHome)
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }
}