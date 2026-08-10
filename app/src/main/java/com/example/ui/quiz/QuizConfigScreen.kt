package com.example.ui.quiz

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ApiKeyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val state by viewModel.configState.collectAsStateWithLifecycle()
    val currentLang by ApiKeyManager.appLanguageFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
        }
    }

    val subjects = when (currentLang) {
        "pt" -> listOf("Matemática", "Física", "Química", "Biologia", "História", "Geografia", "Português", "Inglês", "Outro")
        "es" -> listOf("Matemáticas", "Física", "Química", "Biología", "Historia", "Geografía", "Español", "Inglés", "Otro")
        else -> listOf("Math", "Physics", "Chemistry", "Biology", "History", "Geography", "English", "Other")
    }

    val difficulties = when (currentLang) {
        "pt" -> listOf("Fácil", "Médio", "Difícil")
        "es" -> listOf("Fácil", "Medio", "Difícil")
        else -> listOf("Easy", "Medium", "Hard")
    }

    val questionCounts = listOf(3, 5, 10, 15)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLang) {
                            "pt" -> "Criar Simulado com IA"
                            "es" -> "Crear Cuestionario IA"
                            else -> "Generate AI Quiz"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Subject
            Text(
                text = when (currentLang) {
                    "pt" -> "1. Selecione a Matéria"
                    "es" -> "1. Selecciona la Materia"
                    else -> "1. Select Subject"
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
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (state.subject == "Outro" || state.subject == "Otro" || state.subject == "Other") {
                OutlinedTextField(
                    value = state.customSubject,
                    onValueChange = { viewModel.updateCustomSubject(it) },
                    label = {
                        Text(
                            when (currentLang) {
                                "pt" -> "Nome da Matéria Personalizada"
                                "es" -> "Nombre de la Materia Personalizada"
                                else -> "Custom Subject Name"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Topic
            Text(
                text = when (currentLang) {
                    "pt" -> "2. Informe o Assunto"
                    "es" -> "2. Ingrese el Tema"
                    else -> "2. Enter Topic"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            OutlinedTextField(
                value = state.topic,
                onValueChange = { viewModel.updateTopic(it) },
                label = {
                    Text(
                        when (currentLang) {
                            "pt" -> "Ex: Equação do 2º Grau, Revolução Francesa..."
                            "es" -> "Ej: Ecuación Cuadrática, Revolución Francesa..."
                            else -> "Ex: Quadratic Equations, World War II..."
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_topic_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Question Count
            Text(
                text = when (currentLang) {
                    "pt" -> "3. Quantidade de Questões"
                    "es" -> "3. Cantidad de Preguntas"
                    else -> "3. Number of Questions"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                questionCounts.forEach { count ->
                    FilterChip(
                        selected = state.questionCount == count,
                        onClick = { viewModel.updateQuestionCount(count) },
                        label = {
                            Text(
                                when (currentLang) {
                                    "pt" -> "$count questões"
                                    "es" -> "$count preguntas"
                                    else -> "$count questions"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag("quiz_count_$count")
                    )
                }
            }

            // Difficulty
            Text(
                text = when (currentLang) {
                    "pt" -> "4. Nível de Dificuldade"
                    "es" -> "4. Nivel de Dificultad"
                    else -> "4. Difficulty Level"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                difficulties.forEach { diff ->
                    FilterChip(
                        selected = state.difficulty == diff,
                        onClick = { viewModel.updateDifficulty(diff) },
                        label = { Text(diff, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generate Button
            Button(
                onClick = { viewModel.generateQuiz(onSuccess = onStartQuiz) },
                enabled = !state.isGenerating && state.topic.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_quiz_button"),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        when (currentLang) {
                            "pt" -> "O Gemini está gerando o simulado..."
                            "es" -> "Gemini está generando las preguntas..."
                            else -> "Gemini is generating questions..."
                        }
                    )
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (currentLang) {
                            "pt" -> "Gerar Simulado com IA"
                            "es" -> "Generar Cuestionario con IA"
                            else -> "Generate Quiz with AI"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
