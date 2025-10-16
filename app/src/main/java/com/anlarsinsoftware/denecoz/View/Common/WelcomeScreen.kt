package com.anlarsinsoftware.denecoz.View.Common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Screen

@Composable
fun WelcomeScreen(
    navController: NavController
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp), // Kenarlardan daha fazla boşluk
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Uygulama Adı ve Slogan
            Text(
                "SmartExam",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Deneme Analizinin En Akıllı Yolu",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(64.dp))

            Text(
                "Lütfen rolünüzü seçerek devam edin:",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Öğrenci Seçim Kartı
            RoleSelectionCard(
                title = "Öğrenciyim",
                subtitle = "Denemeleri çöz, analizlerini gör ve gelişimini takip et.",
                icon = Icons.Default.Person,
                onClick = {
                    navController.navigate(Screen.StudentLoginScreen.route)
                }
            )
            Spacer(Modifier.height(16.dp))

            // Yayıncı Seçim Kartı
            RoleSelectionCard(
                title = "Yayıncıyım",
                subtitle = "Denemelerini yayınla ve binlerce öğrenciye ulaş.",
                icon = Icons.Default.Store,
                onClick = {
                    navController.navigate(Screen.PublisherLoginScreen.route)
                }
            )
        }
    }
}


// --- YARDIMCI COMPOSABLE (Bu fonksiyonu dosyanın en altına ekle) ---

@Composable
private fun RoleSelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}