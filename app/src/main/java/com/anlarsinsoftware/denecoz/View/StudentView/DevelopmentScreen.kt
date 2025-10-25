package com.anlarsinsoftware.denecoz.View.StudentView

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.anlarsinsoftware.denecoz.Model.State.Student.DevelopmentEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.DevelopmentNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.TimeFilter
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AnimatedBottomBar
import com.anlarsinsoftware.denecoz.View.Common.DevelopmentChartCard
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.DevelopmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevelopmentScreen(
    navController: NavController,
    viewModel: DevelopmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val bottomItems = listOf(
        "Ana Sayfa" to Icons.Default.Home,
        "Sıralama" to Icons.Default.BarChart,
        "Gelişim" to Icons.AutoMirrored.Filled.TrendingUp,
        "Profil" to Icons.Default.Person
    )

    // Navigasyon olaylarını dinle
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is DevelopmentNavigationEvent.NavigateToHistoricalReport -> {
                    navController.navigate(Screen.HistoricalReportScreen.createRoute(event.examType))
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Gelişim Grafiği") })
        }, bottomBar = {
            AnimatedBottomBar(
                items = bottomItems,
                selectedIndex = 2,
                onItemSelected = { index ->
                    if (index == 2) return@AnimatedBottomBar

                    val route = when (index) {
                        0 -> Screen.HomeScreen.route
                        1 -> Screen.LeaderboardScreen.createRoute(null)
                        3 -> Screen.ProfileScreen.route
                        else -> Screen.ProfileScreen.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // 1. Sınav Türü Filtreleri
            item {
                FilterChipRow(
                    title = "Sınav Türü Seç:",
                    items = listOf("TYT", "AYT", "LGS", "DGS"),
                    selectedItem = uiState.selectedExamType,
                    onItemSelected = { examType ->
                        viewModel.onEvent(DevelopmentEvent.OnExamTypeSelected(examType))
                    }
                )
            }

            // 2. Zaman Aralığı Filtreleri
            item {

                FilterChipRow(
                    title = "Zaman Aralığı Seç:",
                    items = TimeFilter.values().map { it.filterName }, // Enum isimlerini al
                    selectedItem = uiState.selectedTimeFilter.filterName,
                    onItemSelected = { filterName ->
                        val selectedEnum = TimeFilter.values().firstOrNull { it.filterName == filterName }
                        if (selectedEnum != null) viewModel.onEvent(DevelopmentEvent.OnTimeFilterSelected(selectedEnum))
                    }
                )
            }

            // 3. Grafik Alanı
            item {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxWidth().height(300.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.chartData.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(300.dp), Alignment.Center) {
                        Text("Bu filtreler için gösterilecek veri bulunamadı.")
                    }
                } else {
                    // Grafik kartı
                    DevelopmentChartCard(
                        examType = uiState.selectedExamType,
                        chartData = uiState.chartData,
                        onShowReportClicked = {
                            viewModel.onEvent(DevelopmentEvent.OnShowReportClicked(uiState.selectedExamType))
                        }
                    )
                }
            }
        }
    }
}

// --- YARDIMCI COMPOSABLE'LAR ---

@Composable
private fun FilterChipRow(
    title: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                ModernFilterChip(
                    text = item.replace("_", " "),
                    isSelected = selectedItem == item,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}
@Composable
fun ModernFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = if (isSelected)
            listOf(Color(0xFF6B4EFF), Color(0xFFA855F7))
        else
            listOf(Color(0xFF2E2E3A), Color(0xFF2E2E3A))
    )

    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .scale(scale)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
    }
}