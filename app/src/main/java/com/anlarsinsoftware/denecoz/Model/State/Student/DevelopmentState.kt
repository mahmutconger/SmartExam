package com.anlarsinsoftware.denecoz.Model.State.Student

import com.anlarsinsoftware.denecoz.Model.Student.NetScoreHistoryPoint
import java.util.concurrent.TimeUnit

// Kullanıcının seçebileceği zaman filtrelerini temsil eden bir enum
enum class TimeFilter(val durationInDays: Long,val filterName : String) {
    ONE_MONTH(30,"1 Ay"),
    THREE_MONTHS(90,"3 Ay"),
    SIX_MONTHS(180,"6 AY"),
    ONE_YEAR(365,"1 Yıl"),
    TWO_YEAR(730,"2 Yıl")
}

// "Gelişim" ekranının anlık durumunu tutar
data class DevelopmentUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Filtre seçenekleri
    val selectedExamType: String = "TYT", // Varsayılan olarak TYT'yi göster
    val selectedTimeFilter: TimeFilter = TimeFilter.SIX_MONTHS, // Varsayılan son 6 ay

    // Grafik kütüphanesine gönderilecek olan, filtrelenmiş veri
    val chartData: List<NetScoreHistoryPoint> = emptyList()
)

// Kullanıcının yapabileceği eylemler
sealed class DevelopmentEvent {
    // Kullanıcı sınav türü (TYT, AYT) filtresini değiştirdiğinde
    data class OnExamTypeSelected(val examType: String) : DevelopmentEvent()

    // Kullanıcı zaman (1 Ay, 6 Ay) filtresini değiştirdiğinde
    data class OnTimeFilterSelected(val filter: TimeFilter) : DevelopmentEvent()

    // Kullanıcı "Analiz Raporunu Gör" butonuna bastığında
    data class OnShowReportClicked(val examType: String) : DevelopmentEvent()
}

// ViewModel'dan View'e gönderilecek navigasyon komutları
sealed class DevelopmentNavigationEvent {
    // "Akıllı Karne" ekranına gitme komutu
    data class NavigateToHistoricalReport(val examType: String) : DevelopmentNavigationEvent()
}