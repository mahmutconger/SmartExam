package com.anlarsinsoftware.denecoz.Model.Student

import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamDetails

data class AnalysisData(
    val examDetails: ExamDetails, // Denemenin yapısı (dersler, soru sayıları)
    val studentAnswers: Map<String, String>, // Öğrencinin cevapları ("1" -> "A")
    val correctAnswers: Map<String, String>, // Doğru cevap anahtarı ("1" -> "C")
    val topicDistribution: Map<String, Map<String, Any>>, // Konu dağılımı ("1" -> "Üslü Sayılar")
    val studentAlternativeChoice: String? // Öğrencinin seçmeli ders tercihi (örn: "ek_felsefe")
)