package com.anlarsinsoftware.denecoz.View.StudentView

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Student.QuestionDetail
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.AnalysisDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisDetailsScreen(
    navController: NavController,
    viewModel: AnalysisDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.topicName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    QuestionCategorySection(
                        "Doğru Cevaplar (${uiState.correctQuestions.size})",
                        uiState.correctQuestions,
                        Color.Green.copy(alpha = 0.2f),
                        showCorrectAnswer = false
                    )
                }
                item {
                    QuestionCategorySection(
                        "Yanlış Cevaplar (${uiState.incorrectQuestions.size})",
                        uiState.incorrectQuestions,
                        Color.Red.copy(alpha = 0.2f),
                        showCorrectAnswer = true
                    )
                }
                item {
                    QuestionCategorySection(
                        "Boş Bırakılanlar (${uiState.emptyQuestions.size})",
                        uiState.emptyQuestions,
                        Color.LightGray.copy(alpha = 0.4f),
                        showCorrectAnswer = true
                    )
                }

                item {
                    SuccessProgressBar(
                        title = "Konu Bazında Başarı",
                        rate = uiState.topicSuccessRate,
                        summaryText = "Toplam ${uiState.totalTopicQuestions} sorudan ${uiState.correctQuestions.size} tanesini doğru yaptınız."
                    )
                }

                item {
                    // Sadece veri yüklendiyse ve en az bir soru çözülmüşse göster
                    uiState.historicalPerformance?.let { performance ->
                        if (performance.totalQuestions > 0) {
                            val overallRate = performance.totalCorrect.toFloat() / performance.totalQuestions
                            SuccessProgressBar(
                                title = "Genel Toplam",
                                rate = overallRate,
                                summaryText = "Bu konuda şimdiye kadar çözdüğün ${performance.totalQuestions} sorudan ${performance.totalCorrect} tanesini doğru yaptın."
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessProgressBar(title: String, rate: Float, summaryText: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Başarı Oranı", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(
                        text = "${(rate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { rate },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Text(summaryText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionCategorySection(
    title: String,
    questions: List<QuestionDetail>, // Artık QuestionDetail listesi alıyor
    color: Color,
    showCorrectAnswer: Boolean // Yeni parametre
) {
    if (questions.isNotEmpty()) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = color)) {
                FlowRow(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    questions.sortedBy { it.number }.forEach { detail ->
                        // GÜNCELLEME: Artık basit bir Box yerine yeni QuestionChip'i kullanıyoruz
                        QuestionChip(
                            detail = detail,
                            showCorrectAnswer = showCorrectAnswer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionChip(detail: QuestionDetail, showCorrectAnswer: Boolean) {
    if (showCorrectAnswer) {
        OutlinedCard(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("${detail.number}.", fontWeight = FontWeight.Bold)
                Text("Doğru: ${detail.correctAnswer}", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
    // Sadece numarayı göstereceksek, eski dairesel tasarım yeterli.
    else {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(detail.number.toString(), fontWeight = FontWeight.SemiBold)
        }
    }
}