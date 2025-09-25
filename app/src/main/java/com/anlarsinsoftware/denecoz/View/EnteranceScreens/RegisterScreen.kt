package com.anlarsinsoftware.denecoz.View.EnteranceScreens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.EnteranceViewModels.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val registerState by viewModel.registerState.collectAsState()

    AuthScreen(
        title = stringResource(id = R.string.register_title),
        emailValue = email,
        onEmailChange = { email = it },
        passwordValue = password,
        onPasswordChange = { password = it },
        buttonText = stringResource(id = R.string.register_button),
        bottomText = stringResource(id = R.string.have_account),
        bottomActionText = stringResource(id = R.string.login_here),
        onBottomActionClick = {
            navController.navigate(Screen.LoginScreen.route)
        },
        onContinueClick = {
            viewModel.registerUser(email, password)
        },
        showForgotPassword = false
    )

    LaunchedEffect(registerState) {
        registerState?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "Kayıt başarılı! Anasayfaya yönlendiriliyorsunuz.", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Kayıt başarısız oldu."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}