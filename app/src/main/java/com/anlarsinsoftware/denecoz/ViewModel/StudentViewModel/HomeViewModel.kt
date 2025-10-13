package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.HomeUiState
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadExams()
    }

    private fun loadExams() {
        viewModelScope.launch {
            Log.d("DEBUG_DENECOZ", "loadExams fonksiyonu çalıştı.")
            _uiState.update { it.copy(isLoading = true) }
            examRepository.getPublishedExams()
                .onSuccess { examList ->
                    // KRİTİK LOG: ViewModel'a kaç adet deneme geldi?
                    Log.d("DEBUG_DENECOZ", "ViewModel'a ${examList.size} deneme geldi (onSuccess).")
                    _uiState.update {
                        it.copy(isLoading = false, exams = examList)
                    }
                }
                .onFailure { exception ->
                    Log.e("DEBUG_DENECOZ", "ViewModel HATA ALDI (onFailure): ${exception.message}")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message)
                    }
                }
        }
    }

    // TODO: Kullanıcı etkileşimleri için onEvent fonksiyonu eklenecek
    // fun onEvent(event: HomeEvent) { ... }
}