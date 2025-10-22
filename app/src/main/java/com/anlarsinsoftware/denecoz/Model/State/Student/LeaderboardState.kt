package com.anlarsinsoftware.denecoz.Model.State.Student


import com.anlarsinsoftware.denecoz.Model.Province
import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary

// Lider tablosundaki bir öğrencinin kaydını temsil eder
data class LeaderboardEntry(
    val studentId: String = "",
    val profileImageUrl : String = "",
    val studentName: String = "Bilinmeyen Öğrenci",
    val net: Double = 0.0,
    val correct: Long = 0,
    val incorrect: Long = 0,
    val city: String? = null,
    val district: String? = null
)

// Lider Tablosu Ekranının anlık durumunu tutar
data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Arama ve Filtreleme
    val examSearchQuery: String = "", // Deneme arama çubuğundaki metin
    val filteredExams: List<PublishedExamSummary> = emptyList(), // Arama sonuçları
    val selectedExam: PublishedExamSummary? = null, // Kullanıcının seçtiği deneme

    val selectedScope: String = "Türkiye", // "Türkiye", "İl", "İlçe"
    val selectedCity: String = "",
    val selectedDistrict: String = "",

    // Dropdown listeleri (EditProfile'dan gelen mantığın aynısı)
    val cities: List<String> = emptyList(),
    val districts: List<String> = emptyList(),
    val allProvincesData: List<Province> = emptyList(),

    // Sonuçlar
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(), // Top 50 listesi
    val myEntry: LeaderboardEntry? = null // Kullanıcının kendi skoru
)

// Kullanıcı eylemleri
sealed class LeaderboardEvent {
    data class OnExamSearchQueryChanged(val query: String) : LeaderboardEvent()
    data class OnExamSelected(val exam: PublishedExamSummary) : LeaderboardEvent()
    data class OnScopeChanged(val scope: String) : LeaderboardEvent()
    data class OnCitySelected(val city: String) : LeaderboardEvent()
    data class OnDistrictSelected(val district: String) : LeaderboardEvent()
}