package com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<Result<Unit>?>(null)
    val registerState = _registerState.asStateFlow()

    fun registerUser(email: String, pass: String) {
        viewModelScope.launch {
            val result = repository.registerUser(email, pass)
            _registerState.value = result
        }
    }
}