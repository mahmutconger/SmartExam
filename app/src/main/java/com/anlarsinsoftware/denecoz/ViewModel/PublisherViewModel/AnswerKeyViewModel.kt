package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEvent
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyUiState
import com.anlarsinsoftware.denecoz.Model.State.EditorMode
import com.anlarsinsoftware.denecoz.Model.State.FileType
import com.anlarsinsoftware.denecoz.Model.State.TabOption
import com.anlarsinsoftware.denecoz.Model.State.UploadedFile
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

    init {
        val examId = savedStateHandle.get<String>("examId")
        if (examId == null) {
            _uiState.update { it.copy(errorMessage = "Hata: Deneme ID'si bulunamadı.") }
        } else {
            _uiState.update { it.copy(examId = examId) }
        }
    }

    fun onEvent(event: AnswerKeyEvent) {
        when (event) {
            is AnswerKeyEvent.OnTabSelected -> _uiState.update { it.copy(selectedTab = event.tab) }
            is AnswerKeyEvent.OnFileSelected -> _uiState.update { it.copy(uploadedFile = UploadedFile(event.fileName, event.fileType, event.uri)) }
            is AnswerKeyEvent.OnManualEntrySelected -> _uiState.update { it.copy(uploadedFile = UploadedFile(
                "Manuel Giriş",
                FileType.MANUAL,
                null
            )
            ) }
            is AnswerKeyEvent.OnChangeFileClicked -> _uiState.update { it.copy(uploadedFile = null) }
            is AnswerKeyEvent.OnContinueClicked -> handleContinue()
        }
    }

    private fun handleContinue() {
        val currentState = _uiState.value
        val examId = currentState.examId ?: return

        if (currentState.uploadedFile == null) {
            _uiState.update { it.copy(errorMessage = "Lütfen bir yöntem seçin.") }
            return
        }

        viewModelScope.launch {
            when (currentState.uploadedFile.type) {
                FileType.MANUAL -> {
                    val mode = if (currentState.selectedTab == TabOption.ANSWER_KEY) {
                        EditorMode.ANSWER_KEY
                    } else {
                        EditorMode.TOPIC_DISTRIBUTION
                    }
                    _navigationEvent.emit(AnswerKeyNavigationEvent.NavigateToEditor(examId, mode))
                }
                FileType.EXCEL, FileType.PDF -> {
                    // TODO: Dosya işleme mantığı
                    // Şimdilik sadece bir mesaj gösterip bir sonraki adıma geçebiliriz
                    // veya Repository'deki dosya işleme fonksiyonunu çağırabiliriz.

                    _uiState.update { it.copy(isLoading = true) }
                    // repository.processAnswerKeyFile(...)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}