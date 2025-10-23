package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.DevelopmentEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.DevelopmentNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.DevelopmentUiState
import com.anlarsinsoftware.denecoz.Model.State.Student.TimeFilter
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DevelopmentViewModel @Inject constructor(
    private val repository: ExamRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevelopmentUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<DevelopmentNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val studentId = auth.currentUser?.uid

    init {
        // Ekran ilk açıldığında, varsayılan filtrelerle (TYT, 6 Ay) verileri yükle
        loadChartData()
    }

    fun onEvent(event: DevelopmentEvent) {
        viewModelScope.launch {
            when (event) {
                is DevelopmentEvent.OnExamTypeSelected -> {
                    _uiState.update { it.copy(selectedExamType = event.examType) }
                    loadChartData() // Filtre değişti, veriyi yeniden çek
                }
                is DevelopmentEvent.OnTimeFilterSelected -> {
                    _uiState.update { it.copy(selectedTimeFilter = event.filter) }
                    loadChartData() // Filtre değişti, veriyi yeniden çek
                }
                is DevelopmentEvent.OnShowReportClicked -> {
                    _navigationEvent.emit(DevelopmentNavigationEvent.NavigateToHistoricalReport(event.examType))
                }
            }
        }
    }

    private fun loadChartData() {
        if (studentId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Kullanıcı bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentState = _uiState.value
            val examType = currentState.selectedExamType
            val timeFilter = currentState.selectedTimeFilter

            // Başlangıç tarihini hesapla
            val startDate = getStartDateFromFilter(timeFilter)

            repository.getNetScoreHistory(studentId, examType, startDate)
                .onSuccess { historyData ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chartData = historyData
                        )
                    }
                }
                .onFailure { exception ->
                    Log.e("DevViewModel", "Grafik verisi çekilemedi: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chartData = emptyList(), // Hata durumunda grafiği temizle
                            errorMessage = exception.message
                        )
                    }
                }
        }
    }

    /**
     * Seçilen filtreye göre (1 Ay, 6 Ay vb.) bir başlangıç tarihi hesaplar.
     */
    private fun getStartDateFromFilter(filter: TimeFilter): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -filter.durationInDays.toInt())
        return calendar.time
    }
}