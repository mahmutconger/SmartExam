package com.anlarsinsoftware.denecoz.View.PublisherView

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEditorEvent
import com.anlarsinsoftware.denecoz.Model.State.AnswerKeyEditorNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.EditorMode
import com.anlarsinsoftware.denecoz.Model.State.QuestionState
import com.anlarsinsoftware.denecoz.Model.State.SubjectDef
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.ViewModel.PublisherViewModel.AnswerKeyEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerKeyEditorScreen(
    navController: NavController,
    viewModel: AnswerKeyEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is AnswerKeyEditorNavigationEvent.NavigateToPreview -> {
                    navController.navigate(Screen.PreviewScreen.createRoute(event.examId))
                }
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
        items(uiState.subjects) { subject ->
            SubjectRow(
                subject = subject,
                assigned = subject.assignedCount,
                expandedState = subject.isExpanded,
                onToggleExpand = {
                    viewModel.onEvent(AnswerKeyEditorEvent.OnSubjectToggled(subject.name))
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (uiState.mode) {
                            EditorMode.ANSWER_KEY -> stringResource(id = R.string.title_create_answer_key)
                            EditorMode.TOPIC_DISTRIBUTION -> stringResource(id = R.string.title_topic_distribution)
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading && uiState.questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    var questionStartIndex = 0

                    // Her bir ders için bir bölüm oluştur
                    uiState.subjects.forEach { subject ->
                        item {
                            SubjectRow(
                                subject = subject,
                                assigned = subject.assignedCount,
                                expandedState = subject.isExpanded,
                                onToggleExpand = {
                                    viewModel.onEvent(AnswerKeyEditorEvent.OnSubjectToggled(subject.name))
                                }
                            )
                        }

                        // 2. Eğer ders genişletilmişse (isExpanded = true), o derse ait soruları listele
                        if (subject.isExpanded) {
                            val questionEndIndex = questionStartIndex + subject.totalQuestions
                            // Ana 'questions' listesinden bu derse ait olan aralığı al
                            val subjectQuestions = uiState.questions.subList(questionStartIndex, questionEndIndex)


                            items(subjectQuestions) { qState ->
                                val displayIndex = qState.index - questionStartIndex
                                if (uiState.mode == EditorMode.ANSWER_KEY) {
                                    AnswerQuestionRow(
                                        displayIndex=displayIndex,
                                        selected = qState.selectedAnswerIndex,
                                        onSelect = { choiceIndex ->
                                            viewModel.onEvent(AnswerKeyEditorEvent.OnAnswerSelected(qState.index, choiceIndex))
                                        }
                                    )
                                } else { // TOPIC_DISTRIBUTION modu
                                    val subSubjectsToShow = subject.subSubjects ?: listOf(subject)

                                    TopicQuestionRow(
                                        displayIndex = displayIndex,
                                        qState = qState,
                                        subSubjects = subSubjectsToShow,
                                        onSubSubjectSelected = { subSubjectName ->
                                            viewModel.onEvent(AnswerKeyEditorEvent.OnSubSubjectSelected(qState.index, subSubjectName))
                                        },
                                        onTopicSelected = { topic ->
                                            viewModel.onEvent(AnswerKeyEditorEvent.OnTopicSelected(qState.index, topic))
                                        },
                                        onToggleDropdown = {
                                            viewModel.onEvent(AnswerKeyEditorEvent.OnToggleDropdown(qState.index, !qState.isDropdownExpanded))
                                        }
                                    )
                                }
                            }
                        }

                        // Bir sonraki dersin başlangıç index'ini hesaplıyoruz
                        questionStartIndex += subject.totalQuestions
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { viewModel.onEvent(AnswerKeyEditorEvent.OnConfirmClicked) },
                    enabled = uiState.isConfirmButtonEnabled && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(text = stringResource(id = R.string.confirm_continue))
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}



@Composable
private fun SubjectRow(subject: SubjectDef, assigned: Int, expandedState: Boolean, onToggleExpand: () -> Unit) {
    Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = subject.name, modifier = Modifier.weight(1f), fontSize = 16.sp)
            Text(text = "$assigned/${subject.totalQuestions}", color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}
@Composable
private fun AnswerQuestionRow(
    displayIndex: Int,
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.question_label, displayIndex), modifier = Modifier.weight(0.35f))
            Spacer(modifier = Modifier.width(8.dp))
            Row(modifier = Modifier.weight(0.65f), horizontalArrangement = Arrangement.SpaceBetween) {
                // 5 options A-E
                for (i in 0 until 5) {
                    val letter = ('A' + i)
                    val isSelected = selected == i
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onSelect(i) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = letter.toString(), color = if (isSelected) Color.White else Color.Black)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicQuestionRow(
    displayIndex: Int,
    qState: QuestionState,
    subSubjects: List<SubjectDef>,
    onSubSubjectSelected: (String) -> Unit,
    onTopicSelected: (String) -> Unit,
    onToggleDropdown: () -> Unit
) {
    val topicsForSelectedSubSubject = remember(qState.selectedSubSubject) {
        subSubjects.find { it.name == qState.selectedSubSubject }?.topics ?: emptyList()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(id = R.string.question_label, displayIndex))

            // --- 1. DROPDOWN: ALT DERS SEÇİMİ (Tarih, Coğrafya vb.) ---
            var subSubjectExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = subSubjectExpanded,
                onExpandedChange = { subSubjectExpanded = !subSubjectExpanded }
            ) {
                OutlinedTextField(
                    value = qState.selectedSubSubject ?: "Ders Seçin",
                    onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subSubjectExpanded) }
                )
                ExposedDropdownMenu(expanded = subSubjectExpanded, onDismissRequest = { subSubjectExpanded = false }) {
                    subSubjects.forEach { subSubject ->
                        DropdownMenuItem(
                            text = { Text(subSubject.name) },
                            onClick = {
                                onSubSubjectSelected(subSubject.name)
                                subSubjectExpanded = false
                            }
                        )
                    }
                }
            }

            // --- 2. DROPDOWN: KONU SEÇİMİ ---
            ExposedDropdownMenuBox(
                expanded = qState.isDropdownExpanded,
                onExpandedChange = { if (qState.selectedSubSubject != null) onToggleDropdown() }
            ) {
                OutlinedTextField(
                    value = qState.selectedTopic ?: "Konu Seçin",
                    onValueChange = {}, readOnly = true,
                    enabled = qState.selectedSubSubject != null,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qState.isDropdownExpanded) }
                )
                ExposedDropdownMenu(expanded = qState.isDropdownExpanded, onDismissRequest = { onToggleDropdown() }) {
                    topicsForSelectedSubSubject.forEach { topic ->
                        DropdownMenuItem(
                            text = { Text(topic) },
                            onClick = { onTopicSelected(topic) }
                        )
                    }
                }
            }
        }
    }
}