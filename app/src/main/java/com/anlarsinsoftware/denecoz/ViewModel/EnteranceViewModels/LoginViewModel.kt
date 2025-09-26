package com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels

import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.UserRole
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

    fun loginUser(email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            val result = repository.loginUser(email, password, role)
            _loginState.value = result
        }
    }
}