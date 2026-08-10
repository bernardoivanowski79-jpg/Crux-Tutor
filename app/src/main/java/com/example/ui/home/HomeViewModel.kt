package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.TutorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalQuizzes: Int = 0,
    val averageScorePercent: Int = 0,
    val totalLessons: Int = 0,
    val weakTopicsCount: Int = 0,
    val dailyTip: String = "Estudar 15 minutos por dia melhora a retenção de conteúdo em até 80%!"
)

class HomeViewModel(
    repository: TutorRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allQuizResults,
        repository.allLessons,
        repository.weakTopics
    ) { quizzes, lessons, weak ->
        val count = quizzes.size
        val avg = if (quizzes.isNotEmpty()) {
            val totalScore = quizzes.sumOf { it.score }
            val totalQuestions = quizzes.sumOf { it.totalQuestions }
            if (totalQuestions > 0) (totalScore * 100) / totalQuestions else 0
        } else 0

        HomeUiState(
            totalQuizzes = count,
            averageScorePercent = avg,
            totalLessons = lessons.size,
            weakTopicsCount = weak.size,
            dailyTip = getStudyTip()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun getStudyTip(): String {
        val tips = listOf(
            "Tente ensinar o assunto para você mesmo em voz alta para testar se realmente entendeu!",
            "Faça pausas de 5 minutos a cada 25 minutos de estudo (Técnica Pomodoro).",
            "Resolver questionários logo após estudar aumenta muito a retenção no longo prazo.",
            "Quando errar uma questão, leia a explicação atentamente antes de tentar novamente."
        )
        return tips.random()
    }
}
