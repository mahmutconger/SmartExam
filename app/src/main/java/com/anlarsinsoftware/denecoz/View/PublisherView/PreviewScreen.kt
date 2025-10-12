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
                val groupedQuestions = remember(uiState.examDetails, uiState.topicDistribution) {
                    val questionMap = mutableMapOf<String, MutableList<Pair<Int, String>>>()
                    var questionCounter = 1
                    uiState.examDetails?.subjects?.forEach { test ->
                        val testName = test["testName"] as? String ?: "Bilinmeyen Test"
                        val questionCount = (test["questionCount"] as? Long)?.toInt() ?: 0

                        val questionsForTest = mutableListOf<Pair<Int, String>>()
                        for (i in questionCounter until questionCounter + questionCount) {
                            uiState.topicDistribution[i.toString()]?.let { topic ->
                                questionsForTest.add(Pair(i, topic))
                            }
                        }
                        questionMap[testName] = questionsForTest
                        questionCounter += questionCount
                    }
                    questionMap
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    // 1. Genel Bilgiler Bölümü (Mevcut kod)
                    item {
                        PreviewSection(title = "Genel Bilgiler") {
                            Text("Deneme Adı: ${uiState.examDetails?.name ?: "..."}", fontWeight = FontWeight.SemiBold)
                            Text("Deneme Türü: ${uiState.examDetails?.examType ?: "..."}")
                            InfoRow("Kitapçık Türü", uiState.examDetails?.bookletType)
                            InfoRow("Yayın Tarihi", uiState.examDetails?.publicationDate.toFormattedDate())
                        }
                    }

                    // 2. Cevap Anahtarı Bölümü (Mevcut kod)
                    item {
                        PreviewSection(title = "Cevap Anahtarı") {
                            // Cevapları 2 veya 3 sütunlu bir grid'de göstermek daha okunaklı olabilir.
                            // Örnek olarak basit bir liste:
                            val sortedKeys = uiState.answerKey.keys.sortedBy { it.toInt() }
                            sortedKeys.forEach { key ->
                                Text("$key. Soru - ${uiState.answerKey[key]}")
                            }
                        }
                    }

                    item {
                        PreviewSection(title = "Konu Dağılımı") {
                            if (groupedQuestions.isEmpty()) {
                                Text("Konu dağılımı verisi bulunamadı.")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    groupedQuestions.forEach { (testName, questions) ->
                                        // Ders başlığı
                                        Text(
                                            text = testName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        // O derse ait sorular ve konular
                                        Column(
                                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            questions.forEach { (qNumber, topic) ->
                                                Row {
                                                    Text(
                                                        text = "$qNumber. Soru: ",
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.width(80.dp)
                                                    )
                                                    Text(text = topic)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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