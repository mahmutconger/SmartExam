package com.anlarsinsoftware.denecoz.View.PublisherView

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.R

private enum class FileType { PDF, EXCEL, MANUAL }
private data class UploadedFile(val name: String, val type: FileType, val uri: Uri?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerKeyScreen(
    onBack: () -> Unit = {},
    navController: NavController
    // isSelectedTab default 0 for "Cevap Anahtarı"
) {
    val context = LocalContext.current

    // Tab state
    var selectedTab by remember { mutableStateOf(0) } // 0 = Cevap Anahtarı, 1 = Konular

    // Simule edilmiş yüklü dosya state'i
    var uploadedFile by remember { mutableStateOf<UploadedFile?>(null) }

    // File pickers (demo): PDF and Excel
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = queryDisplayName(context.contentResolver, it) ?: "dosya.pdf"
            uploadedFile = UploadedFile(name, FileType.PDF, it)
            Toast.makeText(context, "${name} yüklendi", Toast.LENGTH_SHORT).show()
        }
    }
    val excelPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = queryDisplayName(context.contentResolver, it) ?: "tablo.xlsx"
            uploadedFile = UploadedFile(name, FileType.EXCEL, it)
            Toast.makeText(context, "${name} yüklendi", Toast.LENGTH_SHORT).show()
        }
    }

    // Colors (uses colors.xml entries, fallback hard-coded if not provided)
    val primaryBlue = runCatching { colorResource(id = R.color.primaryBlue) }.getOrElse { Color(0xFF6A4CFF) }
    val green = runCatching { colorResource(id = R.color.successGreen) }.getOrElse { Color(0xFF2E8B57) }
    val cardBg = runCatching { colorResource(id = R.color.cardBackground) }.getOrElse { Color(0xFFEFF3FF) }
    val lightGray = runCatching { colorResource(id = R.color.dividerGray) }.getOrElse { Color(0xFFE6E6E9) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.answer_key_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F8FF)
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // --- TabRow benzeri segment
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color.Transparent),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TabPill(
                        text = stringResource(id = R.string.tab_answer_key),
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        primaryColor = primaryBlue,
                        pillHeight = 40.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TabPill(
                        text = stringResource(id = R.string.tab_topics),
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        primaryColor = primaryBlue,
                        pillHeight = 40.dp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // --- Ana kart alanı (büyük kutu, arkaplan sabit)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // İkonlu üst görsel alan (isteğe göre düzenlenebilir)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = primaryBlue.copy(alpha = 0.6f),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- ACTION BUTONLARI (Excel, PDF, Manuel)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Excel
                            ElevatedActionButton(
                                label = stringResource(id = R.string.upload_excel),
                                onClick = {
                                    // launch excel picker (mime types)
                                    // allow common excel mime types via openDocument
                                    excelPicker.launch(arrayOf(
                                        "application/vnd.ms-excel",
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel.sheet.macroEnabled.12"
                                    ))
                                },
                                backgroundColor = green,
                                trailingIcon = Icons.Default.ChevronRight
                            )

                            // PDF
                            ElevatedActionButton(
                                label = stringResource(id = R.string.upload_pdf),
                                onClick = {
                                    pdfPicker.launch(arrayOf("application/pdf"))
                                },
                                backgroundColor = green,
                                trailingIcon = Icons.Default.ChevronRight
                            )

                            // Manuel oluştur
                            ElevatedActionButton(
                                label = stringResource(id = R.string.create_manual),
                                onClick = {
                                    // Manuel oluşturma akışı (şimdilik demo olarak uploadedFile = manual)
                                    uploadedFile = UploadedFile("Manuel giriş", FileType.MANUAL, null)
                                    Toast.makeText(context, "Manuel içerik oluşturuldu", Toast.LENGTH_SHORT).show()
                                },
                                backgroundColor = primaryBlue,
                                trailingIcon = Icons.Default.ChevronRight
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // --- Önizleme alanı (şu an için sadece varlığı gösteriliyor)
                        if (uploadedFile != null) {
                            // küçük kart şeklinde önizleme
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // ikon türüne göre göster
                                    val icon = when (uploadedFile!!.type) {
                                        FileType.PDF -> Icons.Default.PictureAsPdf
                                        FileType.EXCEL -> Icons.Default.InsertDriveFile
                                        FileType.MANUAL -> Icons.Default.Edit
                                    }
                                    Icon(icon, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = uploadedFile!!.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(
                                            text = stringResource(id = R.string.preview_available),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = stringResource(id = R.string.change),
                                        color = primaryBlue,
                                        modifier = Modifier
                                            .clickable {
                                                // değiştir: null yap veya yeniden seç
                                                uploadedFile = null
                                            }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        } else {
                            // placeholder area (varlığı yeter dedin -> küçük bilgi kartı)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .background(Color(0xFFF4F6FB), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.no_preview_yet),
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // (daha altı boş; buton ekranın en altına sabitlenecek)
                    }
                } // Card end

                Spacer(modifier = Modifier.height(14.dp))

                // --- Onayla butonu (ekrandaki alt alanda sabit, yukarıdaki card ile ayrılmış)
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            if (uploadedFile != null) {
                                Toast.makeText(context, "${uploadedFile!!.name} ile devam ediliyor", Toast.LENGTH_SHORT).show()
                                // devam et işlemi
                            } else {
                                Toast.makeText(context, "Lütfen önce bir dosya yükleyin ya da manuel oluşturun.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(28.dp),
                        enabled = true
                    ) {
                        Text(text = stringResource(id = R.string.approve_continue), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

        }
    }
}

/** küçük yardımcı composable'lar **/

@Composable
private fun TabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    pillHeight: Dp
) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .height(pillHeight)
            .clickable(onClick = onClick),
        color = if (selected) primaryColor else Color.LightGray.copy(alpha = 0.25f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(text = text, color = if (selected) Color.White else Color.DarkGray, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
        }
    }
}

@Composable
private fun ElevatedActionButton(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Text(text = label, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))
        Icon(imageVector = trailingIcon, contentDescription = null, tint = Color.White)
    }
}

/** küçük helper: uri'den display name almak (basit) **/
private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
    var name: String? = null
    val cursor = resolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) name = it.getString(idx)
        }
    }
    return name
}
