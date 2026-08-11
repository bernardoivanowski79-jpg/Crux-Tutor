package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatMessageEntity::class,
        QuizResultEntity::class,
        StudyLessonEntity::class,
        TopicProgressEntity::class,
        NewsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun quizDao(): QuizDao
    abstract fun studyDao(): StudyDao
    abstract fun progressDao(): ProgressDao
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crux_tutor_db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
