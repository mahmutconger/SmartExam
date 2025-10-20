package com.anlarsinsoftware.denecoz.View.StudentView

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.EditProfileNavigationEvent
import com.anlarsinsoftware.denecoz.R // R dosyasını import et
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class) // ExposedDropdownMenuBox için gerekli
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> viewModel.onEvent(EditProfileEvent.OnPhotoSelected(uri)) }
    )

    // Navigasyon ve hata yönetimi
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is EditProfileNavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    // Dropdown menülerin açık/kapalı durumlarını tutacak state'ler
    var educationExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var schoolExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profili Düzenle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        // Ekran ilk yüklendiğinde tam ekran yükleme göstergesi
        if (uiState.isLoading && uiState.name.isBlank()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()), // Kaydırılabilir yaptık
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }) {
                    AsyncImage(
                        model = uiState.newProfilePhotoUri ?: uiState.profileImageUrl,
                        placeholder = painterResource(id = R.drawable.profile_placeholder),
                        error = painterResource(id = R.drawable.profile_placeholder),
                        contentDescription = "Profil Resmi",
                        modifier = Modifier.size(120.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    // ... (Edit ikonu eklenebilir)
                }
                TextButton(onClick = { imagePickerLauncher.launch("image/*") }) { Text("Fotoğrafı Değiştir") }
                Spacer(Modifier.height(24.dp))

                // İsim Alanı
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.OnNameChanged(it)) },
                    label = { Text("İsim Soyisim") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))

                // Eğitim Seviyesi Dropdown
                ExposedDropdownMenuBox(
                    expanded = educationExpanded,
                    onExpandedChange = { educationExpanded = !educationExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.educationLevel,
                        onValueChange = { }, // Sadece okunabilir
                        readOnly = true,
                        label = { Text("Öğrenim Durumu") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = educationExpanded,
                        onDismissRequest = { educationExpanded = false }
                    ) {
                        uiState.educationLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    viewModel.onEvent(EditProfileEvent.OnEducationLevelSelected(level))
                                    educationExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Şehir Dropdown
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = !cityExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = {}, readOnly = true,
                        label = { Text("Şehir") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) }
                    )
                    ExposedDropdownMenu(expanded = cityExpanded, onDismissRequest = { cityExpanded = false }) {
                        uiState.cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    viewModel.onEvent(EditProfileEvent.OnCitySelected(city))
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // İlçe Dropdown (Sadece şehir seçiliyse aktif)
                ExposedDropdownMenuBox(
                    expanded = districtExpanded && uiState.city.isNotBlank(), // Şehir seçiliyse aktif
                    onExpandedChange = { if(uiState.city.isNotBlank()) districtExpanded = !districtExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.district,
                        onValueChange = {}, readOnly = true,
                        enabled = uiState.city.isNotBlank(), // Şehir seçiliyse aktif
                        label = { Text("İlçe") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded && uiState.city.isNotBlank()) }
                    )
                    ExposedDropdownMenu(expanded = districtExpanded && uiState.city.isNotBlank(), onDismissRequest = { districtExpanded = false }) {
                        uiState.districts.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(district) },
                                onClick = {
                                    viewModel.onEvent(EditProfileEvent.OnDistrictSelected(district))
                                    districtExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Okul Dropdown (Sadece ilçe seçiliyse aktif)
                ExposedDropdownMenuBox(
                    expanded = schoolExpanded && uiState.district.isNotBlank(), // İlçe seçiliyse aktif
                    onExpandedChange = { if(uiState.district.isNotBlank()) schoolExpanded = !schoolExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.schools,
                        onValueChange = {}, readOnly = true,
                        enabled = uiState.district.isNotBlank(), // İlçe seçiliyse aktif
                        label = { Text("Okul") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolExpanded && uiState.district.isNotBlank()) }
                    )
//                    ExposedDropdownMenu(expanded = schoolExpanded && uiState.district.isNotBlank(), onDismissRequest = { schoolExpanded = false }) {
//                        uiState.schools.forEach { school ->
//                            DropdownMenuItem(
//                                text = { Text(school) },
//                                onClick = {
//                                    viewModel.onEvent(EditProfileEvent.OnSchoolSelected(school))
//                                    schoolExpanded = false
//                                }
//                            )
//                        }
//                    }
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.onEvent(EditProfileEvent.OnSaveClicked) },
                  //  enabled = uiState.isSaveButtonEnabled && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (uiState.isLoading) CircularProgressIndicator() else Text("Değişiklikleri Kaydet")
                }
                Spacer(Modifier.height(24.dp)) // Altta boşluk
            }
        }
    }
}