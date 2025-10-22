package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.Province
import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary
import com.anlarsinsoftware.denecoz.Model.State.Student.LeaderboardEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.LeaderboardUiState
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
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
class LeaderboardViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val authRepository: AuthRepository, // İl/ilçe ve profil bilgisi için
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState = _uiState.asStateFlow()

    private var allExams: List<PublishedExamSummary> = emptyList() // Arama için tam deneme listesi
    private val studentId = auth.currentUser?.uid

    init {
        // Gerekli tüm verileri paralel olarak yükle
        loadInitialData(savedStateHandle.get("examId"))
    }

    private fun loadInitialData(initialExamId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Üç kritik veriyi AYNI ANDA (paralel) çek
            val examsDeferred = async { examRepository.getPublishedExams() }
            val profileDeferred = async { studentId?.let { authRepository.getUserProfile(it) } }
            val provincesDeferred = async { authRepository.getProvincesAndDistricts() }

            // Sonuçları bekle
            val examsResult = examsDeferred.await()
            val profileResult = profileDeferred.await()
            val provincesResult = provincesDeferred.await()

            // State'i güncellemek için geçici bir değişken
            var updatedState = _uiState.value

            // 1. Denemeleri al
            examsResult.onSuccess { exams ->
                allExams = exams
                updatedState = updatedState.copy(filteredExams = exams) // Başlangıçta tümünü göster
            }.onFailure { exception ->
                updatedState = updatedState.copy(errorMessage = exception.message)
            }

            // 2. Profil bilgilerini (varsayılan il/ilçe) al
            profileResult?.onSuccess { profile ->
                updatedState = updatedState.copy(
                    selectedCity = profile?.city ?: "",
                    selectedDistrict = profile?.district ?: ""
                )
            }?.onFailure { exception ->
                Log.e("LeaderboardVM", "Profil bilgisi çekilemedi: ${exception.message}")
            }

            // 3. İl/ilçe listelerini al
            provincesResult.onSuccess { provinces ->
                updatedState = updatedState.copy(
                    allProvincesData = provinces,
                    cities = provinces.map { it.name }.sorted(),
                    districts = getDistrictsForCity(
                        updatedState.selectedCity,
                        provinces
                    )
                )
            }.onFailure { exception ->
                Log.e("LeaderboardVM", "İl/İlçe verisi çekilemedi: ${exception.message}")
            }

            // 4. Tüm güncellemeleri tek seferde state'e ata
            _uiState.value = updatedState.copy(isLoading = false)

            // 5. Eğer bu ekran bir deneme detayı sayfasından açıldıysa (examId dolu geldiyse),
            // o denemeyi otomatik olarak seç ve sıralamasını getir.
            initialExamId?.let { examId ->
                allExams.find { it.id == examId }?.let {
                    onEvent(LeaderboardEvent.OnExamSelected(it))
                }
            }
        }
    }

    fun onEvent(event: LeaderboardEvent) {
        when(event) {
            // Arama çubuğu metni değiştiğinde
            is LeaderboardEvent.OnExamSearchQueryChanged -> {
                val query = event.query
                _uiState.update { it.copy(examSearchQuery = query) }

                // Arama mantığı
                _uiState.update {
                    it.copy(
                        filteredExams = if (query.isBlank()) {
                            allExams // Arama boşsa tümünü göster
                        } else {
                            allExams.filter { exam ->
                                exam.name.contains(query, ignoreCase = true) ||
                                        exam.publisherName.contains(query, ignoreCase = true)
                            }
                        }
                    )
                }
            }
            // Arama sonucundan bir deneme seçildiğinde
            is LeaderboardEvent.OnExamSelected -> {
                _uiState.update { it.copy(
                    selectedExam = event.exam,
                    examSearchQuery = "", // Arama çubuğunu temizle
                    filteredExams = emptyList() // Arama sonuç listesini gizle
                )}
                fetchLeaderboard() // Sıralamayı getir
            }
            // Kapsam (Türkiye, İl, İlçe) değiştiğinde
            is LeaderboardEvent.OnScopeChanged -> {
                _uiState.update { it.copy(selectedScope = event.scope) }
                fetchLeaderboard() // Sıralamayı yeniden getir
            }
            // Şehir değiştiğinde
            is LeaderboardEvent.OnCitySelected -> {
                // DEĞİŞİKLİK: Artık state'den okuyoruz
                val districts = getDistrictsForCity(event.city, _uiState.value.allProvincesData)
                _uiState.update { it.copy(
                    selectedCity = event.city,
                    selectedDistrict = "",
                    districts = districts
                )}
                if (_uiState.value.selectedScope == "İl") { fetchLeaderboard() }
            }
            // İlçe değiştiğinde
            is LeaderboardEvent.OnDistrictSelected -> {
                _uiState.update { it.copy(selectedDistrict = event.district) }
                // Kapsam "İlçe" ise sıralamayı yenile
                if (_uiState.value.selectedScope == "İlçe") {
                    fetchLeaderboard()
                }
            }
        }
    }

    private fun fetchLeaderboard() {
        val state = _uiState.value
        val examId = state.selectedExam?.id ?: return // Seçili deneme yoksa çık
        if (studentId == null) return // Öğrenci ID'si yoksa çık

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Hem Top 50'yi hem de benim skorunu aynı anda çek
            val leaderboardDeferred = async {
                examRepository.getLeaderboard(
                    examId = examId,
                    scope = state.selectedScope,
                    city = state.selectedCity.ifEmpty { null },
                    district = state.selectedDistrict.ifEmpty { null },
                    limit = 50
                )
            }
            val myEntryDeferred = async { examRepository.getMyLeaderboardEntry(examId, studentId) }

            val leaderboardResult = leaderboardDeferred.await()
            val myEntryResult = myEntryDeferred.await()

            var updatedState = _uiState.value

            leaderboardResult.onSuccess { entries ->
                updatedState = updatedState.copy(leaderboardEntries = entries)
            }.onFailure { exception ->
                updatedState = updatedState.copy(errorMessage = exception.message)
            }

            myEntryResult.onSuccess { entry ->
                updatedState = updatedState.copy(myEntry = entry)
            }.onFailure { exception ->
                Log.e("LeaderboardVM", "Kullanıcı skoru çekilemedi: ${exception.message}")
            }

            _uiState.update { updatedState.copy(isLoading = false) }
        }
    }

    private fun getDistrictsForCity(cityName: String, allProvinces: List<Province>): List<String> {
        return allProvinces
            .find { it.name == cityName }
            ?.districts
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }
}