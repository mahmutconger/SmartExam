package com.anlarsinsoftware.denecoz.View.PublisherView

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.denecoz.Model.State.Publisher.ExamSummaryItem
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PubHomeEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PubHomeNavigationEvent
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.PubHomeViewModel


@Composable
fun PubHomeScreen(
    navController: NavController,
    viewModel: PubHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is PubHomeNavigationEvent.NavigateToGeneralInfo -> {
                    navController.navigate(Screen.GeneralInfoScreen.route)
                }
                is PubHomeNavigationEvent.NavigateToAnswerKey -> {
                    navController.navigate(Screen.AnswerKeyScreen.createRoute(event.examId))
                }
            }
        }
    }

    val verificationStatus = uiState.publisherProfile?.verificationStatus
    if (verificationStatus == "PENDING") {
        VerificationPendingDialog()
    }

    Scaffold(
        bottomBar = { BottomNavBar(
            selectedIndex = 0,
            onItemSelected = { index ->
                when (index) {
                    0 -> { /* Zaten bu ekrandayız, bir şey yapmaya gerek yok */ }
                    // 1 -> navController.navigate(...) // TODO: Grafik ekranı
                    // 2 -> navController.navigate(...) // TODO: Ayarlar ekranı
                    3 -> {
                        // Profil ikonuna tıklandığında ProfileScreen'e git
                        navController.navigate(Screen.PublisherProfileScreen.route)
                    }
                }
            }
        ) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (verificationStatus != "APPROVED") {
                    Toast.makeText(context, "Hesabınız onaylandıktan sonra yeni deneme ekleyebilirsiniz.", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.onEvent(PubHomeEvent.OnAddNewExamClicked)
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Deneme Ekle")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TopHeader(publisherName = uiState.publisherProfile?.name ?: "Yayıncı")
            Spacer(Modifier.height(12.dp))
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = { newQuery ->
                    viewModel.onEvent(PubHomeEvent.OnQueryChanged(newQuery))
                }
            )
            Spacer(Modifier.height(16.dp))
            HeaderRow()
            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                }

                uiState.exams.isEmpty() -> {
                    Text(
                        "Henüz bir deneme oluşturmadınız. Yeni bir tane eklemek için '+' butonuna dokunun.",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    // Deneme grid'ini, ViewModel'dan gelen dinamik veriyle doldur
                    ExamGrid(
                        exams = uiState.exams,
                        onExamClick = { exam ->
                            if (verificationStatus != "APPROVED") {
                                Toast.makeText(context, "Hesabınız henüz onaylanmamış.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.onEvent(PubHomeEvent.OnExamClicked(exam))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopHeader(publisherName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(publisherName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(id = R.string.title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SearchBar(query :String,onQueryChanged:(String)->Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(stringResource(id = R.string.search)) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(id = R.string.publishedExams),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            stringResource(id = R.string.see_all),
            color = Color(0xFF4A6CF7),
            fontSize = 14.sp,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun ExamGrid(exams: List<ExamSummaryItem>, onExamClick: (ExamSummaryItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exams, key = { it.id }) { exam ->
            ExamCard(exam = exam, onClick = { onExamClick(exam) })
        }
    }
}

@Composable
fun ExamCard(exam: ExamSummaryItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFECECEC)),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp)
                )
                AsyncImage(
                    model = exam.coverImageUrl,
                    contentDescription = exam.name,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (exam.status == "draft") {
                    Badge(modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                        Text("Taslak")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(exam.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
//            Spacer(Modifier.height(4.dp))
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(6.dp)
//            ) {
//                Text(exam.duration, fontSize = 12.sp, color = Color.Gray)
//                Icon(
//                    Icons.Default.Star,
//                    null,
//                    tint = Color(0xFFFFA500),
//                    modifier = Modifier.size(14.dp)
//                )
//                Text("${exam.rating}", fontSize = 12.sp, color = Color.Gray)
//            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (exam.status == "draft") "Devam Et" else "İstatistikler")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun BottomNavBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        Pair(R.string.bottom_home, Icons.Default.Home),
        Pair(R.string.bottom_explore, Icons.Default.Search),
        Pair(R.string.bottom_settings, Icons.Default.Settings),
        Pair(R.string.bottom_profile, Icons.Default.Person)
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            val isSelected = (selectedIndex == index)
            val label = stringResource(id = item.first)
            val icon = item.second

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
@Composable
private fun VerificationPendingDialog() {
    AlertDialog(
        onDismissRequest = { /* Diyaloğun dışına basarak kapatılmasını engelle */ },
        title = { Text("Hesabınız İnceleniyor") },
        text = {
            Text("Yayıncı hesabınızın doğruluğu ekibimiz tarafından incelenmektedir. Onay süreci tamamlandığında size e-posta ve uygulama bildirimi yoluyla geri bildirim sağlayacağız. Anlayışınız için teşekkür ederiz.")
        },
        confirmButton = {
            TextButton(
                onClick = { /* Belki bir 'Destek' sayfasına yönlendirilebilir */ }
            ) {
                Text("Daha Fazla Bilgi")
            }
        }
    )
}