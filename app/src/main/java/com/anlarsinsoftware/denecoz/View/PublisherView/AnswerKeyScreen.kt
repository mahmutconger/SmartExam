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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEvent
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.FileType
import com.anlarsinsoftware.denecoz.Model.State.TabOption
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.AnswerKeyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerKeyScreen(
    onBack: () -> Unit = {},
    navController: NavController,
    viewModel: AnswerKeyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = queryDisplayName(context.contentResolver, it) ?: "dosya"
            val type = if (name.endsWith(".pdf")) FileType.PDF else FileType.EXCEL
            viewModel.onEvent(AnswerKeyEvent.OnFileSelected(it, name, type))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when(event) {
                is AnswerKeyNavigationEvent.NavigateToEditor -> {
                    navController.navigate(Screen.AnswerKeyEditorScreen.createRoute(event.examId, event.mode))
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
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

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color.Transparent),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TabPill(
                        text = stringResource(id = R.string.tab_answer_key),
                        selected = uiState.selectedTab == TabOption.ANSWER_KEY,
                        onClick = { viewModel.onEvent(AnswerKeyEvent.OnTabSelected(TabOption.ANSWER_KEY)) },
                        primaryColor = primaryBlue,
                        pillHeight = 40.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TabPill(
                        text = stringResource(id = R.string.tab_topics),
                        selected = uiState.selectedTab == TabOption.TOPICS,
                        onClick = { viewModel.onEvent(AnswerKeyEvent.OnTabSelected(TabOption.TOPICS)) },
                        primaryColor = primaryBlue,
                        pillHeight = 40.dp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

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

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Excel
                            ElevatedActionButton(
                                label = stringResource(id = R.string.upload_excel),
                                onClick = {
                                    filePicker.launch(arrayOf(
                                        "application/vnd.ms-excel",
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    ))
                                },
                                backgroundColor = green,
                                trailingIcon = Icons.Default.ChevronRight
                            )
                            // PDF
                            ElevatedActionButton(
                                label = stringResource(id = R.string.upload_pdf),
                                onClick = {
                                    filePicker.launch(arrayOf("application/pdf"))
                                },
                                backgroundColor = green,
                                trailingIcon = Icons.Default.ChevronRight
                            )

                            ElevatedActionButton(
                                label = stringResource(id = R.string.create_manual),
                                onClick = {
                                    viewModel.onEvent(AnswerKeyEvent.OnManualEntrySelected)
                                },
                                backgroundColor = primaryBlue,
                                trailingIcon = Icons.Default.ChevronRight
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        if (uiState.uploadedFile != null) {
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
                                    val icon = when (uiState.uploadedFile!!.type) {
                                        FileType.PDF -> Icons.Default.PictureAsPdf
                                        FileType.EXCEL -> Icons.Default.InsertDriveFile
                                        FileType.MANUAL -> Icons.Default.Edit
                                    }
                                    Icon(icon, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = uiState.uploadedFile!!.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(
                                            text = stringResource(id = R.string.preview_available),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = stringResource(id = R.string.change),
                                        color = primaryBlue,
                                        modifier = Modifier.clickable {
                                            viewModel.onEvent(AnswerKeyEvent.OnChangeFileClicked)
                                        }.padding(8.dp)
                                    )
                                }
                            }
                        } else {

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
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            viewModel.onEvent(AnswerKeyEvent.OnContinueClicked)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !uiState.isLoading
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
