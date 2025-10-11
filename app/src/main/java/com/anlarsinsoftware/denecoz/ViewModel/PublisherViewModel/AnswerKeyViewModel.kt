package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEvent // Event dosyasını da güncellememiz gerekecek
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyUiState
import com.anlarsinsoftware.denecoz.Model.State.EditorMode
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
class AnswerKeyViewModel @Inject constructor(
    private val repository: ExamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnswerKeyUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<AnswerKeyNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val examId: String? = savedStateHandle.get<String>("examId")

    init {
        loadExamStatus()
    }

    fun loadExamStatus() {
        if (examId == null) {
            _uiState.update { it.copy(errorMessage = "Hata: Deneme ID'si bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Repository'ye ekleyeceğimiz yeni fonksiyonu çağırıyoruz
            val statusResult = repository.getExamStatus(examId)

            statusResult.onSuccess { status ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        examId = examId,
                        isAnswerKeyCreated = status.hasAnswerKey,
                        isTopicDistributionCreated = status.hasTopicDistribution
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }

    // Olay (Event) yönetimini de yeni akışa göre güncelliyoruz.
    // Önce AnswerKeyEvent dosyasını da güncellemelisin.
    fun onEvent(event: AnswerKeyEvent) {
        when (event) {
            is AnswerKeyEvent.OnNavigateToAnswerKeyEditor -> navigateToEditor(EditorMode.ANSWER_KEY)
            is AnswerKeyEvent.OnNavigateToTopicEditor -> navigateToEditor(EditorMode.TOPIC_DISTRIBUTION)
            is AnswerKeyEvent.OnNavigateToPreview -> navigateToPreview()
        }
    }

    private fun navigateToEditor(mode: EditorMode) {
        examId?.let {
            viewModelScope.launch {
                _navigationEvent.emit(AnswerKeyNavigationEvent.NavigateToEditor(it, mode))
            }
        }
    }

    private fun navigateToPreview() {
        // Bu fonksiyonu Ön İzleme ekranına geçiş için kullanacağız.
        // Şimdilik boş kalabilir veya direkt navigasyon emit edebilir.
        examId?.let {
            // _navigationEvent.emit(AnswerKeyNavigationEvent.NavigateToPreview(it))
        }
    }
}