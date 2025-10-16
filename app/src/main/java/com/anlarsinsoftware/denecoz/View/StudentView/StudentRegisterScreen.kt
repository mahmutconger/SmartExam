package com.anlarsinsoftware.denecoz.View.StudentView

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Student.StudentRegisterNavigationEvent
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AuthMode
import com.anlarsinsoftware.denecoz.View.Common.AuthScreenContent
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.StudentRegisterViewModel

@Composable
fun StudentRegisterScreen(
    navController: NavController,
    viewModel: StudentRegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is StudentRegisterNavigationEvent.NavigateToHome -> {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                    }
                }
                is StudentRegisterNavigationEvent.NavigateToLogin -> {
                    navController.navigate(Screen.StudentLoginScreen.route)
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AuthScreenContent(
                uiState = uiState,
                mode = AuthMode.REGISTER,
                onEvent = viewModel::onEvent,
                nameLabel = "İsim Soyisim"
            )
        }
    }
}

