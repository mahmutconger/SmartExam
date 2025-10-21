package com.anlarsinsoftware.denecoz.Model.State.Student

// Ekranın anlık durumunu tutacak olan ana State
data class ExamDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Tasarımındaki "Genel Bilgiler" bölümü
    val examName: String = "",
    val publicationDate: String = "", // Basım Tarihi
    val publisherName: String = "", // Yayınevi
    val examType: String = "", // Deneme Türü

    // Tasarımındaki "Detaylar" bölümü
    val difficulty: String = "Bilinmiyor", // Zorluk Seviyesi
    val isSolvedByUser: Boolean = false, // Çözülme Durumu (true = Çözüldü)
    val solveCount: Long = 0 // Çözülme Sayısı
)

// Kullanıcının yapabileceği eylemler
sealed class ExamDetailsEvent {
    object OnSolveClicked : ExamDetailsEvent() // "Denemeyi Kontrol Et"
    object OnLeaderboardClicked : ExamDetailsEvent() // "Sırala Tablosunu İncele"
    object OnRefresh : ExamDetailsEvent() // Pull-to-refresh
}

// ViewModel'dan View'e gönderilecek navigasyon komutları
sealed class ExamDetailsNavigationEvent {
    data class NavigateToExamEntry(val examId: String) : ExamDetailsNavigationEvent()
    data class NavigateToLeaderboard(val examId: String) : ExamDetailsNavigationEvent()
}