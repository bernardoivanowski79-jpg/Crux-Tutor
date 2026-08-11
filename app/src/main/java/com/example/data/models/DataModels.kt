package com.example.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuizQuestion(
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0,
    val type: String = "MULTIPLE_CHOICE", // MULTIPLE_CHOICE, TRUE_FALSE, OPEN
    val explanation: String = ""
)

@JsonClass(generateAdapter = true)
data class GeneratedQuiz(
    val title: String,
    val questions: List<QuizQuestion>
)

@JsonClass(generateAdapter = true)
data class LessonSection(
    val title: String,
    val content: String,
    val keyTakeaway: String = ""
)

@JsonClass(generateAdapter = true)
data class StudyLesson(
    val title: String,
    val subject: String,
    val topic: String,
    val summary: String,
    val sections: List<LessonSection>,
    val checkQuestions: List<QuizQuestion> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AnswerEvaluation(
    val isCorrect: Boolean,
    val scorePercentage: Int,
    val feedback: String,
    val suggestedImprovement: String
)

@JsonClass(generateAdapter = true)
data class RevisionRecommendation(
    val subject: String,
    val topic: String,
    val reason: String,
    val keyPointsToReview: List<String>
)

@JsonClass(generateAdapter = true)
data class GeneratedNews(
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val authorName: String = "Crux Newsroom & IA"
)
