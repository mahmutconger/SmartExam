package com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels

import android.util.Log
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.UserRole
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepository
import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState = _loginState.asStateFlow()

    fun loginUser(email: String, pass: String, role: UserRole) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading

            val result = repository.loginUser(email, pass, role)

            if (result.isSuccess) {
                _loginState.value = AuthUiState.Success
            } else {
                _loginState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
            }
        }
    }
    // LoginViewModel.kt içinde
    fun moveEdebiyatData() {
        viewModelScope.launch {
            Log.d("Migration", "Geometri taşıma işlemi başlıyor...")
            examRepository.moveDocument()
                .onSuccess {
                    Log.d("Migration", "Geometri taşıma işlemi BAŞARILI!")
                }
                .onFailure { exception ->
                    Log.e("Migration", "Geometri taşıma işlemi BAŞARISIZ!", exception)
                }
        }
    }
}