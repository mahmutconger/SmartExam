package com.anlarsinsoftware.denecoz.View.StudentView

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Student.SubjectResult
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.ResultsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    navController: NavController,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Deneme Sonucun") })
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Genel Sonuç Özeti
                item {
                    OverallSummaryCard(
                        examName = uiState.examName,
                        correct = uiState.overallCorrect,
                        incorrect = uiState.overallIncorrect,
                        empty = uiState.overallEmpty,
                        net = uiState.overallNet
                    )
                }

                // 2. Ders Sonuçları
                items(uiState.subjectResults) { subjectResult ->
                    SubjectResultCard(result = subjectResult)
                }

                // 3. Ana Sayfaya Dön Butonu
                item {
                    Button(
                        onClick = { /* TODO: Ana sayfaya dön */ },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Harika! Ana Sayfaya Dön")
                    }
                }
            }
        }
    }
}

// --- YARDIMCI COMPOSABLE'LAR (Bu fonksiyonları dosyanın en altına ekle) ---

@Composable
private fun OverallSummaryCard(examName: String, correct: Int, incorrect: Int, empty: Int, net: Double) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(examName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                SummaryItem("Doğru", correct.toString())
                SummaryItem("Yanlış", incorrect.toString())
                SummaryItem("Boş", empty.toString())
                SummaryItem("Net", String.format("%.2f", net))
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SubjectResultCard(result: SubjectResult) {
    var isExpanded by remember { mutableStateOf(false) }

    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(result.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                SummaryItem("D", result.correct.toString())
                Spacer(Modifier.width(16.dp))
                SummaryItem("Y", result.incorrect.toString())
                Spacer(Modifier.width(16.dp))
                SummaryItem("Net", String.format("%.2f", result.net))
            }

            if (isExpanded) {
                Divider(Modifier.padding(vertical = 12.dp))
                Text("Konu Analizi", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                result.topicResults.forEach { topic ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(topic.topicName, modifier = Modifier.weight(1f))
                        Text("${topic.correct}D, ${topic.incorrect}Y, ${topic.empty}B")
                    }
                }
            }
        }
    }
}