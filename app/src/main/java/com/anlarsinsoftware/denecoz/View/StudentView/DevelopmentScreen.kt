package com.anlarsinsoftware.denecoz.View.StudentView

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.anlarsinsoftware.denecoz.Model.State.Student.DevelopmentEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.DevelopmentNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.TimeFilter
import com.anlarsinsoftware.denecoz.Model.Student.NetScoreHistoryPoint
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AnimatedBottomBar
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.DevelopmentViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.text.SimpleDateFormat
import java.util.Locale

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
                    items = TimeFilter.values().map { it.name }, // Enum isimlerini al
                    selectedItem = uiState.selectedTimeFilter.name,
                    onItemSelected = { filterName ->
                        viewModel.onEvent(DevelopmentEvent.OnTimeFilterSelected(TimeFilter.valueOf(filterName)))
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

@OptIn(ExperimentalMaterial3Api::class)
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
                FilterChip(
                    selected = (selectedItem == item),
                    onClick = { onItemSelected(item) },
                    label = { Text(item.replace("_", " ")) } // Enum isimlerini güzelleştir
                )
            }
        }
    }
}

@Composable
private fun DevelopmentChartCard(
    examType: String,
    chartData: List<NetScoreHistoryPoint>,
    onShowReportClicked: () -> Unit
) {
    // Vico için veri modelini hazırla
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    LaunchedEffect(chartData) {
        val chartEntries = chartData.mapIndexed { index, dataPoint ->
            FloatEntry(index.toFloat(), dataPoint.net.toFloat())
        }
        chartEntryModelProducer.setEntries(chartEntries)
    }

    // Vico için X ekseni etiketlerini (Tarihleri) hazırla
    val dateFormat = SimpleDateFormat("dd MMM", Locale("tr"))
    val bottomAxisValueFormatter = remember {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            chartData.getOrNull(value.toInt())?.date?.let { dateFormat.format(it) } ?: ""
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$examType Gelişim Grafiği", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            // Vico Grafik
            ProvideChartStyle() {
                Chart(
                    chart = lineChart(),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(), // Y ekseni (Netler)
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter), // X ekseni (Tarihler)
                    modifier = Modifier.height(250.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onShowReportClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("$examType Analiz Raporunu Gör")
            }
        }
    }
}