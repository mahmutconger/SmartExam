package com.anlarsinsoftware.denecoz.View.PublisherView

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.R

// Mode: Answer key editing (A/B/C...) or Topic distribution (topic dropdown)
enum class Mode { ANSWER_KEY, TOPIC_DISTRIBUTION }

// Question state holder (compose-friendly)
class QuestionState(val index: Int) {
    var selectedAnswer by mutableStateOf<Int?>(null) // 0..4 for A..E
    var selectedTopic by mutableStateOf<String?>(null)
    var dropdownExpanded by mutableStateOf(false)
}

// Subject + topics
data class SubjectDef(val id: Int, val name: String, val total: Int, val topics: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerKeyEditorScreen(
    mode: Mode = Mode.ANSWER_KEY,
    questionCount: Int = 40,
    onBack: () -> Unit = {},
    navController: NavController
) {
    val context = LocalContext.current

    // Sample subjects + topics (you can load dynamically from backend)
    val subjects = remember {
        mutableStateListOf<SubjectDef>()
    }
    LaunchedEffect(Unit) {
        subjects.addAll(
            listOf(
                SubjectDef(1, context.getString(R.string.subject_turkish), 40, listOf("Dil Bilgisi", "Okuma", "Anlatım", "Madde ve Halleri")),
                SubjectDef(2, context.getString(R.string.subject_social), 20, listOf("Tarih", "Coğrafya")),
                SubjectDef(3, context.getString(R.string.subject_math), 40, listOf("Sayilar", "Geometri", "Fonksiyon")),
                SubjectDef(4, context.getString(R.string.subject_science), 20, listOf("Hareket ve Kuvvet", "Madde ve Halleri", "Dinamik"))
            )
        )
    }


    // question states
    val questions = remember {
        List(questionCount) { QuestionState(it + 1) }.toMutableStateList()
    }

    // subject expansion state
    val subjectExpandedStates = remember {
        subjects.map { mutableStateOf(false) }
    }

    // Helper: compute assigned counts per subject (based on selectedTopic)
    val assignedCounts by remember {
        derivedStateOf {
            subjects.mapIndexed { idx, subject ->
                val count = questions.count { q ->
                    val t = q.selectedTopic
                    t != null && subject.topics.contains(t)
                }
                count
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (mode) {
                            Mode.ANSWER_KEY -> stringResource(id = R.string.title_create_answer_key)
                            Mode.TOPIC_DISTRIBUTION -> stringResource(id = R.string.title_topic_distribution)
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)) {

            Spacer(modifier = Modifier.height(12.dp))

            // Subjects summary list (top area)
            Column(modifier = Modifier.fillMaxWidth()) {
                subjects.forEachIndexed { idx, subject ->
                    SubjectRow(
                        subject = subject,
                        assigned = assignedCounts.getOrNull(idx) ?: 0,
                        expandedState = subjectExpandedStates[idx].value,
                        onToggleExpand = { subjectExpandedStates[idx].value = !subjectExpandedStates[idx].value }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Questions list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                itemsIndexed(questions) { idx, qState ->
                    if (mode == Mode.ANSWER_KEY) {
                        AnswerQuestionRow(
                            index = qState.index,
                            selected = qState.selectedAnswer,
                            onSelect = { choiceIndex -> qState.selectedAnswer = choiceIndex }
                        )
                    } else {
                        // Topic distribution
                        TopicQuestionRow(
                            index = qState.index,
                            selectedTopic = qState.selectedTopic,
                            onToggleDropdown = { qState.dropdownExpanded = !qState.dropdownExpanded },
                            expanded = qState.dropdownExpanded,
                            allSubjects = subjects,
                            onTopicSelected = { topic ->
                                qState.selectedTopic = topic
                                qState.dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Confirm button at bottom (above bottom nav)
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    // Example behavior: collect results and continue.
                    // In real app, validate & call ViewModel.
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(text = stringResource(id = R.string.confirm_continue))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/** Subject row showing name, assigned/total and expand chevron **/
@Composable
private fun SubjectRow(
    subject: SubjectDef,
    assigned: Int,
    expandedState: Boolean,
    onToggleExpand: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = subject.name, modifier = Modifier.weight(1f), fontSize = 16.sp)
            Text(text = "$assigned/${subject.total}", color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}

/** Question row for Answer Key mode: left Soru N, right 5 circular choices **/
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

/** Question row for Topic Distribution: left Soru N, right dropdown showing chosen topic **/
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
