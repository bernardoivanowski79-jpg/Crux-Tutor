package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repositories.TutorRepository
import com.example.ui.chat.ChatViewModel
import com.example.ui.home.HomeViewModel
import com.example.ui.profile.ProfileViewModel
import com.example.ui.quiz.QuizViewModel
import com.example.ui.revision.RevisionViewModel
import com.example.ui.study.StudyViewModel

class AppViewModelFactory(
    private val repository: TutorRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> ChatViewModel(repository) as T
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> QuizViewModel(repository) as T
            modelClass.isAssignableFrom(StudyViewModel::class.java) -> StudyViewModel(repository) as T
            modelClass.isAssignableFrom(RevisionViewModel::class.java) -> RevisionViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
