package com.anlarsinsoftware.denecoz.View.StudentView

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.anlarsinsoftware.denecoz.Model.State.Student.LeaderboardEvent
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AnimatedBottomBar
import com.anlarsinsoftware.denecoz.View.Common.LeaderboardPlaceCard
import com.anlarsinsoftware.denecoz.View.Common.LeaderboardRow
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bottomItems = listOf(
        "Ana Sayfa" to Icons.Default.Home,
        "Sıralama" to Icons.Default.BarChart,
        "Gelişim" to Icons.AutoMirrored.Filled.TrendingUp,
        "Profil" to Icons.Default.Person
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sıralamalar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            AnimatedBottomBar(
                items = bottomItems,
                selectedIndex = 1,
                onItemSelected = { index ->
                    if (index == 1) return@AnimatedBottomBar

                    val route = when (index) {
                        0 -> Screen.HomeScreen.route
                        2 -> Screen.DevelopmentScreen.route
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // --- 1. Deneme Seçim / Arama Bölümü ---
            // Genişletilebilir arama çubuğu ve seçili deneme kutucuğu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Deneme Arama Çubuğu (Genişletilmiş olduğunda açılacak)
                var showExamSearchDropdown by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = showExamSearchDropdown,
                    onExpandedChange = { showExamSearchDropdown = !showExamSearchDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.examSearchQuery.ifEmpty { uiState.selectedExam?.name ?: "Deneme Seçin" },
                        onValueChange = { viewModel.onEvent(LeaderboardEvent.OnExamSearchQueryChanged(it)) },
                        label = { Text("Deneme ara...") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showExamSearchDropdown) },
                        readOnly = uiState.selectedExam != null && uiState.examSearchQuery.isEmpty(), // Eğer deneme seçiliyse ve arama boşsa, sadece okunabilir yap
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = showExamSearchDropdown && uiState.filteredExams.isNotEmpty(),
                        onDismissRequest = { showExamSearchDropdown = false }
                    ) {
                        uiState.filteredExams.forEach { exam ->
                            DropdownMenuItem(
                                text = { Text("${exam.name} - ${exam.publisherName}") },
                                onClick = {
                                    viewModel.onEvent(LeaderboardEvent.OnExamSelected(exam))
                                    showExamSearchDropdown = false
                                }
                            )
                        }
                    }
                }

                // Seçili deneme adı (opsiyonel, arama kutusuna entegre edildi)
            }
            Spacer(Modifier.height(8.dp))

            // --- 2. Konum Seçim Dropdown'ları (İl / İlçe) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // İl Seçimi
                var showCityDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = showCityDropdown,
                    onExpandedChange = { showCityDropdown = !showCityDropdown },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.selectedCity.ifEmpty { "İl Seçin" },
                        onValueChange = { /* Sadece listeden seçim */ },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCityDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false }
                    ) {
                        uiState.cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    viewModel.onEvent(LeaderboardEvent.OnCitySelected(city))
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }

                // İlçe Seçimi
                AnimatedVisibility(visible = uiState.selectedScope == "İlçe") {
                    var showDistrictDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = showDistrictDropdown,
                        onExpandedChange = { showDistrictDropdown = !showDistrictDropdown },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedDistrict.ifEmpty { "İlçe Seçin" },
                            onValueChange = { /* Sadece listeden seçim */ },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDistrictDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = showDistrictDropdown,
                            onDismissRequest = { showDistrictDropdown = false }
                        ) {
                            uiState.districts.forEach { district ->
                                DropdownMenuItem(
                                    text = { Text(district) },
                                    onClick = {
                                        viewModel.onEvent(LeaderboardEvent.OnDistrictSelected(district))
                                        showDistrictDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // --- 3. Kapsam Filtreleri ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()), // Yatay kaydırma eklendi
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedScope == "Türkiye",
                    onClick = { viewModel.onEvent(LeaderboardEvent.OnScopeChanged("Türkiye")) },
                    label = { Text("Türkiye Geneli") }
                )
                FilterChip(
                    selected = uiState.selectedScope == "İl",
                    onClick = { viewModel.onEvent(LeaderboardEvent.OnScopeChanged("İl")) },
                    label = { Text("İl Geneli") }
                )
                FilterChip(
                    selected = uiState.selectedScope == "İlçe",
                    onClick = { viewModel.onEvent(LeaderboardEvent.OnScopeChanged("İlçe")) },
                    label = { Text("İlçe Geneli") }
                )
            }
            Spacer(Modifier.height(16.dp))

            // --- SIRALAMA BÖLÜMÜ ---
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.selectedExam == null) {
                // Deneme seçilmemişse uyarı mesajı
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Lütfen yukarıdan bir deneme seçin.", style = MaterialTheme.typography.titleMedium)
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Hata: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
                }
            } else {
                // Başlık: Seçilen Deneme Adı
                Text(
                    text = uiState.selectedExam?.name ?: "Deneme Adı",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // İlk 3'ü gösteren Row
                val top3 = uiState.leaderboardEntries.take(3)
                if (top3.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Sıralamaya göre yerleştirme
                        if (top3.size >= 2) {
                            LeaderboardPlaceCard(entry = top3[1], rank = 2, modifier = Modifier.weight(1f))
                        }
                        LeaderboardPlaceCard(entry = top3[0], rank = 1, modifier = Modifier.weight(1f))
                        if (top3.size >= 3) {
                            LeaderboardPlaceCard(entry = top3[2], rank = 3, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Kalanları ve kullanıcının kendi sırasını gösteren LazyColumn
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // 4. sıradan itibaren göster
                    itemsIndexed(uiState.leaderboardEntries.drop(3)) { index, entry ->
                        LeaderboardRow(rank = index + 4, entry = entry)
                    }

                    // Kullanıcının kendi skor kartı (eğer listede yoksa veya kendisi listede değilse)
                    if (uiState.myEntry != null && !uiState.leaderboardEntries.any { it.studentId == uiState.myEntry?.studentId }) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Divider()
                            Spacer(Modifier.height(8.dp))
                            LeaderboardRow(rank = null, entry = uiState.myEntry!!, isMyRank = true)
                        }
                    }
                }
            }
        }
    }
}