package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.* // Oluşturduğumuz tüm state'leri import et
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
class HistoricalReportViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricalReportUiState())
    val uiState = _uiState.asStateFlow()

    private val examType: String = savedStateHandle.get("examType")!!
    private val studentId = auth.currentUser?.uid

    init {
        loadHistoricalReport()
    }

    fun onEvent(event: HistoricalReportEvent) {
        when (event) {
            is HistoricalReportEvent.OnRefresh -> loadHistoricalReport()
        }
    }

    private fun loadHistoricalReport() {
        if (studentId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Kullanıcı bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, examType = examType) }

            // 1. O sınav türünün (TYT) DERS/KONU yapısını çek
            val curriculumDeferred = async { examRepository.getCurriculum(examType) }
            // 2. Öğrencinin TÜM konu performans verilerini çek
            val performanceDeferred = async { examRepository.getHistoricalTopicPerformance(studentId) }

            val curriculumResult = curriculumDeferred.await()
            val performanceResult = performanceDeferred.await()

            // Hata durumlarını kontrol et
            if (curriculumResult.isFailure || performanceResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Rapor verileri çekilemedi.") }
                return@launch
            }

            val curriculum = curriculumResult.getOrThrow()
            val performanceMap = performanceResult.getOrThrow()
                .associateBy { it.topicId }

            val finalReport = curriculum.map { subjectDef ->

                val topicReports = subjectDef.topics.map { topicRef ->

                    val performance = performanceMap[topicRef.id]

                    val correct = performance?.totalCorrect ?: 0
                    val incorrect = performance?.totalIncorrect ?: 0
                    val empty = performance?.totalEmpty ?: 0
                    val total = correct + incorrect + empty
                    val rate = if (total > 0) correct.toFloat() / total else 0f

                    val (feedbackMsg, feedbackColor) = generateSmartFeedback(rate, total)

                    SmartTopicReport(
                        topicId = topicRef.id,
                        topicName = topicRef.name,
                        correct = correct,
                        incorrect = incorrect,
                        empty = empty,
                        total = total,
                        successRate = rate,
                        feedbackMessage = feedbackMsg,
                        feedbackColor = Color(feedbackColor)
                    )
                }

                SmartSubjectReport(
                    subjectName = subjectDef.name,
                    topicReports = topicReports
                )
            }

            _uiState.update {
                it.copy(isLoading = false, subjectReports = finalReport)
            }
        }
    }

    private fun generateSmartFeedback(rate: Float, total: Int): Pair<String, Long> {
        if (total == 0) {
            return Pair("Bu konudan henüz hiç soru çözmemişsin.", 0xFF808080) // Gri
        }

        return when {
            rate <= 0.35f -> Pair("❗️ Acil Tekrar! Bu konuya odaklanmalısın.", 0xFFC62828) // Koyu Kırmızı
            rate <= 0.60f -> Pair("⚠️ Geliştirilmeli. Sık tekrar etmen iyi olur.", 0xFFE65100) // Koyu Turuncu
            rate < 1.0f -> Pair("💡 Neredeyse Tamam! Sadece küçük hatalar var.", 0xFF2E7D32) // Koyu Yeşil
            else -> Pair("🔥 Mükemmel! Bu konuya tam hakimsin.", 0xFF1565C0) // Koyu Mavi
        }
    }
}