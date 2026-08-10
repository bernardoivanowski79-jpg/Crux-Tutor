package com.example.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.AnswerEvaluation
import com.example.data.models.GeneratedQuiz
import com.example.data.repositories.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizConfigUiState(
    val subject: String = "Matemática",
    val customSubject: String = "",
    val topic: String = "Equação do 2º Grau",
    val questionCount: Int = 5,
    val difficulty: String = "Médio",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)

data class ActiveQuizUiState(
    val quiz: GeneratedQuiz? = null,
    val currentQuestionIndex: Int = 0,
    val userAnswers: MutableList<String> = mutableListOf(),
    val isAnswerChecked: Boolean = false,
    val currentSelectedOption: Int = -1,
    val openAnswerInput: String = "",
    val openAnswerEvaluation: AnswerEvaluation? = null,
    val isEvaluatingOpenAnswer: Boolean = false,
    val score: Int = 0,
    val isFinished: Boolean = false
)

class QuizViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    private val _configState = MutableStateFlow(QuizConfigUiState())
    val configState: StateFlow<QuizConfigUiState> = _configState.asStateFlow()

    private val _quizState = MutableStateFlow(ActiveQuizUiState())
    val quizState: StateFlow<ActiveQuizUiState> = _quizState.asStateFlow()

    fun updateSubject(subject: String) {
        _configState.value = _configState.value.copy(subject = subject)
    }

    fun updateCustomSubject(text: String) {
        _configState.value = _configState.value.copy(customSubject = text)
    }

    fun updateTopic(topic: String) {
        _configState.value = _configState.value.copy(topic = topic)
    }

    fun updateQuestionCount(count: Int) {
        _configState.value = _configState.value.copy(questionCount = count)
    }

    fun updateDifficulty(diff: String) {
        _configState.value = _configState.value.copy(difficulty = diff)
    }

    fun generateQuiz(onSuccess: () -> Unit) {
        val subjectToUse = if (_configState.value.subject == "Outro")
            _configState.value.customSubject.ifBlank { "Geral" }
        else _configState.value.subject

        val topicToUse = _configState.value.topic.ifBlank { "Conceitos Gerais" }

        _configState.value = _configState.value.copy(isGenerating = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.generateAndSaveQuiz(
                subject = subjectToUse,
                topic = topicToUse,
                count = _configState.value.questionCount,
                difficulty = _configState.value.difficulty
            )

            if (result.isSuccess) {
                val generated = result.getOrNull()!!
                _configState.value = _configState.value.copy(isGenerating = false)
                _quizState.value = ActiveQuizUiState(
                    quiz = generated,
                    userAnswers = MutableList(generated.questions.size) { "" }
                )
                onSuccess()
            } else {
                _configState.value = _configState.value.copy(
                    isGenerating = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Erro ao gerar questionário."
                )
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        _quizState.value = _quizState.value.copy(currentSelectedOption = optionIndex)
    }

    fun updateOpenAnswerInput(text: String) {
        _quizState.value = _quizState.value.copy(openAnswerInput = text)
    }

    fun checkAnswer() {
        val currentQuiz = _quizState.value.quiz ?: return
        val currentIdx = _quizState.value.currentQuestionIndex
        val question = currentQuiz.questions.getOrNull(currentIdx) ?: return

        if (question.type == "OPEN") {
            // Evaluate open answer via AI
            _quizState.value = _quizState.value.copy(isEvaluatingOpenAnswer = true)
            viewModelScope.launch {
                val evalResult = repository.evaluateOpenAnswer(
                    question = question.question,
                    studentAnswer = _quizState.value.openAnswerInput,
                    expectedExplanation = question.explanation
                )
                if (evalResult.isSuccess) {
                    val eval = evalResult.getOrNull()!!
                    val newAnswers = _quizState.value.userAnswers.toMutableList()
                    newAnswers[currentIdx] = _quizState.value.openAnswerInput

                    var newScore = _quizState.value.score
                    if (eval.isCorrect) {
                        newScore += 1
                    }

                    _quizState.value = _quizState.value.copy(
                        isAnswerChecked = true,
                        isEvaluatingOpenAnswer = false,
                        openAnswerEvaluation = eval,
                        userAnswers = newAnswers,
                        score = newScore
                    )
                } else {
                    _quizState.value = _quizState.value.copy(
                        isEvaluatingOpenAnswer = false,
                        isAnswerChecked = true,
                        openAnswerEvaluation = AnswerEvaluation(
                            isCorrect = true,
                            scorePercentage = 80,
                            feedback = "Sua resposta foi registrada.",
                            suggestedImprovement = question.explanation
                        )
                    )
                }
            }
        } else { // MULTIPLE_CHOICE or TRUE_FALSE
            val selectedOption = _quizState.value.currentSelectedOption
            if (selectedOption == -1) return

            val isCorrect = selectedOption == question.correctAnswer
            val newAnswers = _quizState.value.userAnswers.toMutableList()
            newAnswers[currentIdx] = "$selectedOption"

            var newScore = _quizState.value.score
            if (isCorrect) newScore += 1

            _quizState.value = _quizState.value.copy(
                isAnswerChecked = true,
                userAnswers = newAnswers,
                score = newScore
            )
        }
    }

    fun nextQuestion() {
        val currentQuiz = _quizState.value.quiz ?: return
        val currentIdx = _quizState.value.currentQuestionIndex

        if (currentIdx + 1 < currentQuiz.questions.size) {
            _quizState.value = _quizState.value.copy(
                currentQuestionIndex = currentIdx + 1,
                isAnswerChecked = false,
                currentSelectedOption = -1,
                openAnswerInput = "",
                openAnswerEvaluation = null
            )
        } else {
            // Finish Quiz and save
            _quizState.value = _quizState.value.copy(isFinished = true)
            viewModelScope.launch {
                val subjectToUse = if (_configState.value.subject == "Outro")
                    _configState.value.customSubject
                else _configState.value.subject

                repository.saveQuizCompletion(
                    quizTitle = currentQuiz.title,
                    subject = subjectToUse,
                    topic = _configState.value.topic,
                    difficulty = _configState.value.difficulty,
                    score = _quizState.value.score,
                    totalQuestions = currentQuiz.questions.size,
                    quiz = currentQuiz,
                    userAnswers = _quizState.value.userAnswers
                )
            }
        }
    }

    fun resetQuiz() {
        _quizState.value = ActiveQuizUiState()
    }
}
