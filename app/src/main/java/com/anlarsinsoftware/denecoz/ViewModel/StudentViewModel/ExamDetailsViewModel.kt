package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamDetailsEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamDetailsNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamDetailsUiState
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository // Doğru yolu import et
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
class ExamDetailsViewModel @Inject constructor(
    private val repository: ExamRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ExamDetailsNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val examId: String = savedStateHandle.get("examId")!!

    init {
        loadExamAllData()
    }

    fun onEvent(event: ExamDetailsEvent) {
        when (event) {
            is ExamDetailsEvent.OnSolveClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(ExamDetailsNavigationEvent.NavigateToExamEntry(examId))
                }
            }
            is ExamDetailsEvent.OnLeaderboardClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(ExamDetailsNavigationEvent.NavigateToLeaderboard(examId))
                }
            }
            is ExamDetailsEvent.OnRefresh -> {
                loadExamAllData()
            }
        }
    }

    private fun loadExamAllData() {
        val studentId = auth.currentUser?.uid
        if (studentId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Kullanıcı bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // İki farklı veri isteğini AYNI ANDA (paralel) başlatıyoruz
            val detailsDeferred = async { repository.getExamDetails(examId) }
            val solvedDeferred = async { repository.hasUserSolvedExam(studentId, examId) }

            // İki isteğin de bitmesini bekliyoruz
            val detailsResult = detailsDeferred.await()
            val solvedResult = solvedDeferred.await()

            // State'i tek seferde, atomik olarak güncelliyoruz
            _uiState.update { currentState ->
                var updatedState = currentState.copy(isLoading = false)

                detailsResult.onSuccess { details ->
                    updatedState = updatedState.copy(
                        examName = details.name,
                        publicationDate = details.publicationDate,
                        publisherName = details.publisherName,
                        examType = details.examType,
                        difficulty = details.difficulty,
                        solveCount = details.solveCount
                    )
                }.onFailure {
                    updatedState = updatedState.copy(errorMessage = it.message)
                }

                solvedResult.onSuccess { isSolved ->
                    updatedState = updatedState.copy(isSolvedByUser = isSolved)
                }.onFailure {
                    // Bu hata kritik olmayabilir, sadece çözülme durumu bilinmez
                  //updatedState = updatedPost.copy(isSolvedByUser = false)
                }

                updatedState // Son ve tam state'i döndür
            }
        }
    }
}