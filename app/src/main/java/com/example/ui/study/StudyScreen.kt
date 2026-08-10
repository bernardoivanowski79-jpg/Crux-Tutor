package com.example.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ApiKeyManager
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLang by ApiKeyManager.appLanguageFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val subjects = when (currentLang) {
        "pt" -> listOf("Matemática", "Física", "Química", "Biologia", "História", "Geografia", "Português", "Inglês")
        "es" -> listOf("Matemáticas", "Física", "Química", "Biología", "Historia", "Geografía", "Español", "Inglés")
        else -> listOf("Math", "Physics", "Chemistry", "Biology", "History", "Geography", "English")
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLang) {
                            "pt" -> "Modo Estudo e Aulas"
                            "es" -> "Modo Estudio y Lecciones"
                            else -> "Study & Lessons"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.currentLesson != null) {
                        IconButton(onClick = { viewModel.resetLesson() }) {
                            Icon(Icons.Default.Book, contentDescription = "New Lesson")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (state.currentLesson == null) {
                // Topic Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Informe o assunto e o Crux Tutor gerará uma aula didática com explicações e exercícios!"
                                    "es" -> "¡Ingresa el tema y Crux Tutor generará una lección didáctica estructurada!"
                                    else -> "Enter any topic and Crux Tutor will generate a structured lesson with key takeaways and practice questions!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = when (currentLang) {
                            "pt" -> "Selecione a Matéria"
                            "es" -> "Selecciona la Materia"
                            else -> "Select Subject"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(subjects) { subj ->
                            FilterChip(
                                selected = state.subject == subj,
                                onClick = { viewModel.updateSubject(subj) },
                                label = { Text(subj, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    Text(
                        text = when (currentLang) {
                            "pt" -> "Qual assunto deseja aprender?"
                            "es" -> "¿Qué tema deseas aprender?"
                            else -> "What topic do you want to learn?"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = state.topicInput,
                        onValueChange = { viewModel.updateTopicInput(it) },
                        label = {
                            Text(
                                when (currentLang) {
                                    "pt" -> "Ex: Teorema de Pitágoras, Ciclo da Água..."
                                    "es" -> "Ej: Teorema de Pitágoras, Ciclo del Agua..."
                                    else -> "Ex: Pythagorean Theorem, Water Cycle..."
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_topic_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Button(
                        onClick = { viewModel.generateLesson() },
                        enabled = !state.isLoading && state.topicInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("create_lesson_button"),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                when (currentLang) {
                                    "pt" -> "Criando sua aula com IA..."
                                    "es" -> "Creando tu lección con IA..."
                                    else -> "Generating lesson with AI..."
                                }
                            )
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Gerar Aula Didática"
                                    "es" -> "Generar Lección"
                                    else -> "Generate Structured Lesson"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                // Lesson Viewer
                val lesson = state.currentLesson!!
                val currentSection = lesson.sections.getOrNull(state.currentSectionIndex)
                val totalSections = lesson.sections.size

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = lesson.subject,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        "pt" -> "Parte ${state.currentSectionIndex + 1} de $totalSections"
                                        "es" -> "Parte ${state.currentSectionIndex + 1} de $totalSections"
                                        else -> "Part ${state.currentSectionIndex + 1} of $totalSections"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lesson.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Section Content Card
                    if (currentSection != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = currentSection.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = currentSection.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )

                                if (currentSection.keyTakeaway.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = currentSection.keyTakeaway,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section Pager Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousSection() },
                            enabled = state.currentSectionIndex > 0
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                            Text(
                                when (currentLang) {
                                    "pt" -> "Anterior"
                                    "es" -> "Anterior"
                                    else -> "Previous"
                                }
                            )
                        }

                        Button(
                            onClick = { viewModel.nextSection() },
                            enabled = state.currentSectionIndex + 1 < totalSections
                        ) {
                            Text(
                                when (currentLang) {
                                    "pt" -> "Próxima Parte"
                                    "es" -> "Siguiente Parte"
                                    else -> "Next Part"
                                }
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }

                    // Check Question at bottom if available
                    val checkQuestion = lesson.checkQuestions.firstOrNull()
                    if (checkQuestion != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (currentLang) {
                                "pt" -> "Exercício de Fixação"
                                "es" -> "Ejercicio de Fijación"
                                else -> "Practice Exercise"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = checkQuestion.question, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(12.dp))

                                checkQuestion.options.forEachIndexed { idx, opt ->
                                    val isSelected = state.selectedCheckOption == idx
                                    val isCorrect = idx == checkQuestion.correctAnswer

                                    val (bgColor, borderColor) = when {
                                        state.isCheckChecked && isCorrect -> SuccessGreen.copy(alpha = 0.2f) to SuccessGreen
                                        state.isCheckChecked && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.2f) to ErrorRed
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                            .clickable(enabled = !state.isCheckChecked) {
                                                viewModel.selectCheckOption(idx)
                                            },
                                        color = bgColor
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${('A' + idx)}. $opt",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (state.isCheckChecked && isCorrect) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                if (!state.isCheckChecked) {
                                    Button(
                                        onClick = { viewModel.checkCheckAnswer() },
                                        enabled = state.selectedCheckOption != -1,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            when (currentLang) {
                                                "pt" -> "Verificar Resposta"
                                                "es" -> "Verificar Respuesta"
                                                else -> "Check Answer"
                                            }
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "${if (currentLang == "pt") "Explicação:" else if (currentLang == "es") "Explicación:" else "Explanation:"} ${checkQuestion.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
