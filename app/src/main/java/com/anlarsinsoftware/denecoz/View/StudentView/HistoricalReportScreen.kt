package com.anlarsinsoftware.denecoz.View.StudentView

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Student.HistoricalReportUiState
import com.anlarsinsoftware.denecoz.Model.State.Student.SmartSubjectReport
import com.anlarsinsoftware.denecoz.Model.State.Student.SmartTopicReport
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.HistoricalReportViewModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricalReportScreen(
    navController: NavController,
    viewModel: HistoricalReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Hata mesajlarını göster
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${uiState.examType} Akıllı Karne") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.subjectReports.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("Gösterilecek rapor verisi bulunamadı.", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Genel Durum Kartları (Accuracy)
                item { OverallSummaryCard(uiState) }

                // 2. Ders Performansı (Doughnut Chart)
                item { SubjectPerformanceCard(uiState) }

                // 3. Konu Hakimiyeti (Bar Charts)
                item { TopicMasteryCard(uiState) }

                // 4. Anahtar İçgörüler (En Zayıf Konular vb.)
                item { KeyInsightsCard(uiState) }

                // 5. Soru Analizi (Şimdilik yer tutucu, veri yok)
                item { QuestionAnalysisPlaceholder() }
            }
        }
    }
}

// --- YARDIMCI COMPOSABLE'LAR ---

@Composable
fun OverallSummaryCard(uiState: HistoricalReportUiState) {
    val totalCorrect = uiState.subjectReports.sumOf { subject -> subject.topicReports.sumOf { it.correct } }
    val totalIncorrect = uiState.subjectReports.sumOf { subject -> subject.topicReports.sumOf { it.incorrect } }
    val totalEmpty = uiState.subjectReports.sumOf { subject -> subject.topicReports.sumOf { it.empty } }
    val totalQuestions = totalCorrect + totalIncorrect + totalEmpty

    val accuracy = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions) * 100 else 0f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Genel Durum", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accuracy Kartı
                SummaryItem(
                    title = "Doğruluk Oranı",
                    value = "${"%.1f".format(accuracy)}%",
                    description = "Toplam ${totalQuestions} soruda",
                    color = MaterialTheme.colorScheme.primary
                )
                // Çözülen Toplam Soru Sayısı Kartı
                SummaryItem(
                    title = "Çözülen Toplam Soru",
                    value = totalQuestions.toString(),
                    description = "${totalCorrect} Doğru, ${totalIncorrect} Yanlış",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, value: String, description: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SubjectPerformanceCard(uiState: HistoricalReportUiState) {
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        Color(0xFFFDD835), // Sarı
        Color(0xFF4CAF50)  // Yeşil
    )

    // Ders bazında toplam netleri hesapla ve Vico formatına dönüştür
    val subjectData = uiState.subjectReports.mapIndexed { index, subjectReport ->
        val totalNetForSubject = subjectReport.topicReports.sumOf { (it.correct * 1.0 - it.incorrect * 0.25) }.toFloat() // Net hesapla
        FloatEntry(index.toFloat(), totalNetForSubject)
    }

    LaunchedEffect(subjectData) {
        chartEntryModelProducer.setEntries(subjectData)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ders Performansı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (subjectData.sumOf { it.y.toDouble() } == 0.0) { // Toplam net 0 ise grafik çizme
                Text("Henüz ders performansı verisi bulunmuyor.", Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pasta/Halka Grafik
//                    ProvideChartStyle() {
//                        Chart(
//                            chart = pieChart(
//                                slices = subjectData.mapIndexed { index, _ ->
//                                    rememberPieChartSlice(
//                                        color = colors.getOrElse(index) { Color.Gray }, // Renk döngüsü
//                                        value = subjectData[index].y
//                                    )
//                                }
//                            ),
//                            chartModelProducer = chartEntryModelProducer,
//                            modifier = Modifier.weight(0.7f).fillMaxHeight()
//                        )
//                    }

                    // Gösterge (Legend)
                    Column(
                        modifier = Modifier.weight(0.3f).fillMaxHeight().padding(start = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        uiState.subjectReports.forEachIndexed { index, subjectReport ->
                            val totalNetForSubject = subjectReport.topicReports.sumOf { (it.correct * 1.0 - it.incorrect * 0.25) }
                            if (totalNetForSubject > 0) { // Sadece neti olan dersleri göstergede göster
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(colors.getOrElse(index) { Color.Gray })
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(subjectReport.subjectName, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicMasteryCard(uiState: HistoricalReportUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Konu Hakimiyeti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            uiState.subjectReports.forEach { subjectReport ->
                Text(subjectReport.subjectName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (subjectReport.topicReports.any { it.total > 0 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjectReport.topicReports.filter { it.total > 0 }.forEach { topic -> // Sadece çözülen konuları göster
                            TopicMasteryBar(topic = topic)
                        }
                    }
                } else {
                    Text("Bu dersten çözülmüş konu yok.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TopicMasteryBar(topic: SmartTopicReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = topic.topicName,
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = topic.successRate,
            modifier = Modifier.weight(0.5f).height(8.dp).clip(MaterialTheme.shapes.small),
            color = topic.feedbackColor,
            trackColor = topic.feedbackColor.copy(alpha = 0.3f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${"%.0f".format(topic.successRate * 100)}%",
            modifier = Modifier.weight(0.1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun KeyInsightsCard(uiState: HistoricalReportUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Anahtar İçgörüler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            val allTopics = uiState.subjectReports.flatMap { it.topicReports }.filter { it.total > 0 }

            // En zayıf 3 konu
            val weakestTopics = allTopics
                .sortedBy { it.successRate }
                .take(3)

            if (weakestTopics.isNotEmpty()) {
                Text("En çok tekrar etmen gereken konular:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                weakestTopics.forEach { topic ->
                    Text("• ${topic.topicName} (${"%.0f".format(topic.successRate * 100)}% Başarı)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }
            } else {
                Text("Henüz yeterli veri yok, daha çok soru çözerek içgörü kazanabilirsin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // En başarılı 3 konu (istersen eklersin)
            val strongestTopics = allTopics
                .sortedByDescending { it.successRate }
                .take(3)

            if (strongestTopics.isNotEmpty()) {
                Text("En başarılı olduğun konular:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                strongestTopics.forEach { topic ->
                    Text("• ${topic.topicName} (${"%.0f".format(topic.successRate * 100)}% Başarı)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun QuestionAnalysisPlaceholder() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Soru Analizi (Geliştirme Aşamasında)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Çözülen soruların detaylı analizleri ve yanlış yapılan soru tipleri bu bölümde yer alacaktır. " +
                        "Şu anki veri yapımız bu detayı sağlamıyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}