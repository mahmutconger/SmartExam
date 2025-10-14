package com.anlarsinsoftware.denecoz.View.StudentView

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anlarsinsoftware.denecoz.Model.State.Publisher.SubjectDef
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamEntryEvent
import com.anlarsinsoftware.denecoz.Model.State.Student.ExamEntryNavigationEvent
import com.anlarsinsoftware.denecoz.Model.TestSection
import com.anlarsinsoftware.denecoz.Screen
import com.anlarsinsoftware.denecoz.View.Common.AnswerQuestionRow
import com.anlarsinsoftware.denecoz.ViewModel.StudentViewModel.ExamEntryViewModel

private sealed interface ExamDisplayItem {
    data class Header(val title: String) : ExamDisplayItem
    data class Question(
        val overallIndex: Int,
        val displayIndex: Int,
        val subjectId: String,
        val isAlternativeTrigger: Boolean,
        val sectionSubSubjects: List<Map<String, Any>>?
    ) : ExamDisplayItem
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamEntryScreen(
    navController: NavController,
    viewModel: ExamEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ExamEntryNavigationEvent.NavigateToResults -> {
                    navController.navigate(
                        Screen.ResultsScreen.createRoute(event.examId, event.attemptId)
                    ) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    if (uiState.showConfirmationDialog) {
        ConfirmationDialog(
            unansweredSummary = uiState.unansweredSummary,
            onConfirm = { viewModel.onEvent(ExamEntryEvent.OnConfirmSubmit) },
            onDismiss = { viewModel.onEvent(ExamEntryEvent.OnDismissDialog) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Başlığı öğrenciye uygun hale getirdik
                    Text(
                        text = uiState.examDetails?.name ?: "Cevaplarınızı Girin",
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
        // Henüz kitapçık seçilmediyse, seçim ekranını göster
        if (uiState.selectedBooklet == null) {
            BookletSelection(onSelect = { booklet ->
                viewModel.onEvent(ExamEntryEvent.OnBookletSelected(booklet))
            })
        }
        // Kitapçık seçildiyse, cevap giriş arayüzünü göster
        else if (uiState.isLoading && uiState.examDetails == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val displayItems = remember(uiState.examDetails) {
                val items = mutableListOf<ExamDisplayItem>()
                var questionStartIndex = 0
                uiState.examDetails?.subjects?.forEach { subjectMap ->
                    val section = TestSection(
                        name = subjectMap["testName"] as? String ?: "",
                        questionCount = (subjectMap["questionCount"] as? Long)?.toInt() ?: 0,
                        subSubjects = subjectMap["subSubjects"] as? List<Map<String, Any>>
                    )

                    items.add(ExamDisplayItem.Header(section.name))

                    // GÜNCELLENMİŞ MANTIK: Alt derslere göre soruları ekle
                    if (section.subSubjects.isNullOrEmpty()) {
                        // Eğer alt ders yoksa (Türkçe gibi), varsayılan olarak ilerle
                        val subjectId = section.name.lowercase() // Basit bir kimlik
                        (1..section.questionCount).forEach { index ->
                            val overallIndex = questionStartIndex + index
                            items.add(ExamDisplayItem.Question(
                                overallIndex = overallIndex, displayIndex = index,
                                subjectId = subjectId, // Yeni alanı doldur
                                isAlternativeTrigger = false, sectionSubSubjects = null
                            ))
                        }
                    } else {
                        // Eğer alt dersler VARSA (Sosyal Bilimler gibi)
                        var questionIndexInTest = 0
                        section.subSubjects.forEach { subSubjectMap ->
                            val subSubjectId = subSubjectMap["subjectId"] as String
                            val subSubjectQuestionCount = (subSubjectMap["questionCount"] as Long).toInt()

                            (1..subSubjectQuestionCount).forEach { _ ->
                                questionIndexInTest++
                                val overallIndex = questionStartIndex + questionIndexInTest
                                items.add(ExamDisplayItem.Question(
                                    overallIndex = overallIndex, displayIndex = questionIndexInTest,
                                    subjectId = subSubjectId, // Her sorunun kime ait olduğunu belirt
                                    isAlternativeTrigger = viewModel.isAlternativeTrigger(section.name, questionIndexInTest),
                                    sectionSubSubjects = section.subSubjects
                                ))
                            }
                        }
                    }
                    questionStartIndex += section.questionCount
                }
                items
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // YENİ VE DOĞRU LazyColumn YAPISI
                LazyColumn(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)) {
                    items(
                        items = displayItems,
                        key = { item ->
                            when (item) {
                                is ExamDisplayItem.Header -> item.title
                                is ExamDisplayItem.Question -> item.overallIndex
                            }
                        }
                    ) { item ->
                        when (item) {
                            is ExamDisplayItem.Header -> {
                                SubjectHeader(item.title)
                            }
                            is ExamDisplayItem.Question -> {
                                val selectedAnswer = uiState.studentAnswers[item.overallIndex]

                                val isAlternativeSubject = item.sectionSubSubjects?.find {
                                    it["subjectId"] == item.subjectId
                                }?.get("isAlternative") as? Boolean

                                val isEnabled = when (isAlternativeSubject) {
                                    null -> true // Alternatif değil, her zaman etkin
                                    else -> uiState.alternativeChoice != null && item.subjectId == uiState.alternativeChoice
                                }

                                AnswerQuestionRow(
                                    displayIndex = item.displayIndex,
                                    selected = selectedAnswer,
                                    onSelect = { choiceIndex ->
                                        viewModel.onEvent(ExamEntryEvent.OnAnswerSelected(item.overallIndex, choiceIndex))
                                    },
                                    enabled = isEnabled
                                )


                                AnimatedVisibility(visible = item.isAlternativeTrigger) {
                                    AlternativeSubjectChooser(
                                        selectedChoice = uiState.alternativeChoice,
                                        subSubjects = item.sectionSubSubjects ?: emptyList(),
                                        onChoiceSelected = { choiceId ->
                                            viewModel.onEvent(ExamEntryEvent.OnAlternativeSelected(choiceId))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Butonu öğrenciye uygun hale getirdik
                Button(
                    onClick = { viewModel.onEvent(ExamEntryEvent.OnSubmitClicked) },
                    enabled = !uiState.isLoading && uiState.studentAnswers.values.any { it != null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(text = "Denemeyi Bitir ve Sonuçları Gör")
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
private fun SubjectHeader(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AlternativeSubjectChooser(
    selectedChoice: String?,
    subSubjects: List<Map<String, Any>>,
    onChoiceSelected: (String) -> Unit
) {
    val alternativeSubjects = subSubjects.filter { it["isAlternative"] as? Boolean == true || it["isAlternative"] as? Boolean == false }

    Card(modifier = Modifier.padding(vertical = 16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Hangi bölümü cevapladın?", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                alternativeSubjects.forEach { subject ->
                    val subjectId = subject["subjectId"] as String
                    val subjectName = subject["name"] as String
                    val isSelected = selectedChoice == subjectId

                    OutlinedButton(
                        onClick = { onChoiceSelected(subjectId) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Text(subjectName, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookletSelection(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Hangi kitapçığı çözdün?", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { onSelect("A") }) { Text("A Kitapçığı") }
            Button(onClick = { onSelect("B") }) { Text("B Kitapçığı") }
        }
    }
}

@Composable
private fun ConfirmationDialog(
    unansweredSummary: Map<String, Int>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Boş Bıraktığın Sorular Var") },
        text = {
            Column {
                Text("Aşağıdaki derslerde boş bıraktığın sorular bulunuyor:")
                Spacer(modifier = Modifier.height(16.dp))
                // Hangi dersten kaç boş olduğunu listele
                unansweredSummary.forEach { (subject, count) ->
                    Text("• $subject: $count soru", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Yine de denemeyi bitirip sonuçları görmek istediğine emin misin?")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Evet, Bitir")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hayır, Devam Et")
            }
        }
    )
}