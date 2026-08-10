package com.example.data.repositories

import com.example.ai.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMessageEntity
import com.example.data.database.QuizResultEntity
import com.example.data.database.StudyLessonEntity
import com.example.data.database.TopicProgressEntity
import com.example.data.models.AnswerEvaluation
import com.example.data.models.GeneratedQuiz
import com.example.data.models.RevisionRecommendation
import com.example.data.models.StudyLesson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class TutorRepository(private val db: AppDatabase) {

    private val chatDao = db.chatDao()
    private val quizDao = db.quizDao()
    private val studyDao = db.studyDao()
    private val progressDao = db.progressDao()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    fun getChatMessages(sessionId: String = "default_session"): Flow<List<ChatMessageEntity>> {
        return chatDao.getChatHistory(sessionId)
    }

    suspend fun sendMessageToTutor(
        sessionId: String = "default_session",
        userText: String
    ): Result<String> = withContext(Dispatchers.IO) {
        // Save user message
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            sender = "USER",
            text = userText
        )
        chatDao.insertMessage(userMsg)

        // Get past messages for context
        val historyList = chatDao.getChatHistory(sessionId).firstOrNull() ?: emptyList()
        val historyPairs = historyList.dropLast(1).takeLast(10).map { msg ->
            val role = if (msg.sender == "USER") "user" else "model"
            role to msg.text
        }

        val result = GeminiClient.chatWithTutor(historyPairs, userText)
        if (result.isSuccess) {
            val reply = result.getOrNull().orEmpty()
            val tutorMsg = ChatMessageEntity(
                sessionId = sessionId,
                sender = "TUTOR",
                text = reply
            )
            chatDao.insertMessage(tutorMsg)
            Result.success(reply)
        } else {
            val err = result.exceptionOrNull()?.message ?: "Erro ao consultar o professor."
            val errorMsg = ChatMessageEntity(
                sessionId = sessionId,
                sender = "TUTOR",
                text = "⚠️ $err"
            )
            chatDao.insertMessage(errorMsg)
            Result.failure(result.exceptionOrNull() ?: Exception(err))
        }
    }

    suspend fun clearChat(sessionId: String = "default_session") = withContext(Dispatchers.IO) {
        chatDao.clearHistory(sessionId)
    }

    // Quiz operations
    val allQuizResults: Flow<List<QuizResultEntity>> = quizDao.getAllQuizResults()

    suspend fun generateAndSaveQuiz(
        subject: String,
        topic: String,
        count: Int,
        difficulty: String
    ): Result<GeneratedQuiz> = withContext(Dispatchers.IO) {
        GeminiClient.generateQuiz(subject, topic, count, difficulty)
    }

    suspend fun saveQuizCompletion(
        quizTitle: String,
        subject: String,
        topic: String,
        difficulty: String,
        score: Int,
        totalQuestions: Int,
        quiz: GeneratedQuiz,
        userAnswers: List<String>
    ) = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val quizJson = moshi.adapter(GeneratedQuiz::class.java).toJson(quiz)
        val answersJson = moshi.adapter(List::class.java).toJson(userAnswers)

        val entity = QuizResultEntity(
            id = id,
            title = quizTitle,
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            score = score,
            totalQuestions = totalQuestions,
            questionsJson = quizJson,
            userAnswersJson = answersJson
        )
        quizDao.insertQuizResult(entity)

        // Update progress for this topic
        updateProgress(subject, topic, score, totalQuestions)
    }

    // Study lesson operations
    val allLessons: Flow<List<StudyLessonEntity>> = studyDao.getAllLessons()

    suspend fun generateLesson(subject: String, topic: String): Result<StudyLesson> = withContext(Dispatchers.IO) {
        val result = GeminiClient.generateStudyLesson(subject, topic)
        if (result.isSuccess) {
            val lesson = result.getOrNull()!!
            val json = moshi.adapter(StudyLesson::class.java).toJson(lesson)
            val entity = StudyLessonEntity(
                id = UUID.randomUUID().toString(),
                subject = subject,
                topic = topic,
                title = lesson.title,
                contentJson = json
            )
            studyDao.insertLesson(entity)
        }
        result
    }

    suspend fun evaluateOpenAnswer(
        question: String,
        studentAnswer: String,
        expectedExplanation: String
    ): Result<AnswerEvaluation> = withContext(Dispatchers.IO) {
        GeminiClient.evaluateOpenAnswer(question, studentAnswer, expectedExplanation)
    }

    suspend fun getRevisionRecommendations(weakTopics: List<String>): Result<List<RevisionRecommendation>> = withContext(Dispatchers.IO) {
        GeminiClient.generateRevisionPlan(weakTopics)
    }

    val weakTopics: Flow<List<TopicProgressEntity>> = progressDao.getWeakTopics()
    val allProgress: Flow<List<TopicProgressEntity>> = progressDao.getAllProgress()

    private suspend fun updateProgress(
        subject: String,
        topic: String,
        score: Int,
        totalQuestions: Int
    ) = withContext(Dispatchers.IO) {
        val key = "$subject:$topic"
        val existing = progressDao.getProgressForTopic(key)
        val newAttempts = (existing?.attemptsCount ?: 0) + 1
        val newCorrect = (existing?.correctCount ?: 0) + score
        val newTotal = (existing?.totalQuestions ?: 0) + totalQuestions

        val updated = TopicProgressEntity(
            topicKey = key,
            subject = subject,
            topic = topic,
            attemptsCount = newAttempts,
            correctCount = newCorrect,
            totalQuestions = newTotal,
            lastStudiedAt = System.currentTimeMillis()
        )
        progressDao.insertOrUpdateProgress(updated)
    }
}
