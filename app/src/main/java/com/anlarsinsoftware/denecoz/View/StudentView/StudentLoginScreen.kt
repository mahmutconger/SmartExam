package com.anlarsinsoftware.denecoz.View.StudentView

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Student.StudentLoginNavigationEvent
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AuthMode
import com.anlarsinsoftware.denecoz.View.Common.AuthScreenContent
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.StudentLoginViewModel

@Composable
fun StudentLoginScreen(
    navController: NavController,
    viewModel: StudentLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is StudentLoginNavigationEvent.NavigateToHome -> {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                    }
                }
                is StudentLoginNavigationEvent.NavigateToRegister -> {
                    navController.navigate(Screen.StudentRegisterScreen.route)
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // TODO: ViewModel'a mesajın gösterildiğini bildiren bir event eklenebilir.
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AuthScreenContent(
                uiState = uiState,
                mode = AuthMode.LOGIN,
                onEvent = viewModel::onEvent
            )
        }
    }
}