package com.anlarsinsoftware.denecoz.View.StudentView

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.HomeViewModel

// --- Basit modeller (örnek veri)
data class Publisher(val id: Int, val name: String, @DrawableRes val logo: Int)
data class Product(
    val id: Int,
    val title: String,
    val duration: String,
    val rating: Double,
    @DrawableRes val image: Int
)

private val samplePublishers = listOf(
    Publisher(1, "Limit", R.drawable.logo_limit),
    Publisher(2, "ÜçDörtBeş", R.drawable.logo_ucdortbes)
)


@Composable
fun HomeScreen(
    navController: NavController?,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val primary = colorResource(id = R.color.primaryBlue)
    val outline = colorResource(id = R.color.linkBlue)
    val divider = colorResource(id = R.color.dividerGray)
    val muted = colorResource(id = R.color.mutedText)
    val screenBg = colorResource(id = R.color.screenBackground)

    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg),
        bottomBar = {
            HomeBottomBar(selectedIndex = 0) { index ->
                // placeholder: nav işlemleri burada yapılabilir
            }
        },
        containerColor = screenBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))

                // --- Üst Row: profil + selam + menü
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_placeholder),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.welcome_greeting, "Can"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { /* menu */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = muted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Search bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { /* filter */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Filter"
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = outline,
                        unfocusedIndicatorColor = outline,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedLeadingIconColor = outline,
                        unfocusedLeadingIconColor = muted,
                        unfocusedPlaceholderColor = muted
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                // --- Yayınevleri başlığı
                Text(
                    text = stringResource(id = R.string.discover_publishers),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- Publishers carousel (tek item)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(samplePublishers) { pub ->
                        PublisherItem(pub = pub)
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
            }

            // --- Popüler header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.popular),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(id = R.string.see_all),
                        color = primary,
                        modifier = Modifier
                            .clickable { /* tümünü gör */ }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.exams.isEmpty() && !uiState.isLoading) {
                item {
                    Text("Görünüşe göre henüz yayınlanmış bir deneme yok.")
                }
            } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.exams) { exam ->
                        ProductCard(
                            exam = exam,
                            onClick = {
                                navController?.navigate(Screen.ExamEntryScreen.createRoute(exam.id))
                            }

                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun PublisherItem(pub: Publisher) {
    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 90.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = pub.logo),
                contentDescription = pub.name,
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun ProductCard(exam: PublishedExamSummary, onClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(170.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(colorResource(id = R.color.cardBackground))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Book image
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                ) {
                    AsyncImage(
                        model = exam.coverImageUrl,
                        contentDescription = exam.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )

                    // kalp (favori) overlay
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "favorite",
                            tint = if (isFavorite) colorResource(id = R.color.primaryBlue) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = exam.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // meta (duration + rating)
                // ProductCard içinde, Row'un içini bununla değiştir
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Yayıncı Adı
                    Text(
                        text = exam.publisherName,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.mutedText)
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Arada boşluk bırak

                    // Deneme Türü (TYT/AYT)
                    Icon(
                        imageVector = Icons.Default.School, // Veya başka uygun bir ikon
                        contentDescription = "Deneme Türü",
                        tint = colorResource(id = R.color.primaryBlue),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = exam.examType, // Doğrudan examType'ı gösteriyoruz
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.mutedText)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CTA button
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primaryBlue))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.control_it),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "go",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        Pair(R.string.bottom_home, Icons.Default.Home),
        Pair(R.string.bottom_explore, Icons.Default.ShoppingCart),
        Pair(R.string.bottom_settings, Icons.Default.Settings),
        Pair(R.string.bottom_profile, Icons.Default.Person)
    )

    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, pair ->
                val label = stringResource(id = pair.first)
                val icon = pair.second
                val activeColor = colorResource(id = R.color.primaryBlue)
                val inactive = Color.Gray

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onItemSelected(index) }
                        .padding(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (index == selectedIndex) activeColor else inactive
                    )
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (index == selectedIndex) activeColor else inactive
                    )
                }
            }
        }
    }
}
