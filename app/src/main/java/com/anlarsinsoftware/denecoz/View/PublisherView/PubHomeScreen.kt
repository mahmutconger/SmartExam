package com.anlarsinsoftware.denecoz.View.PublisherView

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen


data class Exam(
    val id: Int,
    val title: String,
    val duration: String,
    val rating: Double,
    val imageRes: Int
)

val sampleExams = listOf(
    Exam(1, "Limit TYT 5’li Deneme", "20min", 4.5, android.R.drawable.ic_menu_gallery),
    Exam(2, "345 AYT Deneme", "20min", 4.5, android.R.drawable.ic_menu_gallery),
    Exam(3, "AYT Deneme Sınavları", "20min", 4.5, android.R.drawable.ic_menu_gallery),
    Exam(4, "TYT 5’li Deneme", "20min", 4.5, android.R.drawable.ic_menu_gallery)
)

@Composable
fun PubHomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TopHeader(navController = navController)
            Spacer(Modifier.height(12.dp))
            SearchBar()
            Spacer(Modifier.height(16.dp))
            HeaderRow()
            Spacer(Modifier.height(12.dp))
            ExamGrid()
        }
    }
}

@Composable
fun TopHeader(navController: NavController) {
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
                Text("L", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(id = R.string.title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        IconButton(
            onClick = {
                navController.navigate(Screen.GeneralInfoScreen.route)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
        }
    }
}

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
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
        Text(stringResource(id = R.string.publishedExams), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            stringResource(id = R.string.see_all),
            color = Color(0xFF4A6CF7),
            fontSize = 14.sp,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun ExamGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sampleExams) { exam ->
            ExamCard(exam)
        }
    }
}

@Composable
fun ExamCard(exam: Exam) {
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
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.padding(6.dp))
                Image(
                    painter = painterResource(id = exam.imageRes),
                    contentDescription = exam.title,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(exam.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(exam.duration, fontSize = 12.sp, color = Color.Gray)
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFA500), modifier = Modifier.size(14.dp))
                Text("${exam.rating}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.edit))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun BottomNavBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text(stringResource(id = R.string.bottom_home)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Search, null) },
            label = { Text(stringResource(id = R.string.bottom_explore)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text(stringResource(id = R.string.bottom_settings)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text(stringResource(id = R.string.bottom_profile)) }
        )
    }
}
