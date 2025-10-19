package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.ResultsUiState
import com.anlarsinsoftware.denecoz.Model.State.Student.SubjectResult
import com.anlarsinsoftware.denecoz.Model.State.Student.TopicResult
import com.anlarsinsoftware.denecoz.Model.Student.AnalysisData
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Analiz sırasında D/Y/B sayılarını kolayca artırmak için geçici bir yardımcı class
private data class MutableTopicResult(var correct: Int = 0, var incorrect: Int = 0, var empty: Int = 0)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: ExamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState = _uiState.asStateFlow()

     val examId: String = savedStateHandle.get("examId")!!
     val attemptId: String = savedStateHandle.get("attemptId")!!

    init {
        loadAndAnalyzeResults()
    }

    private fun loadAndAnalyzeResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. ADIM: Repository'den tüm ham analiz verilerini çek
            repository.getAnalysisData(examId, attemptId)
                .onSuccess { analysisData ->
                    // 2. ADIM: Gelen ham veriyi anlamlı sonuçlara dönüştüren analiz motorunu çalıştır
                    val results = analyze(analysisData)
                    _uiState.update { results } // Analiz sonucunu doğrudan state'e ata
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    /**
     * Bu fonksiyon, projenin kalbidir. Ham veriyi alıp tam bir analiz yapar.
     */
    private fun analyze(data: AnalysisData): ResultsUiState {
        val finalSubjectResults = mutableListOf<SubjectResult>()
        var overallCorrect = 0
        var overallIncorrect = 0
        var overallEmpty = 0

        var questionCounter = 0
        data.examDetails.subjects.forEach { subjectMap ->
            val testName = subjectMap["testName"] as String
            val questionCount = (subjectMap["questionCount"] as Long).toInt()
            val subSubjects = subjectMap["subSubjects"] as? List<Map<String, Any>>

            var subjectCorrect = 0
            var subjectIncorrect = 0
            var subjectEmpty = 0
            val topicResultsMap = mutableMapOf<String, MutableTopicResult>()
            val topicIdToNameMap = mutableMapOf<String, String>()

            for (i in 1..questionCount) {
                val overallIndex = (questionCounter + i).toString()

                val correctAnswer = data.correctAnswers[overallIndex]
                val studentAnswer = data.studentAnswers[overallIndex]

                val topicInfo = data.topicDistribution[overallIndex] as? Map<String, String>
                val uniqueTopicId = topicInfo?.get("topicId") ?: "diger_${overallIndex}"
                val topicNameForDisplay = topicInfo?.get("topicName") ?: "Diğer"
                topicIdToNameMap.putIfAbsent(uniqueTopicId, topicNameForDisplay)
                val subjectIdOfQuestion = uniqueTopicId.substringBeforeLast('_')
                val isAlternative = subSubjects?.find { it["subjectId"] == subjectIdOfQuestion }?.get("isAlternative") as? Boolean

                if (isAlternative != null && subjectIdOfQuestion != data.studentAlternativeChoice) {
                    continue
                }
                when {
                    studentAnswer == null || studentAnswer == "-" -> {
                        subjectEmpty++
                        val topicResult = topicResultsMap.getOrPut(uniqueTopicId) { MutableTopicResult() }
                        topicResult.empty++
                    }
                    studentAnswer == correctAnswer -> {
                        subjectCorrect++
                        val topicResult = topicResultsMap.getOrPut(uniqueTopicId) { MutableTopicResult() }
                        topicResult.correct++
                    }
                    else -> {
                        subjectIncorrect++
                        val topicResult = topicResultsMap.getOrPut(uniqueTopicId) { MutableTopicResult() }
                        topicResult.incorrect++
                    }
                }
            }

            val subjectNet = subjectCorrect - (subjectIncorrect / 4.0)
            overallCorrect += subjectCorrect
            overallIncorrect += subjectIncorrect
            overallEmpty += subjectEmpty

            finalSubjectResults.add(
                SubjectResult(
                    subjectName = testName,
                    correct = subjectCorrect,
                    incorrect = subjectIncorrect,
                    empty = subjectEmpty,
                    net = subjectNet,
                    topicResults = topicResultsMap.map { (uniqueId, result) ->
                        TopicResult(
                            uniqueTopicId = uniqueId,
                            topicName = topicIdToNameMap[uniqueId] ?: "Diğer",
                            correct = result.correct,
                            incorrect = result.incorrect,
                            empty = result.empty
                        )
                    }.sortedBy { it.topicName }
                )
            )
            questionCounter += questionCount
        }

        val overallNet = overallCorrect - (overallIncorrect / 4.0)

        return ResultsUiState(
            isLoading = false,
            examName = data.examDetails.name,
            overallCorrect = overallCorrect,
            overallIncorrect = overallIncorrect,
            overallEmpty = overallEmpty,
            overallNet = overallNet,
            subjectResults = finalSubjectResults
        )
    }
}