package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String = "default_session",
    val sender: String, // "USER" or "TUTOR"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val score: Int,
    val totalQuestions: Int,
    val questionsJson: String,
    val userAnswersJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_lessons")
data class StudyLessonEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val topic: String,
    val title: String,
    val contentJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_progress")
data class TopicProgressEntity(
    @PrimaryKey val topicKey: String, // "Subject:Topic"
    val subject: String,
    val topic: String,
    val attemptsCount: Int = 0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val lastStudiedAt: Long = System.currentTimeMillis()
)
