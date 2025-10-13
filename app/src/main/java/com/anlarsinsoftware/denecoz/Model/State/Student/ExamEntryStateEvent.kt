package com.anlarsinsoftware.denecoz.Model.State.Student

import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamDetails

// --- EKRANIN DURUMUNU TUTACAK DATA CLASS ---
data class ExamEntryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val examDetails: ExamDetails? = null,
    val selectedBooklet: String? = null,
    val alternativeChoice: String? = null,
    val studentAnswers: Map<Int, Int?> = emptyMap(),
    val showConfirmationDialog: Boolean = false,
    val unansweredSummary: Map<String, Int> = emptyMap()
)

// --- KULLANICI EYLEMLERİNİ TEMSİL EDEN EVENT'LER ---
sealed class ExamEntryEvent {
    // Kullanıcı bir kitapçık seçtiğinde
    data class OnBookletSelected(val bookletName: String) : ExamEntryEvent()
    // Kullanıcı bir cevap şıkkını işaretlediğinde
    data class OnAnswerSelected(val questionIndex: Int, val answerIndex: Int) : ExamEntryEvent()
    // Kullanıcı seçmeli bir dersi seçtiğinde
    data class OnAlternativeSelected(val choiceId: String) : ExamEntryEvent()
    // Kullanıcı denemeyi bitir butonuna bastığında
    object OnSubmitClicked : ExamEntryEvent()
    // Kullanıcı onay diyaloğunda "Evet" dediğinde
    object OnConfirmSubmit : ExamEntryEvent()
    // Kullanıcı onay diyaloğunu kapattığında veya "Hayır" dediğinde
    object OnDismissDialog : ExamEntryEvent()
}

// --- NAVİGASYON İÇİN EVENT'LER ---
sealed class ExamEntryNavigationEvent {
    // Analiz ekranına yönlendirme
    data class NavigateToResults(val examId: String, val attemptId: String) : ExamEntryNavigationEvent()
}