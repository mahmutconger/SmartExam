package com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.State.Student.StudentRegisterNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.StudentRegisterUiState
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
class StudentRegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentRegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<StudentRegisterNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnNameChanged -> _uiState.update { it.copy(name = event.name) }
            is AuthEvent.OnEmailChanged -> _uiState.update { it.copy(email = event.email) }
            is AuthEvent.OnPasswordChanged -> _uiState.update { it.copy(password = event.pass) }
            is AuthEvent.OnPrimaryButtonClicked -> registerUser()
            is AuthEvent.OnSecondaryTextClicked -> viewModelScope.launch {
                _navigationEvent.emit(StudentRegisterNavigationEvent.NavigateToLogin)
            }
        }
    }

    private fun registerUser() {
        val state = _uiState.value
        // TODO: Girdi kontrolleri (email formatı, şifre uzunluğu vb.)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.registerStudent(state.email, state.password, state.name)
                .onSuccess {
                    _navigationEvent.emit(StudentRegisterNavigationEvent.NavigateToHome)
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }
}