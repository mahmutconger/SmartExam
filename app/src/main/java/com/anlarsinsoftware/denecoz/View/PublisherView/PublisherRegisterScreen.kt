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
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherRegisterNavigationEvent
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AuthMode
import com.anlarsinsoftware.denecoz.View.Common.AuthScreenContent
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.PublisherRegisterViewModel

@Composable
fun PublisherRegisterScreen(
    navController: NavController,
    viewModel: PublisherRegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // İstenen Detay: Navigasyon olaylarını dinle
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is PublisherRegisterNavigationEvent.NavigateToPubHome -> {
                    // Kayıt başarılı, yayıncı ana ekranına git ve geri tuşunu engelle
                    navController.navigate(Screen.PubHomeScreen.route) {
                        popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                    }
                }
                is PublisherRegisterNavigationEvent.NavigateToLogin -> {
                    // "Giriş Yap" metnine tıklandı, yayıncı giriş ekranına git
                    navController.navigate(Screen.PublisherLoginScreen.route)
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // Ortak UI bileşenimizi, yayıncıya özel verilerle çağırıyoruz
            AuthScreenContent(
                uiState = uiState,
                mode = AuthMode.REGISTER,
                onEvent = viewModel::onEvent,
                nameLabel = "Yayınevi Adı" // İşte esnekliğin güzelliği!
            )
        }
    }
}