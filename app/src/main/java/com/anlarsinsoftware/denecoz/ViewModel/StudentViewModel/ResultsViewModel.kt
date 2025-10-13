package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.ResultsUiState
import com.anlarsinsoftware.denecoz.Model.State.Student.SubjectResult
import com.anlarsinsoftware.denecoz.Model.State.Student.TopicResult
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository // Bu Repository'yi genişleteceğiz
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: ExamRepository, // TODO: Bu Repository'yi güncellememiz gerekecek
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState = _uiState.asStateFlow()

    private val examId: String = savedStateHandle.get("examId")!!
    private val attemptId: String = savedStateHandle.get("attemptId")!!

    init {
        loadAndAnalyzeResults()
    }

    private fun loadAndAnalyzeResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: Repository'ye tüm analiz verilerini çeken yeni bir fonksiyon ekleyeceğiz.
            // val result = repository.getAnalysisData(examId, attemptId)

            // --- ŞİMDİLİK ÖRNEK VERİYLE ÇALIŞALIM ---
            // Bu bölüm, Repository'den veri geldiğinde yapılacak olan ANALİZ MANTIĞINI içerir.

            // 1. Repository'den ham verileri al (doğru cevaplar, öğrenci cevapları, konu dağılımı...)

            // 2. Cevapları karşılaştır, D/Y/B sayılarını ve konuları hesapla

            // 3. Hesaplanan verileri SubjectResult ve TopicResult objelerine dönüştür

            // 4. Sonuçları UiState'e aktar
            _uiState.update {
                it.copy(
                    isLoading = false,
                    examName = "Örnek TYT Denemesi",
                    overallCorrect = 80,
                    overallIncorrect = 25,
                    overallEmpty = 15,
                    overallNet = 73.75,
                    subjectResults = listOf(
                        // Örnek bir ders sonucu
                        SubjectResult(
                            subjectName = "Türkçe",
                            correct = 30,
                            incorrect = 8,
                            empty = 2,
                            net = 28.0,
                            topicResults = listOf(
                                TopicResult("Sözcükte Anlam", 2, 0, 0),
                                TopicResult("Cümlede Anlam", 5, 1, 0),
                                TopicResult("Yazım Kuralları", 0, 2, 0)
                            )
                        )
                        // ... diğer dersler
                    )
                )
            }
        }
    }
}