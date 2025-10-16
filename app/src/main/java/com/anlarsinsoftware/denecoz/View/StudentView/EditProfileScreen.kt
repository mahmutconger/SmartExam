package com.anlarsinsoftware.denecoz.View.StudentView

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileEvent
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.EditProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Fotoğraf seçiciyi başlat
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            viewModel.onEvent(EditProfileEvent.OnPhotoSelected(uri))
        }
    )

    // TODO: Navigasyon ve hata yönetimi için LaunchedEffect'ler eklenecek

    Scaffold(
        topBar = { /* ... "Profili Düzenle" başlıklı bir TopAppBar ... */ }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Profil Fotoğrafı Alanı
            Box {
                AsyncImage(
                    model = uiState.newProfilePhotoUri ?: uiState.profileImageUrl,
                    // placeholder = ...,
                    contentDescription = "Profil Resmi",
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Fotoğrafı Değiştir")
            }

            Spacer(Modifier.height(24.dp))

            // Form Alanları
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(EditProfileEvent.OnNameChanged(it)) },
                label = { Text("İsim Soyisim") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // TODO: Eğitim Seviyesi, Şehir, İlçe, Okul için ExposedDropdownMenuBox'lar
            // Bunlar GeneralInfoScreen'deki gibi olacak, ancak birbirlerine bağlı çalışacaklar.

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onEvent(EditProfileEvent.OnSaveClicked) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isLoading) CircularProgressIndicator() else Text("Değişiklikleri Kaydet")
            }
        }
    }
}