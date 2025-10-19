package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.AnalysisDetailsUiState
import com.anlarsinsoftware.denecoz.Model.State.Student.QuestionDetail
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisDetailsViewModel @Inject constructor(
    private val repository: ExamRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val examId: String = savedStateHandle.get("examId")!!
    private val attemptId: String = savedStateHandle.get("attemptId")!!
    private val uniqueTopicId: String = savedStateHandle.get("topicName")!!

    init {
        loadAndFilterTopicDetails()
    }

    private fun loadAndFilterTopicDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, topicName = "Yükleniyor...") }

            repository.getAnalysisData(examId, attemptId)
                .onSuccess { data ->
                    val correct = mutableListOf<QuestionDetail>()
                    val incorrect = mutableListOf<QuestionDetail>()
                    val empty = mutableListOf<QuestionDetail>()

                    var displayTopicName: String? = null

                    data.topicDistribution.forEach { (questionIndexStr, topicInfoMapAny) ->
                        val topicInfoMap = topicInfoMapAny as? Map<String, String>
                        val currentTopicName = topicInfoMap?.get("topicName")
                        val currentTopicId = topicInfoMap?.get("topicId")


                        if (currentTopicId == uniqueTopicId) {

                            if (displayTopicName == null) {
                                displayTopicName = currentTopicName
                            }

                            val questionIndex = questionIndexStr.toInt()
                            val studentAnswer = data.studentAnswers[questionIndexStr]
                            val correctAnswer = data.correctAnswers[questionIndexStr] ?: "?"
                            val detail = QuestionDetail(number = questionIndex, correctAnswer = correctAnswer)

                            when {
                                studentAnswer == null || studentAnswer == "-" -> {
                                    empty.add(detail)
                                }
                                studentAnswer == correctAnswer -> {
                                    correct.add(detail)
                                }
                                else -> {
                                    incorrect.add(detail)
                                }
                            }
                        }
                    }

                    val totalQuestionsInTopic = correct.size + incorrect.size + empty.size
                    val successRate = if (totalQuestionsInTopic > 0) {
                        correct.size.toFloat() / totalQuestionsInTopic
                    } else {
                        0f
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            topicName = displayTopicName ?: "Bilinmeyen Konu",
                            correctQuestions = correct,
                            incorrectQuestions = incorrect,
                            emptyQuestions = empty,
                            topicSuccessRate = successRate,
                            totalTopicQuestions = totalQuestionsInTopic
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message)}
                }

            val studentId = auth.currentUser?.uid
            if (studentId != null) {
                repository.getHistoricalTopicPerformance(studentId, uniqueTopicId)
                    .onSuccess { performance ->
                        _uiState.update { it.copy(historicalPerformance = performance) }
                    }
                    .onFailure {
                    }
            }
        }
    }
}