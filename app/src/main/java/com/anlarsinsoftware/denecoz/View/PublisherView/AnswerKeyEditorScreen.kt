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

                Spacer(modifier = Modifier.height(12.dp))

                // TODO: Bu bölümün state yönetimini de ViewModel'e taşıyabiliriz. Şimdilik UI'da kalabilir.
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max=200.dp)) {
                    items(uiState.subjects) { subject ->
                        SubjectRow(
                            subject = subject,
                            assigned = 0,
                            expandedState = false,
                            onToggleExpand = { /* TODO */ }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(uiState.questions) { qState ->
                        if (uiState.mode == EditorMode.ANSWER_KEY) {
                            AnswerQuestionRow(
                                index = qState.index,
                                selected = qState.selectedAnswerIndex,
                                onSelect = { choiceIndex ->
                                    viewModel.onEvent(AnswerKeyEditorEvent.OnAnswerSelected(qState.index, choiceIndex))
                                }
                            )
                        } else {
                            TopicQuestionRow(
                                index = qState.index,
                                selectedTopic = qState.selectedTopic,
                                onToggleDropdown = {
                                    viewModel.onEvent(AnswerKeyEditorEvent.OnToggleDropdown(qState.index, !qState.isDropdownExpanded))
                                },
                                expanded = qState.isDropdownExpanded,
                                allSubjects = uiState.subjects,
                                onTopicSelected = { topic ->
                                    viewModel.onEvent(AnswerKeyEditorEvent.OnTopicSelected(qState.index, topic))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        viewModel.onEvent(AnswerKeyEditorEvent.OnConfirmClicked)
                    },
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
    index: Int,
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
            Text(text = stringResource(id = R.string.question_label, index), modifier = Modifier.weight(0.35f))
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

@Composable
private fun TopicQuestionRow(
    index: Int,
    selectedTopic: String?,
    onToggleDropdown: () -> Unit,
    expanded: Boolean,
    allSubjects: List<SubjectDef>,
    onTopicSelected: (String) -> Unit
) {
    // Flatten all topics into a list with their subject reference (show subject name in dropdown optional)
    val flatTopics = remember(allSubjects) {
        allSubjects.flatMap { subj -> subj.topics.map { topic -> Pair(subj.name, topic) } }
    }

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
            Text(text = stringResource(id = R.string.question_label, index), modifier = Modifier.weight(0.35f))
            Spacer(modifier = Modifier.width(8.dp))

            // Custom dropdown area
            Box(modifier = Modifier.weight(0.65f), contentAlignment = Alignment.CenterEnd) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDropdown() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedTopic ?: stringResource(id = R.string.select_topic_placeholder),
                        color = if (selectedTopic == null) Color.Gray else Color.Black
                    )
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                }

                // Dropdown menu
                DropdownMenu(expanded = expanded, onDismissRequest = { onToggleDropdown() }) {
                    flatTopics.forEach { (subjectName, topic) ->
                        DropdownMenuItem(text = {
                            Column {
                                Text(text = topic)
                                Text(text = subjectName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }, onClick = {
                            onTopicSelected(topic)
                        })
                    }
                }
            }
        }
    }
}
