package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getChatHistory(sessionId: String = "default_session"): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearHistory(sessionId: String = "default_session")
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: String): QuizResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity)
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_lessons ORDER BY createdAt DESC")
    fun getAllLessons(): Flow<List<StudyLessonEntity>>

    @Query("SELECT * FROM study_lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): StudyLessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: StudyLessonEntity)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM topic_progress ORDER BY lastStudiedAt DESC")
    fun getAllProgress(): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE topicKey = :topicKey LIMIT 1")
    suspend fun getProgressForTopic(topicKey: String): TopicProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: TopicProgressEntity)

    @Query("SELECT * FROM topic_progress WHERE totalQuestions > 0 AND (CAST(correctCount AS FLOAT) / totalQuestions) < 0.7 ORDER BY (CAST(correctCount AS FLOAT) / totalQuestions) ASC")
    fun getWeakTopics(): Flow<List<TopicProgressEntity>>
}
