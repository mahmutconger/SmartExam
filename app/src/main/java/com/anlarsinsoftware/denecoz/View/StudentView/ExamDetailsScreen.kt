package com.anlarsinsoftware.denecoz.View.StudentView

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anlarsinsoftware.denecoz.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamDetailsEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamDetailsNavigationEvent
import com.anlarsinsoftware.denecoz.R.drawable.ic_calendar
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.ExamDetailsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailsScreen(
    navController: NavController,
    viewModel: ExamDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Navigasyon ve Hata Yönetimi
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ExamDetailsNavigationEvent.NavigateToExamEntry -> {
                    navController.navigate(Screen.ExamEntryScreen.createRoute(event.examId))
                }
                is ExamDetailsNavigationEvent.NavigateToLeaderboard -> {
                    navController.navigate(Screen.LeaderboardScreen.createRoute(event.examId))
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // TODO: ViewModel'a mesajın gösterildiğini bildiren bir event eklenebilir
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.examName,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Kartları içeren kaydırılabilir alan
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // "Genel Bilgiler" Kartı
                    Text("Genel Bilgiler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    GeneralInfoCard(
                        date = uiState.publicationDate,
                        publisher = uiState.publisherName,
                        type = uiState.examType
                    )

                    // "Detaylar" Kartı
                    Text("Detaylar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DetailsCard(
                        difficulty = uiState.difficulty,
                        isSolved = uiState.isSolvedByUser,
                        solveCount = uiState.solveCount
                    )
                }

                // Alt Kısma Sabitlenmiş Butonlar
                Column(Modifier.padding(vertical = 16.dp)) {
                    Button(
                        onClick = { viewModel.onEvent(ExamDetailsEvent.OnLeaderboardClicked) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sırala Tablosunu İncele!")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.onEvent(ExamDetailsEvent.OnSolveClicked) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Denemeyi Kontrol Et!")
                    }
                }
            }
        }
    }
}


// --- YARDIMCI COMPOSABLE'LAR ---

@Composable
private fun GeneralInfoCard(date: String, publisher: String, type: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            InfoRow(icon = ic_calendar, label = "Basım Tarihi", value = date.toFormattedDate())
            InfoRow(icon = R.drawable.ic_publisher, label = "Yayınevi", value = publisher,false)
            InfoRow(icon = R.drawable.ic_book, label = "Deneme Türü", value = type,false)
            InfoRow(icon = R.drawable.ic_content, label = "İçerik", value = "Tüm Dersler")
        }
    }
}

@Composable
private fun DetailsCard(difficulty: String, isSolved: Boolean, solveCount: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            InfoRow(icon = R.drawable.ic_stairs_up, label = "Zorluk Seviyesi", value = difficulty)
            InfoRow(icon = R.drawable.ic_check_circle, label = "Çözülme Durumu", value = if (isSolved) "Çözüldü" else "Çözülmedi")
            InfoRow(icon = R.drawable.ic_information, label = "Çözülme Sayısı", value = solveCount.toString())
        }
    }
}

@Composable
private fun InfoRow(icon: Int, label: String, value: String,isHaveTint: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = if(isHaveTint) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// Tarih formatlama için yardımcı fonksiyon
private fun String?.toFormattedDate(): String {
    if (this.isNullOrBlank()) return "Belirtilmemiş"
    return try {
        val date = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) // yyyy-MM-dd
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("tr"))
        date.format(formatter)
    } catch (e: Exception) {
        this // Parse edilemezse orijinal halini döndür
    }
}