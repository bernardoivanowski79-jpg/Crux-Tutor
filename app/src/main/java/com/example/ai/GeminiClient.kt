package com.example.ai

import com.example.BuildConfig
import com.example.data.models.AnswerEvaluation
import com.example.data.models.GeneratedNews
import com.example.data.models.GeneratedQuiz
import com.example.data.models.RevisionRecommendation
import com.example.data.models.StudyLesson
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    fun getApiKey(): String {
        return ApiKeyManager.getApiKey()
    }

    fun getSelectedModel(): String {
        return ApiKeyManager.getSelectedModel()
    }

    /**
     * Send chat prompt with conversation history and Crux Tutor system prompt.
     */
    suspend fun chatWithTutor(
        history: List<Pair<String, String>>, // role ("user"/"model") to text
        userMessage: String
    ): Result<String> {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalStateException("Chave da API do Gemini não configurada. Configure a chave GEMINI_API_KEY no painel Secrets do AI Studio."))
        }
        val model = getSelectedModel()

        val contentsList = mutableListOf<Content>()
        history.forEach { (role, text) ->
            contentsList.add(
                Content(
                    role = role,
                    parts = listOf(Part(text = text))
                )
            )
        }
        contentsList.add(
            Content(
                role = "user",
                parts = listOf(Part(text = userMessage))
            )
        )

        val request = GenerateContentRequest(
            contents = contentsList,
            systemInstruction = Content(
                parts = listOf(Part(text = Prompts.SYSTEM_PROMPT))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!replyText.isNullOrBlank()) {
                Result.success(replyText)
            } else {
                Result.failure(Exception("O professor Gemini não retornou resposta. Tente novamente."))
            }
        } catch (e: Exception) {
            Result.failure(mapApiError(e))
        }
    }

    /**
     * Generate a structured quiz JSON and parse it into [GeneratedQuiz].
     */
    suspend fun generateQuiz(
        subject: String,
        topic: String,
        count: Int,
        difficulty: String
    ): Result<GeneratedQuiz> {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalStateException("Chave da API do Gemini não configurada."))
        }
        val model = getSelectedModel()

        val prompt = Prompts.quizGenerationPrompt(subject, topic, count, difficulty)
        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = Prompts.SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(
                temperature = 0.4f,
                responseMimeType = "application/json"
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            val jsonText = cleanJsonResponse(response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "")
            if (jsonText.isNotBlank()) {
                val adapter = moshi.adapter(GeneratedQuiz::class.java)
                val quiz = adapter.fromJson(jsonText)
                if (quiz != null && quiz.questions.isNotEmpty()) {
                    Result.success(quiz)
                } else {
                    Result.failure(Exception("Formato de questionário inválido retornado pela IA."))
                }
            } else {
                Result.failure(Exception("O Gemini não gerou o questionário. Tente novamente."))
            }
        } catch (e: Exception) {
            Result.failure(mapApiError(e))
        }
    }

    /**
     * Generate a micro study lesson and parse into [StudyLesson].
     */
    suspend fun generateStudyLesson(
        subject: String,
        topic: String
    ): Result<StudyLesson> {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalStateException("Chave da API do Gemini não configurada."))
        }
        val model = getSelectedModel()

        val prompt = Prompts.studyLessonPrompt(subject, topic)
        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = Prompts.SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(
                temperature = 0.5f,
                responseMimeType = "application/json"
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            val jsonText = cleanJsonResponse(response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "")
            if (jsonText.isNotBlank()) {
                val adapter = moshi.adapter(StudyLesson::class.java)
                val lesson = adapter.fromJson(jsonText)
                if (lesson != null) {
                    Result.success(lesson)
                } else {
                    Result.failure(Exception("Erro ao processar conteúdo da aula."))
                }
            } else {
                Result.failure(Exception("Não foi possível gerar a aula."))
            }
        } catch (e: Exception) {
            Result.failure(mapApiError(e))
        }
    }

    /**
     * Evaluate an open-ended question answer from the student.
     */
    suspend fun evaluateOpenAnswer(
        question: String,
        studentAnswer: String,
        expectedExplanation: String
    ): Result<AnswerEvaluation> {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalStateException("Chave da API do Gemini não configurada."))
        }
        val model = getSelectedModel()

        val prompt = Prompts.evaluateOpenAnswerPrompt(question, studentAnswer, expectedExplanation)
        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = Prompts.SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                responseMimeType = "application/json"
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            val jsonText = cleanJsonResponse(response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "")
            val adapter = moshi.adapter(AnswerEvaluation::class.java)
            val eval = adapter.fromJson(jsonText)
            if (eval != null) {
                Result.success(eval)
            } else {
                Result.failure(Exception("Erro ao avaliar a resposta."))
            }
        } catch (e: Exception) {
            Result.failure(mapApiError(e))
        }
    }

    /**
     * Generate revision recommendations for weak topics.
     */
    suspend fun generateRevisionPlan(weakTopics: List<String>): Result<List<RevisionRecommendation>> {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalStateException("Chave da API do Gemini não configurada."))
        }
        val model = getSelectedModel()

        val prompt = Prompts.revisionPlanPrompt(weakTopics)
        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = Prompts.SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(
                temperature = 0.4f,
                responseMimeType = "application/json"
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            val jsonText = cleanJsonResponse(response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "")
            val type = Types.newParameterizedType(List::class.java, RevisionRecommendation::class.java)
            val adapter = moshi.adapter<List<RevisionRecommendation>>(type)
            val recommendations = adapter.fromJson(jsonText)
            if (recommendations != null) {
                Result.success(recommendations)
            } else {
                Result.failure(Exception("Não foi possível gerar as sugestões de revisão."))
            }
        } catch (e: Exception) {
            Result.failure(mapApiError(e))
        }
    }

    /**
     * Generate an educational news article using AI.
     */
    suspend fun generateNewsArticle(
        theme: String,
        category: String,
        language: String
    ): Result<GeneratedNews> {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalStateException("Chave da API do Gemini não configurada."))
        }
        val model = getSelectedModel()

        val prompt = Prompts.newsGenerationPrompt(theme, category, language)
        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = Prompts.SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(
                temperature = 0.6f,
                responseMimeType = "application/json"
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            val jsonText = cleanJsonResponse(response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "")
            val adapter = moshi.adapter(GeneratedNews::class.java)
            val news = adapter.fromJson(jsonText)
            if (news != null) {
                Result.success(news)
            } else {
                Result.failure(Exception("Não foi possível gerar o artigo de notícia com IA."))
            }
        } catch (e: Exception) {
            Result.failure(mapApiError(e))
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    private fun mapApiError(e: Exception): Exception {
        val msg = e.message ?: ""
        return when {
            msg.contains("401") || msg.contains("API key") -> Exception("Chave da API inválida. Verifique sua chave no Secrets do AI Studio.")
            msg.contains("429") || msg.contains("ResourceHasExhausted") -> Exception("Limite de requisições atingido. Aguarde alguns instantes e tente novamente.")
            msg.contains("Unable to resolve host") || msg.contains("ConnectException") -> Exception("Sem conexão com a internet. Verifique sua rede.")
            msg.contains("SocketTimeoutException") -> Exception("Tempo de resposta esgotado. Tente uma pergunta mais curta ou tente novamente.")
            else -> Exception("Erro de comunicação com Crux Tutor: ${e.localizedMessage}")
        }
    }
}
