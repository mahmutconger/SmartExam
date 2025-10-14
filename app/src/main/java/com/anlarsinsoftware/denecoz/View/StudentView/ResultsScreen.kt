package com.anlarsinsoftware.denecoz.View.StudentView

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Student.SubjectResult
import com.anlarsinsoftware.denecoz.Screen
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
            CenterAlignedTopAppBar(title = { Text("Deneme Analizi") })
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
                item {
                    OverallSummaryCard(
                        examName = uiState.examName,
                        correct = uiState.overallCorrect,
                        incorrect = uiState.overallIncorrect,
                        empty = uiState.overallEmpty,
                        net = uiState.overallNet
                    )
                }

                // GÜNCELLENMİŞ BÖLÜM: Artık her ders için genişletilebilir bir kart listeliyoruz
                items(uiState.subjectResults, key = { it.subjectName }) { subjectResult ->
                    SubjectResultCard(
                        result = subjectResult,
                        examId = viewModel.examId, // ViewModel'dan ID'leri alıyoruz
                        attemptId = viewModel.attemptId,
                        onTopicClick = { topicName ->
                            navController.navigate(
                                Screen.AnalysisDetailsScreen.createRoute(viewModel.examId, viewModel.attemptId, topicName)
                            )
                        }
                    )
                }

                item {
                    Button(
                        onClick = {
                            navController.navigate(Screen.HomeScreen.route) {
                                popUpTo(Screen.HomeScreen.route) { inclusive = true }
                            }
                        },
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
private fun SubjectResultCard(
    result: SubjectResult,
    examId: String,
    attemptId: String,
    onTopicClick: (topicName: String) -> Unit
) {
    // Her kart kendi "genişletilme" durumunu kendisi yönetir.
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    OutlinedCard(Modifier.fillMaxWidth()) {
        Column {
            // Tıklandığında kartı genişleten/daraltan ana satır
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(result.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                SummaryItem("D", result.correct.toString())
                Spacer(Modifier.width(12.dp))
                SummaryItem("Y", result.incorrect.toString())
                Spacer(Modifier.width(12.dp))
                SummaryItem("Net", String.format("%.2f", result.net))
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Genişlet/Daralt",
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            // Sadece isExpanded true ise görünen, animasyonlu bölüm
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider()
                    Spacer(Modifier.height(12.dp))
                    Text("Konu Analizi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    // Konuları listele
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Her konu arasına 12.dp boşluk
                    ) {
                        result.topicResults.forEach { topic ->
                            // Her konu satırını tıklanabilir yap
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTopicClick(topic.topicName) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(topic.topicName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Text("${topic.correct}D, ${topic.incorrect}Y, ${topic.empty}B", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Detay", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}