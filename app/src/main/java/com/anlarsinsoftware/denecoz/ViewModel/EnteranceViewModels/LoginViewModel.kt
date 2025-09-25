package com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels

import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<Unit>?>(null)
    val loginState = _loginState.asStateFlow()

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            val result = repository.loginUser(email, pass)
            _loginState.value = result
        }
    }
}