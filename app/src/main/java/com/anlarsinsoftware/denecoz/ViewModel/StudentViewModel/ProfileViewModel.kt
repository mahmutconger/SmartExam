package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.ProfileUiState
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
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
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

            // Her isteği doğru uzmana yönlendiriyoruz
            val profileDeferred = async { authRepository.getUserProfile(studentId) }
            val pastAttemptsDeferred = async { examRepository.getPastAttempts(studentId) }

            val profileResult = profileDeferred.await()
            val pastAttemptsResult = pastAttemptsDeferred.await()

            _uiState.update { currentState ->
                var updatedState = currentState.copy(isLoading = false)

                profileResult.onSuccess { userProfile ->
                    updatedState = updatedState.copy(userProfile = userProfile)
                }.onFailure { exception ->
                    updatedState = updatedState.copy(errorMessage = exception.message)
                }

                pastAttemptsResult.onSuccess { attempts ->
                    updatedState = updatedState.copy(
                        pastAttempts = attempts,
                        totalAttemptsCount = attempts.size
                    )
                }.onFailure { exception ->
                    updatedState = updatedState.copy(errorMessage = exception.message)
                }

                updatedState
            }
        }
    }
}