package com.anlarsinsoftware.denecoz.View.PublisherView

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditPublisherProfileEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditPublisherProfileNavigationEvent
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.EditPublisherProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublisherProfileScreen(
    navController: NavController,
    viewModel: EditPublisherProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Fotoğraf seçiciyi başlat ve sonucu ViewModel'a gönder
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            viewModel.onEvent(EditPublisherProfileEvent.OnLogoSelected(uri))
        }
    )

    // Navigasyon ve hata olaylarını dinle
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is EditPublisherProfileNavigationEvent.NavigateBack -> {
                    // Kaydetme başarılı, bir önceki ekrana dön
                    navController.popBackStack()
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profili Düzenle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        // Ekran ilk yüklendiğinde tam ekran yükleme göstergesi
        if (uiState.isLoading && uiState.name.isBlank()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // Logo Seçici
                Box(
                    modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    AsyncImage(
                        model = uiState.newLogoUri ?: uiState.logoUrl,
                        placeholder = painterResource(id = R.drawable.profile_placeholder),
                        error = painterResource(id = R.drawable.profile_placeholder),
                        contentDescription = "Yayınevi Logosu",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    // Düzenleme ikonu
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Logoyu Değiştir",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(8.dp)
                    )
                }
                TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Logoyu Değiştir")
                }

                Spacer(Modifier.height(32.dp))

                // Yayınevi Adı TextField'ı
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onEvent(EditPublisherProfileEvent.OnNameChanged(it)) },
                    label = { Text("Yayınevi Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(32.dp))

                // Kaydet Butonu
                Button(
                    onClick = { viewModel.onEvent(EditPublisherProfileEvent.OnSaveClicked) },
                    enabled = uiState.isSaveButtonEnabled && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    // Yükleme sırasında buton içinde progress indicator göster
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Değişiklikleri Kaydet")
                    }
                }
            }
        }
    }
}