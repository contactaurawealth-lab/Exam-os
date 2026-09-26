package com.studyoffline.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.studyoffline.app.data.preferences.UserSettings
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.ui.components.MainTab
import com.studyoffline.app.ui.components.StudyBottomBar
import com.studyoffline.app.ui.home.*
import com.studyoffline.app.ui.onboarding.OnboardingScreen
import com.studyoffline.app.ui.onboarding.OnboardingViewModel
import com.studyoffline.app.ui.planner.PlannerHomeScreen
import com.studyoffline.app.ui.planner.PlannerViewModel
import com.studyoffline.app.ui.practice.*
import com.studyoffline.app.ui.profile.*
import com.studyoffline.app.ui.subjects.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = UserSettings())

            StudyOfflineTheme(
                themeMode = settings.themeMode,
                accentColorHex = settings.accentColorHex
            ) {
                MainAppHost(
                    initialSettings = settings,
                    initialIntent = intent,
                    preferencesRepository = preferencesRepository
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun MainAppHost(
    initialSettings: UserSettings,
    initialIntent: Intent?,
    preferencesRepository: UserPreferencesRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle deep links from intents
    LaunchedEffect(initialIntent) {
        val routeExtra = initialIntent?.getStringExtra("route")
        if (!routeExtra.isNullOrEmpty() && initialSettings.onboardingComplete) {
            when (routeExtra) {
                "countdown_detail" -> navController.navigate("countdown_detail")
                "flashcard_review" -> navController.navigate("flashcard_review")
                "planner", "pomodoro" -> navController.navigate("main_tab/PLANNER")
                "quiz_setup" -> navController.navigate("quiz_setup")
            }
        }
    }

    val startDestination = if (initialSettings.onboardingComplete) "main_tab/HOME" else "onboarding"

    val isTopLevelTab = currentRoute?.startsWith("main_tab/") == true
    val currentTab = when {
        currentRoute == "main_tab/HOME" -> MainTab.HOME
        currentRoute == "main_tab/SUBJECTS" -> MainTab.SUBJECTS
        currentRoute == "main_tab/PRACTICE" -> MainTab.PRACTICE
        currentRoute == "main_tab/PLANNER" -> MainTab.PLANNER
        currentRoute == "main_tab/PROFILE" -> MainTab.PROFILE
        else -> null
    }

    Scaffold(
        bottomBar = {
            if (isTopLevelTab && currentTab != null) {
                StudyBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        navController.navigate("main_tab/${tab.name}") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(220)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(200)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(180)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(180)) }
        ) {
            // ----------------------------------------------------
            // Onboarding Graph
            // ----------------------------------------------------
            composable("onboarding") {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onComplete = {
                        navController.navigate("main_tab/HOME") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            // ----------------------------------------------------
            // Main Top-level Tabs
            // ----------------------------------------------------
            composable(
                route = "main_tab/{tabName}",
                arguments = listOf(navArgument("tabName") { type = NavType.StringType })
            ) { backStackEntry ->
                val tabName = backStackEntry.arguments?.getString("tabName") ?: "HOME"
                when (tabName) {
                    "HOME" -> {
                        val homeViewModel: HomeViewModel = hiltViewModel()
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToTopic = { topicId ->
                                navController.navigate("topic_detail/$topicId")
                            },
                            onNavigateToCountdownDetail = {
                                navController.navigate("countdown_detail")
                            },
                            onNavigateToFlashcardsDue = {
                                navController.navigate("flashcard_review")
                            },
                            onNavigateToAddSubject = {
                                navController.navigate("main_tab/SUBJECTS")
                            }
                        )
                    }
                    "SUBJECTS" -> {
                        val subjectsViewModel: SubjectsViewModel = hiltViewModel()
                        SubjectListScreen(
                            viewModel = subjectsViewModel,
                            onNavigateToSubjectDetail = { subjectId ->
                                navController.navigate("subject_detail/$subjectId")
                            }
                        )
                    }
                    "PRACTICE" -> {
                        val practiceViewModel: PracticeViewModel = hiltViewModel()
                        PracticeHomeScreen(
                            viewModel = practiceViewModel,
                            onNavigateToQuizSetup = {
                                navController.navigate("quiz_setup")
                            },
                            onNavigateToFlashcardReview = {
                                navController.navigate("flashcard_review")
                            },
                            onNavigateToWeakQuestions = {
                                navController.navigate("quiz_setup?isWeak=true")
                            }
                        )
                    }
                    "PLANNER" -> {
                        val plannerViewModel: PlannerViewModel = hiltViewModel()
                        PlannerHomeScreen(
                            viewModel = plannerViewModel,
                            onNavigateToTopic = { topicId ->
                                navController.navigate("topic_detail/$topicId")
                            }
                        )
                    }
                    "PROFILE" -> {
                        val profileViewModel: ProfileViewModel = hiltViewModel()
                        ProgressDashboardScreen(
                            viewModel = profileViewModel,
                            onBack = {
                                navController.navigate("settings")
                            }
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // Subject & Topic Details
            // ----------------------------------------------------
            composable(
                route = "subject_detail/{subjectId}",
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { entry ->
                val subjectId = entry.arguments?.getLong("subjectId") ?: 0L
                val subjectsViewModel: SubjectsViewModel = hiltViewModel()
                SubjectDetailScreen(
                    subjectId = subjectId,
                    viewModel = subjectsViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToTopicDetail = { topicId ->
                        navController.navigate("topic_detail/$topicId")
                    }
                )
            }

            composable(
                route = "topic_detail/{topicId}",
                arguments = listOf(navArgument("topicId") { type = NavType.LongType })
            ) { entry ->
                val topicId = entry.arguments?.getLong("topicId") ?: 0L
                val subjectsViewModel: SubjectsViewModel = hiltViewModel()
                TopicDetailScreen(
                    topicId = topicId,
                    viewModel = subjectsViewModel,
                    onBack = { navController.popBackStack() },
                    onStartFlashcardReview = { id ->
                        navController.navigate("flashcard_review?topicId=$id")
                    },
                    onStartTopicQuiz = { id ->
                        navController.navigate("quiz_setup?subjectId=-1&topicId=$id")
                    }
                )
            }

            // ----------------------------------------------------
            // Exam Countdown Detail
            // ----------------------------------------------------
            composable("countdown_detail") {
                ExamCountdownDetailScreen(
                    preferencesRepository = preferencesRepository,
                    onBack = { navController.popBackStack() }
                )
            }

            // ----------------------------------------------------
            // Practice: Quiz Flows
            // ----------------------------------------------------
            composable(
                route = "quiz_setup?subjectId={subjectId}&isWeak={isWeak}",
                arguments = listOf(
                    navArgument("subjectId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("isWeak") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { entry ->
                val subjectId = entry.arguments?.getLong("subjectId").takeIf { it != -1L }
                val isWeak = entry.arguments?.getBoolean("isWeak") ?: false
                val practiceViewModel: PracticeViewModel = hiltViewModel()

                QuizSetupScreen(
                    viewModel = practiceViewModel,
                    initialSubjectId = subjectId,
                    isWeakOnly = isWeak,
                    onBack = { navController.popBackStack() },
                    onStartSession = {
                        navController.navigate("quiz_session")
                    }
                )
            }

            composable("quiz_session") {
                val practiceViewModel: PracticeViewModel = hiltViewModel()
                QuizSessionScreen(
                    viewModel = practiceViewModel,
                    onBack = { navController.popBackStack() },
                    onQuizComplete = {
                        navController.navigate("quiz_result") {
                            popUpTo("quiz_session") { inclusive = true }
                        }
                    }
                )
            }

            composable("quiz_result") {
                val practiceViewModel: PracticeViewModel = hiltViewModel()
                QuizResultScreen(
                    viewModel = practiceViewModel,
                    onDone = {
                        navController.popBackStack("main_tab/PRACTICE", inclusive = false)
                    }
                )
            }

            // ----------------------------------------------------
            // Practice: Flashcard Review (SM-2)
            // ----------------------------------------------------
            composable(
                route = "flashcard_review?topicId={topicId}",
                arguments = listOf(
                    navArgument("topicId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val topicId = entry.arguments?.getLong("topicId").takeIf { it != -1L }
                val practiceViewModel: PracticeViewModel = hiltViewModel()

                LaunchedEffect(topicId) {
                    practiceViewModel.startFlashcardReview(topicId)
                }

                FlashcardReviewScreen(
                    viewModel = practiceViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // ----------------------------------------------------
            // Settings
            // ----------------------------------------------------
            composable("settings") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onResetComplete = {
                        navController.navigate("onboarding") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
