package com.anlarsinsoftware.denecoz.View.PublisherView

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Publisher.AnswerKeyEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.AnswerKeyNavigationEvent
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.AnswerKeyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerKeyScreen(
    navController: NavController,
    viewModel: AnswerKeyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadExamStatus()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when(event) {
                is AnswerKeyNavigationEvent.NavigateToEditor -> {
                    navController.navigate(
                        Screen.AnswerKeyEditorScreen.createRoute(event.examId, event.mode, event.bookletName)
                    )
                }
                is AnswerKeyNavigationEvent.NavigateToPreview -> {
                    navController.navigate(Screen.PreviewScreen.createRoute(event.examId))
                }
            }
        }
    }

    // Hata mesajlarını göstermek için
    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Deneme Hazırlığı") }, // Başlığı daha açıklayıcı yaptık
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F8FF) // Arka plan rengi
    ) { padding ->
        // Yükleme durumunda ortada bir progress indicator göster
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Ana içerik
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Açıklayıcı başlık ve metin
                Text("Deneme İçeriğini Tamamla", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cevap anahtarını ve konu dağılımını belirleyerek denemeyi yayına hazır hale getirin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {

                    // Mevcut kitapçıklar için döngü oluştur
                    val sortedBooklets = uiState.bookletStatus.keys.sorted()
                    items(sortedBooklets) { bookletName ->
                        val status = uiState.bookletStatus[bookletName]!!

                        // "A KİTAPÇIĞI İÇİN" gibi bir başlık
                        Text(
                            text = "$bookletName KİTAPÇIĞI İÇİN",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )

                        // Cevap Anahtarı Kartı
                        StatusCard(
                            title = "Cevap Anahtarını Belirle",
                            isCompleted = status.hasAnswerKey,
                            isEnabled = true,
                            onClick = {
                                viewModel.onEvent(AnswerKeyEvent.OnNavigateToAnswerKeyEditor(bookletName))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Konu Dağılımı Kartı
                        StatusCard(
                            title = "Konu Dağılımını Belirle",
                            isCompleted = status.hasTopicDistribution,
                            isEnabled = status.hasAnswerKey, // Sadece cevap anahtarı hazırsa aktif
                            onClick = {
                                viewModel.onEvent(AnswerKeyEvent.OnNavigateToTopicEditor(bookletName))
                            }
                        )
                    }

                    // Yeni Kitapçık Ekle Butonu
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        AddNewBookletButton(
                            onClick = { viewModel.onEvent(AnswerKeyEvent.OnAddNewBooklet) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.onEvent(AnswerKeyEvent.OnNavigateToPreview) },
                    enabled = uiState.bookletStatus.isNotEmpty() && uiState.bookletStatus.values.all {
                        it.hasAnswerKey && it.hasTopicDistribution
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Ön İzleme & Yayınla")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Adımların durumunu gösteren yeniden kullanılabilir kart bileşeni.
 * @param title Kart üzerinde yazacak başlık.
 * @param isCompleted Bu adımın tamamlanıp tamamlanmadığı.
 * @param isEnabled Bu kartın tıklanabilir olup olmadığı.
 * @param onClick Karta tıklandığında tetiklenecek eylem.
 */
@Composable
private fun StatusCard(
    title: String,
    isCompleted: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCompleted -> Color(0xFFE8F5E9)
        isEnabled -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.LightGray.copy(alpha = 0.3f)
    }
    val contentColor = when {
        isCompleted -> Color(0xFF2E7D32)
        isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.Gray.copy(alpha = 0.7f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEnabled && !isCompleted,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Edit,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (isEnabled && !isCompleted) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
fun AddNewBookletButton(onClick: () -> Unit) {
    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.Gray,
                    style = stroke,
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Yeni Kitapçık Ekle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}