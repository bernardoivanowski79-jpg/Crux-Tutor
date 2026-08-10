package com.example.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.QuizResultEntity
import com.example.data.database.StudyLessonEntity
import com.example.data.database.TopicProgressEntity
import com.example.data.repositories.TutorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val totalQuizzes: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val averageScorePercent: Int = 0,
    val quizResults: List<QuizResultEntity> = emptyList(),
    val lessons: List<StudyLessonEntity> = emptyList(),
    val progressList: List<TopicProgressEntity> = emptyList()
)

class ProfileViewModel(
    repository: TutorRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.allQuizResults,
        repository.allLessons,
        repository.allProgress
    ) { quizzes, lessons, progress ->
        val totalQuizzes = quizzes.size
        val totalQuestions = quizzes.sumOf { it.totalQuestions }
        val totalCorrect = quizzes.sumOf { it.score }
        val avg = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0

        ProfileUiState(
            totalQuizzes = totalQuizzes,
            totalQuestionsAnswered = totalQuestions,
            totalCorrect = totalCorrect,
            averageScorePercent = avg,
            quizResults = quizzes,
            lessons = lessons,
            progressList = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )
}
