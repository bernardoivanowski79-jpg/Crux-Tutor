package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ai.ApiKeyManager
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.quiz.ActiveQuizScreen
import com.example.ui.quiz.QuizConfigScreen
import com.example.ui.quiz.QuizViewModel
import com.example.ui.revision.RevisionScreen
import com.example.ui.revision.RevisionViewModel
import com.example.ui.study.StudyScreen
import com.example.ui.study.StudyViewModel

object Screen {
    const val HOME = "home"
    const val CHAT = "chat"
    const val QUIZ_CONFIG = "quiz_config"
    const val ACTIVE_QUIZ = "active_quiz"
    const val STUDY = "study"
    const val REVISION = "revision"
    const val PROFILE = "profile"
}

data class BottomNavItem(
    val route: String,
    val titleEn: String,
    val titlePt: String,
    val titleEs: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.HOME, "Home", "Início", "Inicio", Icons.Default.Home, Icons.Outlined.Home, "nav_item_home"),
    BottomNavItem(Screen.CHAT, "AI Tutor", "Professor", "Tutor IA", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat, "nav_item_chat"),
    BottomNavItem(Screen.QUIZ_CONFIG, "Quiz", "Simulado", "Cuestionario", Icons.Default.Quiz, Icons.Outlined.Quiz, "nav_item_quiz"),
    BottomNavItem(Screen.STUDY, "Study", "Aulas", "Lecciones", Icons.Default.School, Icons.Outlined.School, "nav_item_study"),
    BottomNavItem(Screen.PROFILE, "Profile", "Perfil", "Perfil", Icons.Default.Person, Icons.Outlined.Person, "nav_item_profile")
)

@Composable
fun CruxMainApp(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    chatViewModel: ChatViewModel,
    quizViewModel: QuizViewModel,
    studyViewModel: StudyViewModel,
    revisionViewModel: RevisionViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentLang by ApiKeyManager.appLanguageFlow.collectAsStateWithLifecycle()

    // Determine if bottom bar should be visible
    val showBottomBar = currentRoute in listOf(
        Screen.HOME,
        Screen.CHAT,
        "${Screen.CHAT}?prompt={prompt}",
        Screen.QUIZ_CONFIG,
        Screen.STUDY,
        Screen.REVISION,
        Screen.PROFILE
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = when (item.route) {
                            Screen.CHAT -> currentRoute?.startsWith(Screen.CHAT) == true
                            else -> currentRoute == item.route
                        }
                        val title = when (currentLang) {
                            "pt" -> item.titlePt
                            "es" -> item.titleEs
                            else -> item.titleEn
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = title
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        CruxTutorNavHost(
            navController = navController,
            homeViewModel = homeViewModel,
            chatViewModel = chatViewModel,
            quizViewModel = quizViewModel,
            studyViewModel = studyViewModel,
            revisionViewModel = revisionViewModel,
            profileViewModel = profileViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun CruxTutorNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    chatViewModel: ChatViewModel,
    quizViewModel: QuizViewModel,
    studyViewModel: StudyViewModel,
    revisionViewModel: RevisionViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HOME,
        modifier = modifier
    ) {
        composable(Screen.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToChat = { prompt ->
                    if (!prompt.isNullOrBlank()) {
                        navController.navigate("${Screen.CHAT}?prompt=${android.net.Uri.encode(prompt)}")
                    } else {
                        navController.navigate(Screen.CHAT)
                    }
                },
                onNavigateToQuizConfig = { navController.navigate(Screen.QUIZ_CONFIG) },
                onNavigateToStudy = { navController.navigate(Screen.STUDY) },
                onNavigateToRevision = { navController.navigate(Screen.REVISION) },
                onNavigateToProfile = { navController.navigate(Screen.PROFILE) }
            )
        }

        composable(
            route = "${Screen.CHAT}?prompt={prompt}",
            arguments = listOf(navArgument("prompt") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt")
            ChatScreen(
                viewModel = chatViewModel,
                initialPrompt = prompt,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QUIZ_CONFIG) {
            QuizConfigScreen(
                viewModel = quizViewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartQuiz = { navController.navigate(Screen.ACTIVE_QUIZ) }
            )
        }

        composable(Screen.ACTIVE_QUIZ) {
            ActiveQuizScreen(
                viewModel = quizViewModel,
                onNavigateHome = {
                    quizViewModel.resetQuiz()
                    navController.popBackStack(Screen.HOME, inclusive = false)
                }
            )
        }

        composable(Screen.STUDY) {
            StudyScreen(
                viewModel = studyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.REVISION) {
            RevisionScreen(
                viewModel = revisionViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { prompt ->
                    navController.navigate("${Screen.CHAT}?prompt=${android.net.Uri.encode(prompt)}")
                },
                onNavigateToQuiz = { navController.navigate(Screen.QUIZ_CONFIG) }
            )
        }

        composable(Screen.PROFILE) {
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
