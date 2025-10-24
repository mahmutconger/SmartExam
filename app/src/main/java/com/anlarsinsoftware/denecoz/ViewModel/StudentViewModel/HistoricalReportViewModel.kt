package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.util.Log
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
            Log.d("RAPOR_DEBUG", "[ViewModel] loadHistoricalReport başlatıldı. İstenen examType: $examType")
            _uiState.update { it.copy(isLoading = true, examType = examType) }

            val curriculumDeferred = async { examRepository.getCurriculum(examType) }
            val performanceDeferred = async { examRepository.getHistoricalTopicPerformance(studentId) }

            val curriculumResult = curriculumDeferred.await()
            val performanceResult = performanceDeferred.await()

            if (curriculumResult.isFailure || performanceResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Rapor verileri çekilemedi.") }
                return@launch
            }

            val performanceList = performanceResult.getOrThrow()
            val curriculum = curriculumResult.getOrThrow()

            // performanceMap'in anahtarları GLOBAL ID'lerdir (örn: "biyoloji_biyoloji_bitki_biyolojisi")
            val performanceMap = performanceList.associateBy { it.topicId }

            Log.d("RAPOR_DEBUG", "[ViewModel] Ham veriler alındı. Ders sayısı: ${curriculum.size}, Konu performans kaydı: ${performanceList.size}")
            if (performanceList.isEmpty()) {
                Log.w("RAPOR_DEBUG", "[ViewModel] UYARI: Konu performans verisi boş geldi. Cloud Function'ın çalıştığından emin ol.")
            }

            val finalReport = curriculum.map { subjectDef -> // subjectDef.id = "biyoloji"
                val topicReports = subjectDef.topics.map { topicRef -> // topicRef.id = "biyoloji_bitki_biyolojisi"

                    // --- İŞTE DÜZELTME BURADA ---
                    // "biyoloji" ve "biyoloji_bitki_biyolojisi"ni birleştirerek
                    // Cloud Function'ın yazdığı doğru GLOBAL ID'yi oluşturuyoruz.
                    val uniqueKey = "${subjectDef.id}_${topicRef.id}"

                    // Haritadan bu BİRLEŞTİRİLMİŞ (biyoloji_biyoloji_bitki_biyolojisi) anahtarı arıyoruz
                    val performance = performanceMap[uniqueKey]
                    // --- DÜZELTME SONU ---

                    val correct = performance?.totalCorrect ?: 0
                    val incorrect = performance?.totalIncorrect ?: 0
                    val empty = performance?.totalEmpty ?: 0
                    val total = correct + incorrect + empty
                    val rate = if (total > 0) correct.toFloat() / total else 0f

                    val (feedbackMsg, feedbackColor) = generateSmartFeedback(rate, total)

                    SmartTopicReport(
                        topicId = uniqueKey, // Navigasyon için de benzersiz ID'yi verelim
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

            Log.d("RAPOR_DEBUG", "[ViewModel] Analiz tamamlandı. Ekrana ${finalReport.size} ders raporu gönderiliyor.")
            if (finalReport.isNotEmpty() && finalReport.first().topicReports.isNotEmpty()) {
                Log.d("RAPOR_DEBUG", "İlk dersin ilk konusu (Örnek): ${finalReport.first().topicReports.first().topicName} -")
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