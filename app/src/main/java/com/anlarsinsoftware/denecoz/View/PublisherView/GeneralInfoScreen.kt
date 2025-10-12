package com.anlarsinsoftware.denecoz.View.PublisherView

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import coil.compose.AsyncImage
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.denecoz.Model.State.GeneralInfoEvent
import com.anlarsinsoftware.denecoz.Model.State.NavigationEvent
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.GeneralInfoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralInfoScreen(
    viewModel: GeneralInfoViewModel = hiltViewModel(),
    navController: NavController
) {

    val uiState by viewModel.uiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(GeneralInfoEvent.OnCoverImageSelected(uri))
    }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }


    var examTypeExpanded by remember { mutableStateOf(false) }
    val examTypes = listOf("TYT", "AYT", "LGS")


    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToAnswerKey -> {
                    navController.navigate(Screen.AnswerKeyScreen.createRoute(event.examId))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.title),
                onBack = {navController.popBackStack() }
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
            ImagePlaceholder(
                imageUri = uiState.coverImageUri,
                onClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )
            Spacer(modifier = Modifier.height(24.dp))


            FormField(
                value = uiState.examName,
                onValueChange = { viewModel.onEvent(GeneralInfoEvent.OnExamNameChanged(it)) },
                placeholder =  stringResource(id = R.string.examNamePlaceholder),
                modifier = Modifier,
                leading = {   Image(
                    painter = painterResource(id = R.drawable.ic_speel_check),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                ) }
            )
            Spacer(Modifier.height(12.dp))

            FormField(
                value = uiState.publicationDate,
                onValueChange = { /* enabled=false olduğu için zaten çalışmayacak */ },
                placeholder = "Yayın Tarihi Seçin",
                leading = { Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null) },

                // readOnly yerine enabled=false kullanıyoruz.
                // Bu, tıklanabilirliği etkilemez ama metin alanını tamamen pasif hale getirir.
                enabled = false,

                // Tıklama olayını doğrudan FormField'ın modifier'ına ekliyoruz.
                modifier = Modifier.clickable {
                    showDatePicker = true
                    Log.d("DatePicker", "Tarih alanı tıklandı, showDatePicker true olmalı.") // Hata ayıklama için Log
                },

                // enabled=false olduğunda renklerin soluklaşmasını engellemek için özel renkler
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color(0xFFF6F8FB),
                    disabledIndicatorColor = Color.Transparent,
                    disabledLeadingIconColor = Color(0xFFB0B7C3),
                    disabledTextColor = if (uiState.publicationDate.isNotBlank()) Color.Black else Color(0xFF9AA0B4)
                )
            )


            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = examTypeExpanded,
                onExpandedChange = { examTypeExpanded = !examTypeExpanded }
            ) {
                FormField(
                    value = uiState.examType,
                    onValueChange = { },
                    modifier = Modifier.menuAnchor(),
                    placeholder =  stringResource(id = R.string.examTypePlaceholder),
                    leading = { Icon( painter = painterResource(id = R.drawable.ic_speel_check), contentDescription = null) },
                    readOnly = true,
                )
                ExposedDropdownMenu(
                    expanded = examTypeExpanded,
                    onDismissRequest = { examTypeExpanded = false }
                ) {
                    examTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.onEvent(GeneralInfoEvent.OnExamTypeSelected(type))
                                examTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            ConfirmButton(
                text = stringResource(id = R.string.confirmContinue),
                onClick = {
                    viewModel.onEvent(GeneralInfoEvent.OnContinueClicked)
                },
                enabled = uiState.isFormValid && !uiState.isLoading
            )

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formattedDate = sdf.format(Date(millis))
                            viewModel.onEvent(GeneralInfoEvent.OnPublicationDateSelected(formattedDate))
                        }
                    }
                ) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
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
fun ImagePlaceholder(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF2F4F8)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Seçilen Kapak Resmi",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_photos),
                    contentDescription = "Image placeholder",
                    modifier = Modifier.size(56.dp),
                    tint = Color(0xFF222222)
                )
            }
        }
    }
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF6F8FB),
        unfocusedContainerColor = Color(0xFFF6F8FB),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedLeadingIconColor = Color(0xFF4A4A4A),
        unfocusedLeadingIconColor = Color(0xFFB0B7C3),
        disabledContainerColor = Color(0xFFF6F8FB),
        disabledIndicatorColor = Color.Transparent,
        disabledLeadingIconColor = Color(0xFFB0B7C3),
        disabledTextColor = Color.Black
    )
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        enabled = enabled,
        placeholder = { Text(placeholder, color = Color(0xFF9AA0B4)) },
        leadingIcon = if (leading != null) ({
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                leading()
            }
        }) else null,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = colors
    )
}

@Composable
fun ConfirmButton(text: String,enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
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
