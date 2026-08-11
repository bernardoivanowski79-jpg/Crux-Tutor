package com.example.data.repositories

import com.example.ai.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMessageEntity
import com.example.data.database.NewsEntity
import com.example.data.database.QuizResultEntity
import com.example.data.database.StudyLessonEntity
import com.example.data.database.TopicProgressEntity
import com.example.data.models.AnswerEvaluation
import com.example.data.models.GeneratedNews
import com.example.data.models.GeneratedQuiz
import com.example.data.models.RevisionRecommendation
import com.example.data.models.StudyLesson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.util.UUID

class TutorRepository(private val db: AppDatabase) {

    private val chatDao = db.chatDao()
    private val quizDao = db.quizDao()
    private val studyDao = db.studyDao()
    private val progressDao = db.progressDao()
    private val newsDao = db.newsDao()

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

    // News Operations
    val allNews: Flow<List<NewsEntity>> = newsDao.getAllNews().onStart {
        preloadInitialNewsIfEmpty()
    }

    private suspend fun preloadInitialNewsIfEmpty() = withContext(Dispatchers.IO) {
        val existing = newsDao.getAllNews().firstOrNull() ?: emptyList()
        if (existing.isEmpty()) {
            val initialList = listOf(
                NewsEntity(
                    id = UUID.randomUUID().toString(),
                    title = "ENEM 2026 & Vestibulares: Cronograma e Novas Competências",
                    summary = "Confira as datas do calendário nacional de exames, mudanças nos critérios de redação e estratégias para a nota máxima.",
                    content = "O Instituto Nacional de Estudos e Pesquisas Educacionais reforçou as diretrizes para os exames deste ano. O foco principal das bancas examinadoras nesta edição será a capacidade de articulação crítica, raciocínio em ciências humanas e resolução interdisciplinar.\n\nEspecialistas recomendam criar um cronograma semanal dividindo revisões teóricas de manhã e simulados práticos à tarde. O Crux Tutor oferece simulados com feedback de IA para ajudar você a conquistar a vaga dos seus sonhos.",
                    category = "ENEM",
                    authorName = "Crux Newsroom",
                    dateFormatted = "Hoje",
                    isAiGenerated = false,
                    timestamp = System.currentTimeMillis()
                ),
                NewsEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Inteligência Artificial no Aprendizado: Como Usar o Gemini para Estudar",
                    summary = "Descubra como tutores interativos com IA revolucionam a retenção de conceitos complexos em exames e exatas.",
                    content = "Estudos de psicologia cognitiva comprovam que a prática de recuperação ativa (active recall) combinada com explicação socrática aumenta a retenção em até 70%.\n\nFerramentas como o Crux Tutor utilizam a API do Gemini para atuar como um professor particular 24h. Em vez de simplesmente dar respostas diretas, a IA faz perguntas guiadas para ajudar o estudante a desenvolver raciocínio autônomo.",
                    category = "IA & Tecnologia",
                    authorName = "Prof. Bernardo & Crux Tech",
                    dateFormatted = "Ontem",
                    isAiGenerated = true,
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                NewsEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Técnica Pomodoro 2.0 e Ciclos Rítmicos de Alta Performance",
                    summary = "Aprenda a otimizar seus blocos de foco reduzindo a fadiga mental e aumentando a absorção em Matemática e Física.",
                    content = "Manter o foco em matérias de exatas exige pausas estratégicas. A técnica adaptada sugere 40 minutos de estudo imersivo sem distrações seguidos de 10 minutos de descanso ativo.\n\nDurante o descanso, evite redes sociais e opte por beber água ou caminhar. Essa simples mudança previne a fadiga e garante excelente retenção na memória de longo prazo.",
                    category = "Dicas de Estudo",
                    authorName = "Equipe Pedagógica Crux",
                    dateFormatted = "Há 2 dias",
                    isAiGenerated = false,
                    timestamp = System.currentTimeMillis() - 172800000L
                )
            )
            initialList.forEach { newsDao.insertNews(it) }
        }
    }

    suspend fun publishNews(
        title: String,
        summary: String,
        content: String,
        category: String,
        authorName: String,
        isAiGenerated: Boolean = false
    ): NewsEntity = withContext(Dispatchers.IO) {
        val entity = NewsEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            summary = summary,
            content = content,
            category = category,
            authorName = authorName.ifBlank { "Crux Publisher" },
            dateFormatted = "Hoje",
            isAiGenerated = isAiGenerated,
            timestamp = System.currentTimeMillis()
        )
        newsDao.insertNews(entity)
        entity
    }

    suspend fun generateAndSaveNews(
        theme: String,
        category: String,
        language: String = "pt"
    ): Result<NewsEntity> = withContext(Dispatchers.IO) {
        val result = GeminiClient.generateNewsArticle(theme, category, language)
        if (result.isSuccess) {
            val generated = result.getOrNull()!!
            val entity = publishNews(
                title = generated.title,
                summary = generated.summary,
                content = generated.content,
                category = generated.category.ifBlank { category },
                authorName = generated.authorName.ifBlank { "Crux Redação IA" },
                isAiGenerated = true
            )
            Result.success(entity)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Erro ao gerar notícia com IA."))
        }
    }

    suspend fun deleteNews(id: String) = withContext(Dispatchers.IO) {
        newsDao.deleteNews(id)
    }
}
