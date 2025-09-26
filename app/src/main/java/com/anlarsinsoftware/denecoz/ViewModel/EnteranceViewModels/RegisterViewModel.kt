package com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.denecoz.Model.UserRole
import com.anlarsinsoftware.denecoz.Repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<Result<FirebaseUser>?>(null)
    val registerState: StateFlow<Result<FirebaseUser>?> = _registerState

    fun registerUser(email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            try {
                val firebaseUser = repository.registerUser(email, password, role)
                _registerState.value = Result.success(firebaseUser)
            } catch (e: Exception) {
                _registerState.value = Result.failure(e)
            }
        }
    }
}