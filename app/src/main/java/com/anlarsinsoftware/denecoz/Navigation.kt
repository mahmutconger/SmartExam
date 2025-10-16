package com.anlarsinsoftware.denecoz

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditorMode
import com.anlarsinsoftware.denecoz.View.Common.WelcomeScreen
import com.anlarsinsoftware.denecoz.View.StudentView.HomeScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.AnswerKeyEditorScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.AnswerKeyScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.GeneralInfoScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.PreviewScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.PubHomeScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.PublisherLoginScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.PublisherRegisterScreen
import com.anlarsinsoftware.denecoz.View.StudentView.AnalysisDetailsScreen
import com.anlarsinsoftware.denecoz.View.StudentView.EditProfileScreen
import com.anlarsinsoftware.denecoz.View.StudentView.ExamEntryScreen
import com.anlarsinsoftware.denecoz.View.StudentView.ProfileScreen
import com.anlarsinsoftware.denecoz.View.StudentView.ResultsScreen
import com.anlarsinsoftware.denecoz.View.StudentView.StudentLoginScreen
import com.anlarsinsoftware.denecoz.View.StudentView.StudentRegisterScreen

sealed class Screen(val route: String) {
    // --- GİRİŞ EKRANLARI ---
    object WelcomeScreen : Screen("welcome_screen")
    object StudentLoginScreen : Screen("student_login_screen")
    object StudentRegisterScreen : Screen("student_register_screen")
    object PublisherLoginScreen : Screen("publisher_login_screen")
    object PublisherRegisterScreen : Screen("publisher_register_screen")
    // Student
    object LoginScreen : Screen("login_screen")
    object RegisterScreen : Screen("register_screen")
    object HomeScreen : Screen("home_screen")
    object ProfileScreen : Screen("profile_screen")
    object EditProfileScreen : Screen("edit_profile_screen")
    // Publisher
    object PubHomeScreen : Screen("pub_home_screen")
    object GeneralInfoScreen : Screen("general_info_screen")

    object ExamEntryScreen : Screen("exam_entry_screen/{examId}") {
        fun createRoute(examId: String) = "exam_entry_screen/$examId"
    }

    object ResultsScreen : Screen("results_screen/{examId}/{attemptId}") {
        fun createRoute(examId: String, attemptId: String) = "results_screen/$examId/$attemptId"
    }

    object AnswerKeyScreen : Screen("answer_key_screen/{examId}") {
        fun createRoute(examId: String) = "answer_key_screen/$examId"
    }
    object AnswerKeyEditorScreen : Screen("answer_key_editor_screen/{examId}/{mode}/{bookletName}") {
        fun createRoute(examId: String, mode: EditorMode, bookletName: String) =
            "answer_key_editor_screen/$examId/${mode.name}/$bookletName"
    }
    object PreviewScreen : Screen("preview_screen/{examId}") {
        fun createRoute(examId: String) = "preview_screen/$examId"
    }
    object AnalysisDetailsScreen : Screen("analysis_details/{examId}/{attemptId}/{topicName}") {
        fun createRoute(examId: String, attemptId: String, topicName: String) =
            "analysis_details/$examId/$attemptId/$topicName"
    }
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.WelcomeScreen.route
    ) {
        // --- GİRİŞ AKIŞI ---
        composable(route = Screen.WelcomeScreen.route) {
            WelcomeScreen(navController = navController)
        }
        composable(route = Screen.StudentLoginScreen.route) {
            StudentLoginScreen(navController = navController)
        }
        composable(route = Screen.StudentRegisterScreen.route) {
            StudentRegisterScreen(navController = navController)
        }
        composable(route = Screen.PublisherLoginScreen.route) {
             PublisherLoginScreen(navController = navController)
        }
        composable(route = Screen.PublisherRegisterScreen.route) {
             PublisherRegisterScreen(navController = navController)
        }

        // ExamEntry
        composable(
            route = Screen.ExamEntryScreen.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) {
            ExamEntryScreen(navController = navController)
        }

        composable(
            route = Screen.ResultsScreen.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("attemptId") { type = NavType.StringType }
            )
        ) {
            ResultsScreen(navController = navController)
        }

        // Student_Home
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.ProfileScreen.route) {
            ProfileScreen(navController = navController)
        }
        composable(route = Screen.EditProfileScreen.route) {
            EditProfileScreen(navController = navController)
        }
        composable(
            route = Screen.AnalysisDetailsScreen.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("attemptId") { type = NavType.StringType },
                navArgument("topicName") { type = NavType.StringType }
            )
        ) {
            AnalysisDetailsScreen(navController = navController)
        }

        // Publisher

        // Home
        composable(route = Screen.PubHomeScreen.route) {
            PubHomeScreen(navController = navController)
        }

        // General
        composable(route = Screen.GeneralInfoScreen.route) {
            GeneralInfoScreen(navController = navController)
        }

        // AnswerKey
        composable(
            route = Screen.AnswerKeyScreen.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) {
            AnswerKeyScreen(navController = navController)
        }

        // ManuelAnswerKey
        composable(
            route = Screen.AnswerKeyEditorScreen.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType },
                navArgument("bookletName") { type = NavType.StringType }
            )
        ) {
            AnswerKeyEditorScreen(navController = navController)
        }

        // Preview
        composable(
            route = Screen.PreviewScreen.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) {
            PreviewScreen(navController = navController)
        }
    }
}