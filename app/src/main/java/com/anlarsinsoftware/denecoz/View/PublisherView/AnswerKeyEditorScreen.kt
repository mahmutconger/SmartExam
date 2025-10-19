package com.anlarsinsoftware.denecoz.View.PublisherView

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
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
import com.anlarsinsoftware.denecoz.Model.Publisher.TopicRef
import com.anlarsinsoftware.denecoz.Model.State.Publisher.AnswerKeyEditorEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.AnswerKeyEditorNavigationEvent
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditorMode
import com.anlarsinsoftware.denecoz.Model.State.Publisher.SubjectDef
import com.anlarsinsoftware.denecoz.R
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AnswerQuestionRow
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
                is AnswerKeyEditorNavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
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
                    itemsIndexed(
                        items = uiState.subjects,
                        key = { _, subject -> "subject_${subject.name}" }
                    ) { subjectIndex, subject ->

                        // Her bir satır kendi Column'u içinde olacak.
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 1. Ders Başlığı (SubjectRow)
                            SubjectRow(
                                subject = subject,
                                assigned = subject.assignedCount,
                                expandedState = subject.isExpanded,
                                onToggleExpand = {
                                    viewModel.onEvent(AnswerKeyEditorEvent.OnSubjectToggled(subject.name))
                                }
                            )

                            // 2. Eğer ders genişletilmişse, o derse ait soruları listele
                            if (subject.isExpanded) {
                                val questionStartIndex = uiState.subjects.take(subjectIndex).sumOf { it.totalQuestions }

                                val questionEndIndex = questionStartIndex + subject.totalQuestions

                                if (questionStartIndex < questionEndIndex && questionEndIndex <= uiState.questions.size) {
                                    val subjectQuestions = uiState.questions.subList(questionStartIndex, questionEndIndex)

                                    // 'items' içinde olduğumuz için burada artık 'items' kullanamayız,
                                    // bu yüzden basit bir 'forEach' döngüsü kullanıyoruz. Bu performanslıdır.
                                    subjectQuestions.forEach { qState ->
                                        val displayIndex = qState.index - questionStartIndex

                                        if (uiState.mode == EditorMode.ANSWER_KEY) {
                                            AnswerQuestionRow(
                                                displayIndex = displayIndex,
                                                selected = qState.selectedAnswerIndex,
                                                onSelect = { choiceIndex ->
                                                    viewModel.onEvent(AnswerKeyEditorEvent.OnAnswerSelected(qState.index, choiceIndex))
                                                }
                                            )
                                        } else {
                                            val topicsForThisQuestion = viewModel.getTopicsForSubject(qState.assignedSubjectName)
                                            TopicQuestionRow(
                                                displayIndex = displayIndex,
                                                assignedSubject = qState.assignedSubjectName ?: "Bilinmiyor",
                                                selectedTopic = qState.selectedTopic,
                                                availableTopics = topicsForThisQuestion,
                                                expanded = qState.isDropdownExpanded,
                                                onToggleDropdown = {
                                                    viewModel.onEvent(AnswerKeyEditorEvent.OnToggleDropdown(qState.index, !qState.isDropdownExpanded))
                                                },
                                                onTopicSelected = { topicName, originalTopicId ->
                                                    viewModel.onEvent(
                                                        AnswerKeyEditorEvent.OnTopicSelected(
                                                            questionIndex = qState.index,
                                                            topic = topicName,
                                                            originalTopicId = originalTopicId
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
private fun TopicQuestionRow(
    displayIndex: Int,
    assignedSubject: String,
    selectedTopic: String?,
    availableTopics: List<TopicRef>,
    onToggleDropdown: () -> Unit,
    expanded: Boolean,
    onTopicSelected: (topicName: String, originalTopicId: String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Soru numarasını ve atanmış dersi göster
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.question_label, displayIndex), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "($assignedSubject)", color = Color.Gray, fontSize = 14.sp)
            }

            // Konu seçimi için tek dropdown
            Box(contentAlignment = Alignment.CenterEnd) {
                Row(
                    modifier = Modifier
                        .clickable { onToggleDropdown() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTopic ?: stringResource(id = R.string.select_topic_placeholder),
                        color = if (selectedTopic == null) Color.Gray else Color.Black
                    )
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { onToggleDropdown() }) {
                    availableTopics.forEach { topicRef ->
                        DropdownMenuItem(
                            text = { Text(text = topicRef.name) },
                            onClick = { onTopicSelected(topicRef.name, topicRef.id) }
                        )
                    }
                }
            }
        }
    }
}