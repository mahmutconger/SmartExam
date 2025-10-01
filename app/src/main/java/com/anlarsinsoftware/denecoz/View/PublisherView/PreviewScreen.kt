package com.anlarsinsoftware.denecoz.View.PublisherView

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.PreviewEvent
import com.anlarsinsoftware.denecoz.Model.State.PreviewNavigationEvent
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.PreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    navController: NavController,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is PreviewNavigationEvent.NavigateToPublisherHome -> {
                    Toast.makeText(context, "Deneme başarıyla yayınlandı!", Toast.LENGTH_LONG).show()
                    navController.navigate(Screen.PubHomeScreen.route) {
                        popUpTo(Screen.PubHomeScreen.route) { inclusive = true }
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Önizleme ve Onay") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.examDetails == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        PreviewSection(title = "Genel Bilgiler") {
                            Text("Deneme Adı: ${uiState.examDetails?.name ?: "..."}", fontWeight = FontWeight.SemiBold)
                            // Diğer genel bilgileri de buraya ekleyebilirsiniz...
                        }
                    }
                    item {
                        PreviewSection(title = "Cevap Anahtarı") {
                            val sortedKeys = uiState.answerKey.keys.sortedBy { it.toInt() }
                            sortedKeys.forEach { key ->
                                Text("$key - ${uiState.answerKey[key]}")
                            }
                        }
                    }

                    // TODO: Konu Dağılımı için de benzer bir bölüm eklenebilir.
                }
                Button(
                    onClick = { viewModel.onEvent(PreviewEvent.OnPublishClicked) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Denemeyi Yayınla")
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}