package com.anlarsinsoftware.denecoz.View.PublisherView

// GenelInfoScreen.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen


@Composable
fun GeneralInfoScreen(
    onBackClicked: () -> Unit = {},
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.title),
                onBack = onBackClicked
            )
        },
        bottomBar = { BottomNavBar() },
        containerColor = Color(0xFFF8F9FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            ImagePlaceholder()
            Spacer(modifier = Modifier.height(24.dp))

            var examName by remember { mutableStateOf("") }
            var pressDate by remember { mutableStateOf("") }
            var bookletType by remember { mutableStateOf("") }
            var examType by remember { mutableStateOf("") }

            FormField(
                value = examName,
                onValueChange = { examName = it },
                placeholder =  stringResource(id = R.string.examNamePlaceholder),
                leading = {   Image(
                    painter = painterResource(id = R.drawable.ic_book),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                ) }
            )
            Spacer(Modifier.height(12.dp))

            FormField(
                value = pressDate,
                onValueChange = { pressDate = it },
                placeholder =  stringResource(id = R.string.pressDatePlaceholder),
                leading = { Icon( painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null) },
                readOnly = true,
                onClick = {
                    // Burada DatePicker açılabilir. Bu prototipte sadece tıklama callback bırakıyoruz.
                    // Örnek: showDatePicker(...) ve seçilen tarihi pressDate'e ata.
                }
            )
            Spacer(Modifier.height(12.dp))

            FormField(
                value = bookletType,
                onValueChange = { bookletType = it },
                placeholder =  stringResource(id = R.string.bookletTypePlaceholder),
                leading = { Icon( painter = painterResource(id = R.drawable.ic_analysis), contentDescription = null) },
                readOnly = true,
                onClick = {
                    // Burada dropdown menü açılabilir (ExposedDropdownMenuBox).
                }
            )
            Spacer(Modifier.height(12.dp))

            FormField(
                value = examType,
                onValueChange = { examType = it },
                placeholder =  stringResource(id = R.string.examTypePlaceholder),
                leading = { Icon( painter = painterResource(id = R.drawable.ic_speel_check), contentDescription = null) },
                readOnly = true,
                onClick = {
                    // Dropdown veya seçim ekranı açılabilir.
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Buton
            ConfirmButton(
                text =  stringResource(id = R.string.confirmContinue),
                onClick = {
                    navController.navigate(Screen.AnswerKeyScreen.route)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            Spacer(modifier = Modifier.width(56.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}


@Composable
fun ImagePlaceholder() {
    // Yuvarlak beyaz arka plan, ortada büyük image icon
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF2F4F8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_photos),
                contentDescription = "Image placeholder",
                modifier = Modifier.size(56.dp),
                tint = Color(0xFF222222)
            )
        }
    }
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        placeholder = { Text(placeholder, color = Color(0xFF9AA0B4)) },
        leadingIcon = if (leading != null) ({
            Box(
                modifier = Modifier
                    .size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                leading()
            }
        }) else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF6F8FB),
            unfocusedContainerColor = Color(0xFFF6F8FB),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = Color(0xFF4A4A4A),
            unfocusedLeadingIconColor = Color(0xFFB0B7C3)
        )
    )
}

@Composable
fun ConfirmButton(text: String, onClick: () -> Unit) {
    // Gradient görünümü için Box + Button benzeri görünüm
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B49FF))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text, color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGeneralInfoScreen() {
    MaterialTheme {
        GeneralInfoScreen(navController = rememberNavController())
    }
}
