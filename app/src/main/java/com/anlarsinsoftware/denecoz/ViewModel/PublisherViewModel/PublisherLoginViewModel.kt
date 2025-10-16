package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherLoginNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherLoginUiState
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
class PublisherLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublisherLoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PublisherLoginNavigationEvent>()
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
                loginPublisher()
            }
            is AuthEvent.OnSecondaryTextClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(PublisherLoginNavigationEvent.NavigateToRegister)
                }
            }
            is AuthEvent.OnNameChanged -> {
                // Giriş ekranında bu olay göz ardı edilir.
            }
        }
    }

    private fun loginPublisher() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "E-posta ve şifre alanları boş bırakılamaz.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // KRİTİK FARK: Repository'deki YAYINCI'ya özel giriş fonksiyonunu çağırıyoruz
            authRepository.loginPublisher(state.email, state.password)
                .onSuccess {
                    _navigationEvent.emit(PublisherLoginNavigationEvent.NavigateToPubHome)
                }
                .onFailure { exception ->
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