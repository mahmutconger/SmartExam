package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.ProfileUiState
import com.anlarsinsoftware.denecoz.Repository.AuthRepository // Veya ExamRepository, hangisinde ise
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
class ProfileViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        val studentId = auth.currentUser?.uid
        if (studentId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Kullanıcı bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Kullanıcı profili (en iyi skorlarla birlikte) ve geçmiş denemeleri
            // aynı anda, paralel olarak çekiyoruz.
            val profileDeferred = async { examRepository.getUserProfile(studentId) }
            val pastAttemptsDeferred = async { examRepository.getPastAttempts(studentId) }

            val profileResult = profileDeferred.await()
            val pastAttemptsResult = pastAttemptsDeferred.await()

            // Artık tek bir state güncellemesi ile her şeyi ayarlayabiliriz
            var finalState = _uiState.value.copy(isLoading = false)

            profileResult.onSuccess { userProfile ->
                finalState = finalState.copy(userProfile = userProfile)
            }.onFailure { exception ->
                finalState = finalState.copy(errorMessage = exception.message)
            }

            pastAttemptsResult.onSuccess { attempts ->
                finalState = finalState.copy(
                    pastAttempts = attempts,
                    totalAttemptsCount = attempts.size
                )
            }.onFailure { exception ->
                finalState = finalState.copy(errorMessage = exception.message)
            }

            _uiState.value = finalState
        }
    }
}