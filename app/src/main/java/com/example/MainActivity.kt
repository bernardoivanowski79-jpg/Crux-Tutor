package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.ai.ApiKeyManager
import com.example.data.database.AppDatabase
import com.example.data.repositories.TutorRepository
import com.example.ui.AppViewModelFactory
import com.example.ui.chat.ChatViewModel
import com.example.ui.home.HomeViewModel
import com.example.ui.navigation.CruxMainApp
import com.example.ui.news.NewsViewModel
import com.example.ui.profile.ProfileViewModel
import com.example.ui.quiz.QuizViewModel
import com.example.ui.revision.RevisionViewModel
import com.example.ui.study.StudyViewModel
import com.example.ui.theme.CruxTutorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ApiKeyManager.init(applicationContext)
        com.example.data.StudentScheduleManager.init(applicationContext)

        val db = AppDatabase.getInstance(applicationContext)
        val repository = TutorRepository(db)
        val factory = AppViewModelFactory(repository)

        val homeViewModel: HomeViewModel by viewModels { factory }
        val chatViewModel: ChatViewModel by viewModels { factory }
        val quizViewModel: QuizViewModel by viewModels { factory }
        val studyViewModel: StudyViewModel by viewModels { factory }
        val revisionViewModel: RevisionViewModel by viewModels { factory }
        val profileViewModel: ProfileViewModel by viewModels { factory }
        val newsViewModel: NewsViewModel by viewModels { factory }

        setContent {
            CruxTutorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    CruxMainApp(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        chatViewModel = chatViewModel,
                        quizViewModel = quizViewModel,
                        studyViewModel = studyViewModel,
                        revisionViewModel = revisionViewModel,
                        profileViewModel = profileViewModel,
                        newsViewModel = newsViewModel
                    )
                }
            }
        }
    }
}
