package com.anlarsinsoftware.denecoz.View.EnteranceScreens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val loginState by viewModel.loginState.collectAsState()

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
            viewModel.loginUser(email, password)
        },
        showForgotPassword = true,
        onForgotPasswordClick = { /* TODO: Şifremi Unuttum mantığı eklenecek */ }
    )

    LaunchedEffect(loginState) {
        loginState?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Giriş başarısız oldu."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}