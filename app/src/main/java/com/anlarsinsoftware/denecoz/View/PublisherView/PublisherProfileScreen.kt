package com.anlarsinsoftware.denecoz.View.PublisherView

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherProfile
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherProfileEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherProfileNavigationEvent
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.PublisherProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublisherProfileScreen(
    navController: NavController,
    viewModel: PublisherProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Navigasyon olaylarını dinle
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is PublisherProfileNavigationEvent.NavigateToEditProfile -> {
                    navController.navigate(Screen.EditPublisherProfileScreen.route)
                }

                is PublisherProfileNavigationEvent.NavigateToWelcome -> {
                    // Çıkış yapıldı, tüm geçmişi temizleyerek karşılama ekranına dön
                    navController.navigate(Screen.WelcomeScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
        }
    }

    // Hata mesajlarını göster
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Çıkış yapma onay diyaloğunu göster
    if (uiState.showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = { viewModel.onEvent(PublisherProfileEvent.OnConfirmLogout) },
            onDismiss = { viewModel.onEvent(PublisherProfileEvent.OnDismissLogoutDialog) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Yayıncı Profili") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Ayarlar ekranına git */ }) {
                        Icon(Icons.Default.Settings, "Ayarlar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.profile != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { Spacer(Modifier.height(16.dp)) }

                // 1. Profil Başlığı
                item {
                    PublisherProfileHeader(
                        profile = uiState.profile!!,
                        onEditClick = { viewModel.onEvent(PublisherProfileEvent.OnEditProfileClicked) }
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }

                // 2. DOĞRULAMA DURUMU KARTI
                item {
                    if (uiState.profile?.verificationStatus == "PENDING") {
                        VerificationStatusCard(isPending = true)
                    } else if (uiState.profile?.verificationStatus == "APPROVED") {
                        VerificationStatusCard(isPending = false)
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }

                // 3. Çıkış Yap Butonu
                item {
                    TextButton(onClick = { viewModel.onEvent(PublisherProfileEvent.OnLogoutClicked) }) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Çıkış Yap")
                    }
                }
            }
        }
    }
}

@Composable
private fun PublisherProfileHeader(profile: PublisherProfile, onEditClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable(onClick = onEditClick)
        ) {
            AsyncImage(
                model = profile.logoUrl,
                placeholder = painterResource(id = R.drawable.profile_placeholder),
                error = painterResource(id = R.drawable.profile_placeholder),
                contentDescription = "Yayınevi Logosu",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Profili Düzenle",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            profile.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(profile.email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}

@Composable

private fun VerificationStatusCard(isPending: Boolean) {
    val backgroundColor = if (isPending) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
    val contentColor = if (isPending) Color(0xFFE65100) else Color(0xFF2E7D32)
    val icon = if (isPending) Icons.Default.HourglassTop else Icons.Default.CheckCircle
    val title = if (isPending) "Hesabınız İnceleniyor" else "Hesabınız Onaylandı"
    val text = if (isPending) "Onay süreci tamamlandığında deneme yayınlamaya başlayabilirsiniz."
    else "Artık deneme yayınlayabilir ve binlerce öğrenciye ulaşabilirsiniz."

    Card(colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = contentColor)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Çıkış Yap") },
        text = { Text("Oturumu sonlandırmak istediğinizden emin misiniz?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Evet, Çıkış Yap")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}