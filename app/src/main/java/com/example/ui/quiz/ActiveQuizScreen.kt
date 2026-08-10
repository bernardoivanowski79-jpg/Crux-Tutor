package com.example.ui.quiz

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.GeneratedQuiz
import com.example.data.models.QuizQuestion
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizScreen(
    viewModel: QuizViewModel,
    onNavigateHome: () -> Unit
) {
    val state by viewModel.quizState.collectAsStateWithLifecycle()
    val quiz = state.quiz ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quiz.title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Sair do Questionário")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (state.isFinished) {
                QuizResultView(
                    quiz = quiz,
                    score = state.score,
                    userAnswers = state.userAnswers,
                    onFinish = onNavigateHome
                )
            } else {
                val currentQuestion = quiz.questions.getOrNull(state.currentQuestionIndex) ?: return@Scaffold
                val totalQuestions = quiz.questions.size
                val progress = (state.currentQuestionIndex + 1).toFloat() / totalQuestions

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Questão ${state.currentQuestionIndex + 1} de $totalQuestions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = currentQuestion.type.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    // Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    // Options / Inputs
                    if (currentQuestion.type == "OPEN") {
                        OutlinedTextField(
                            value = state.openAnswerInput,
                            onValueChange = { viewModel.updateOpenAnswerInput(it) },
                            placeholder = { Text("Escreva sua resposta e raciocínio em detalhes...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("open_answer_input"),
                            enabled = !state.isAnswerChecked,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        // MULTIPLE_CHOICE or TRUE_FALSE
                        currentQuestion.options.forEachIndexed { index, optionText ->
                            val isSelected = state.currentSelectedOption == index
                            val isCorrect = index == currentQuestion.correctAnswer

                            val (bgColor, borderColor, textColor) = when {
                                state.isAnswerChecked && isCorrect -> Triple(
                                    SuccessGreen.copy(alpha = 0.15f),
                                    SuccessGreen,
                                    SuccessGreen
                                )
                                state.isAnswerChecked && isSelected && !isCorrect -> Triple(
                                    ErrorRed.copy(alpha = 0.15f),
                                    ErrorRed,
                                    ErrorRed
                                )
                                isSelected -> Triple(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                else -> Triple(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable(enabled = !state.isAnswerChecked) {
                                        viewModel.selectOption(index)
                                    }
                                    .testTag("option_$index"),
                                color = bgColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(borderColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + index).toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = borderColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (state.isAnswerChecked) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Correto", tint = SuccessGreen)
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Close, contentDescription = "Incorreto", tint = ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Explanation & Feedback Box
                    if (state.isAnswerChecked) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Explicação do Crux Tutor",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (currentQuestion.type == "OPEN" && state.openAnswerEvaluation != null) {
                                    val eval = state.openAnswerEvaluation!!
                                    Text(
                                        text = "Pontuação da IA: ${eval.scorePercentage}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (eval.isCorrect) SuccessGreen else ErrorRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = eval.feedback,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Sugestão do Professor: ${eval.suggestedImprovement}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        text = currentQuestion.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!state.isAnswerChecked) {
                        Button(
                            onClick = { viewModel.checkAnswer() },
                            enabled = !state.isEvaluatingOpenAnswer &&
                                    (state.currentSelectedOption != -1 || (currentQuestion.type == "OPEN" && state.openAnswerInput.isNotBlank())),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("check_answer_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (state.isEvaluatingOpenAnswer) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Avaliando resposta...")
                            } else {
                                Text("Verificar Resposta", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("next_question_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (state.currentQuestionIndex + 1 < totalQuestions) "Próxima Questão" else "Ver Resultado Final",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun QuizResultView(
    quiz: GeneratedQuiz,
    score: Int,
    userAnswers: List<String>,
    onFinish: () -> Unit
) {
    val total = quiz.questions.size
    val percentage = if (total > 0) (score * 100) / total else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    if (percentage >= 70) SuccessGreen.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = if (percentage >= 70) SuccessGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            text = if (percentage >= 80) "Parabéns! Excelente desempenho! 🎉"
            else if (percentage >= 50) "Bom trabalho! Continue praticando! 👍"
            else "Precisa revisar mais um pouco! 💪",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = "Você acertou $score de $total questões ($percentage%)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Revisão das Questões:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        quiz.questions.forEachIndexed { idx: Int, q: QuizQuestion ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${idx + 1}. ${q.question}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explicação: ${q.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("finish_quiz_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Voltar ao Início", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
