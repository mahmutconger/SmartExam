package com.anlarsinsoftware.denecoz.View.EnteranceScreens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.UserRole
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels.AuthUiState
import com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    val context = LocalContext.current

    val loginState by viewModel.loginState.collectAsState()
    val isLoading = loginState is AuthUiState.Loading

    // Eğer yükleniyorsa ekranda bir Progress Bar göster (isteğe bağlı ama şık)
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }


    AuthScreen(
        title = stringResource(id = R.string.login_title),
        emailValue = email,
        onEmailChange = { email = it },
        passwordValue = password,
        onPasswordChange = { password = it },
        buttonText = stringResource(id = R.string.continuee),
        bottomText = stringResource(id = R.string.no_account),
        bottomActionText = stringResource(id = R.string.sign_up),
        onBottomActionClick = {
            navController.navigate(Screen.RegisterScreen.route)
        },
        onContinueClick = {
            selectedRole?.let { role ->
                viewModel.loginUser(email, password, role)
            } ?: Toast.makeText(context, "Lütfen bir rol seçin.", Toast.LENGTH_SHORT).show()
        },
        showForgotPassword = true,
        onForgotPasswordClick = { /* TODO: Şifremi Unuttum mantığı eklenecek */ },
        userRole = selectedRole,
        onRoleChange = { selectedRole = it },
        showRoleSelection = true,

    )

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, "Giriş başarılı!", Toast.LENGTH_SHORT).show()

                val destination = when (selectedRole) {
                    UserRole.STUDENT -> Screen.HomeScreen.route
                    UserRole.PUBLISHER -> Screen.PubHomeScreen.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }
}