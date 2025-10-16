package com.anlarsinsoftware.denecoz.View.PublisherView

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
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherLoginNavigationEvent
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AuthMode
import com.anlarsinsoftware.denecoz.View.Common.AuthScreenContent
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.PublisherLoginViewModel

@Composable
fun PublisherLoginScreen(
    navController: NavController,
    viewModel: PublisherLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is PublisherLoginNavigationEvent.NavigateToPubHome -> {
                    // Giriş başarılı, yayıncı ana ekranına git ve geri tuşunu engelle
                    navController.navigate(Screen.PubHomeScreen.route) {
                        popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                    }
                }
                is PublisherLoginNavigationEvent.NavigateToRegister -> {
                    // "Kayıt Ol" metnine tıklandı, yayıncı kayıt ekranına git
                    navController.navigate(Screen.PublisherRegisterScreen.route)
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // "Aptal" bileşenimizi, GİRİŞ modu ve doğru ViewModel ile çağırıyoruz
            AuthScreenContent(
                uiState = uiState,
                mode = AuthMode.LOGIN,
                onEvent = viewModel::onEvent
            )
        }
    }
}