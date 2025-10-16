package com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherRegisterNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherRegisterUiState
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
class PublisherRegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublisherRegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<PublisherRegisterNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnEmailChanged -> _uiState.update { it.copy(email = event.email) }
            is AuthEvent.OnPasswordChanged -> _uiState.update { it.copy(password = event.pass) }
            is AuthEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) } // Yayınevi adını günceller
            is AuthEvent.OnPrimaryButtonClicked -> registerPublisher() // "Kayıt Ol" tıklandı
            is AuthEvent.OnSecondaryTextClicked -> viewModelScope.launch {
                _navigationEvent.emit(PublisherRegisterNavigationEvent.NavigateToLogin) // "Giriş Yap" tıklandı
            }
        }
    }

    private fun registerPublisher() {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Tüm alanlar doldurulmalıdır.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.registerPublisher(state.email, state.password, state.name)
                .onSuccess {
                    // Başarılı kayıttan sonra yayıncı ana ekranına git
                    _navigationEvent.emit(PublisherRegisterNavigationEvent.NavigateToPubHome)
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }
}