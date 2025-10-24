package com.anlarsinsoftware.denecoz.View.StudentView

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.anlarsinsoftware.denecoz.Model.State.Student.BestNetResult
import com.anlarsinsoftware.denecoz.Model.State.Student.PastAttemptSummary
import com.anlarsinsoftware.denecoz.Model.State.Student.UserProfile
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AnimatedBottomBar
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
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
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Ayarlar ekranına git */ }) {
                        Icon(Icons.Default.Settings, "Ayarlar")
                    }
                }
            )
        },bottomBar = {
            AnimatedBottomBar(
                items = bottomItems,
                selectedIndex = 3,
                onItemSelected = { index ->
                    if (index == 3) return@AnimatedBottomBar

                    val route = when (index) {
                        0 -> Screen.HomeScreen.route
                        1 -> Screen.LeaderboardScreen.createRoute(null)
                        2 -> Screen.DevelopmentScreen.route
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
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    ProfileHeader(
                        userProfile = uiState.userProfile,
                        attemptCount = uiState.totalAttemptsCount,
                        onProfileClick = {
                            navController.navigate(Screen.EditProfileScreen.route)
                        }
                    )
                }

                if (uiState.userProfile?.isProfileComplete == false) {
                    item {
                        ProfileCompletionReminderCard(
                            onClick = {
                                navController.navigate(Screen.EditProfileScreen.route)
                            }
                        )
                    }
                }

                // 2. En Yüksek Net Kartları
                item {
                    Spacer(Modifier.height(24.dp))
                    // TYT skoru varsa göster
                    uiState.userProfile?.bestScores?.get("TYT")?.let {
                        BestNetCard(title = "TYT (En Yüksek Net)", result = it)
                        Spacer(Modifier.height(16.dp))
                    }
                    // AYT skoru varsa göster
                    uiState.userProfile?.bestScores?.get("AYT")?.let {
                        BestNetCard(title = "AYT (En Yüksek Net)", result = it)
                    }
                }

                // 3. Geçmiş Denemeler Başlığı ve Listesi
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Geçmiş Denemeler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(uiState.pastAttempts, key = { it.attemptId }) { attempt ->
                    PastAttemptRow(
                        attempt = attempt,
                        onClick = {
                            navController.navigate(
                                Screen.ResultsScreen.createRoute(attempt.examId, attempt.attemptId)
                            )
                        }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}


@Composable
private fun ProfileHeader(userProfile: UserProfile?, attemptCount: Int, onProfileClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
        ) {
            AsyncImage(
                model = userProfile?.profileImageUrl,
                placeholder = painterResource(id = R.drawable.profile_placeholder),
                error = painterResource(id = R.drawable.profile_placeholder),
                contentDescription = "Profil Resmi",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Icon(
                Icons.Default.Edit,
                contentDescription = "Profili Düzenle",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(6.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = userProfile?.name ?: "...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$attemptCount Deneme",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
private fun BestNetCard(title: String, result: BestNetResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ScoreItem(value = result.correct.toInt(), label = "D", color = Color(0xFF2E7D32)) // Green
                ScoreItem(value = result.incorrect.toInt(), label = "Y", color = Color(0xFFC62828)) // Red
                ScoreItem(value = result.net, label = "N", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ScoreItem(value: Any, label: String, color: Color) {
    val valueText = when (value) {
        is Double -> String.format("%.2f", value)
        else -> value.toString()
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Text(text = valueText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun PastAttemptRow(attempt: PastAttemptSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = attempt.coverImageUrl,
            contentDescription = attempt.examName,
            placeholder = painterResource(id = R.drawable.logo_limit), // Varsayılan bir resim
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(attempt.examName, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(attempt.examType, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Detaylara Git", tint = Color.Gray)
    }
}

@Composable
private fun ProfileCompletionReminderCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Sıralamalardaki yerini gör!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Okul, ilçe ve Türkiye sıralamalarını görmek için profilini tamamla.", style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Profili Tamamla")
        }
    }
}