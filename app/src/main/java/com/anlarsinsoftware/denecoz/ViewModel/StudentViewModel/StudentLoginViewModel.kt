package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.StudentLoginNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.StudentLoginUiState
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import com.anlarsinsoftware.denecoz.View.Common.AuthEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentLoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<StudentLoginNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnEmailChanged -> {
                _uiState.update { it.copy(email = event.email) }
            }
            is AuthEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(password = event.pass) }
            }
            is AuthEvent.OnPrimaryButtonClicked -> {
                // "Giriş Yap" butonuna basıldı
                loginUser()
            }
            is AuthEvent.OnSecondaryTextClicked -> {
                // "Hesabın yok mu? Kayıt Ol!" metnine tıklandı
                viewModelScope.launch {
                    _navigationEvent.emit(StudentLoginNavigationEvent.NavigateToRegister)
                }
            }
            is AuthEvent.OnNameChanged -> {
                // Giriş ekranında isim alanı olmadığı için bu olay göz ardı edilir.
            }
        }
    }

    private fun loginUser() {
        val state = _uiState.value

        // Basit girdi kontrolü
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "E-posta ve şifre alanları boş bırakılamaz.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Repository'deki ÖĞRENCİ'ye özel giriş fonksiyonunu çağırıyoruz
            authRepository.loginStudent(state.email, state.password)
                .onSuccess {
                    // Başarılı olursa, Ana Ekrana gitme komutu gönder
                    _navigationEvent.emit(StudentLoginNavigationEvent.NavigateToHome)
                }
                .onFailure { exception ->
                    // Başarısız olursa, yüklemeyi durdur ve hata mesajını göster
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
        }
    }
}