package com.anlarsinsoftware.denecoz.View.PublisherView

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
                        popUpTo(Screen.GeneralInfoScreen.route) { inclusive = true }
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }

    val availableBooklets = remember(uiState.answerKeys) { uiState.answerKeys.keys.sorted() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val selectedBooklet = availableBooklets.getOrNull(selectedTabIndex) ?: ""

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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                PreviewSection(title = "Genel Bilgiler") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("Deneme Adı", uiState.examDetails?.name)
                        InfoRow("Deneme Türü", uiState.examDetails?.examType)
                        InfoRow("Mevcut Kitapçıklar", availableBooklets.joinToString(", ").ifEmpty { "..." })
                        InfoRow("Yayın Tarihi", uiState.examDetails?.publicationDate.toFormattedDate())
                    }
                }

                // Kitapçıklar için Sekmeli Yapı
                if (availableBooklets.isNotEmpty()) {
                    TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.padding(top = 16.dp)) {
                        availableBooklets.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text("Kitapçık $title") }
                            )
                        }
                    }
                }

                // Seçili Kitapçığın Detayları
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // Cevap Anahtarı (Grid yapısında)
                    item {
                        val answerKeyForBooklet = uiState.answerKeys[selectedBooklet] ?: emptyMap()
                        PreviewSection(title = "Cevap Anahtarı") {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 75.dp),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                this@LazyColumn.items(answerKeyForBooklet.entries.toList().sortedBy { it.key.toInt() }) { (q, a) ->
                                    Text("$q. $a")
                                }
                            }
                        }
                    }

                    // Konu Dağılımı
                    item {
                        val topicDistForBooklet = uiState.topicDistributions[selectedBooklet] ?: emptyMap()
                        PreviewSection(title = "Konu Dağılımı") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                topicDistForBooklet.entries.toList().sortedBy { it.key.toInt() }.forEach { (q, topic) ->
                                    InfoRow("$q. Soru", topic)
                                }
                            }
                        }
                    }
                }

                // Eylem Butonları
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Düzenle") }

                    Button(
                        onClick = { viewModel.onEvent(PreviewEvent.OnPublishClicked) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Yayınla")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarih string'ini (örn: "2025-10-11") kullanıcı dostu bir formata çevirir.
 * Örnek Çıktı: "11 Ekim 2025"
 */
private fun String?.toFormattedDate(): String {
    if (this.isNullOrBlank()) return "Belirtilmemiş"
    return try {
        val date = java.time.LocalDate.parse(this)
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("dd MMMM yyyy", java.util.Locale("tr")) // Türkçe ay isimleri için
        date.format(formatter)
    } catch (e: Exception) {
        this
    }
}

/**
 * "Başlık: Değer" formatında bir satır oluşturan basit bir Composable.
 */
@Composable
private fun InfoRow(label: String, value: String?) {
    Row {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value ?: "...",
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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