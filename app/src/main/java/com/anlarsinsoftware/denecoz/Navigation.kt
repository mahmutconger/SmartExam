package com.anlarsinsoftware.denecoz

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anlarsinsoftware.denecoz.Model.State.Publisher.EditorMode
import com.anlarsinsoftware.denecoz.View.EnteranceScreens.LoginScreen
import com.anlarsinsoftware.denecoz.View.EnteranceScreens.RegisterScreen
import com.anlarsinsoftware.denecoz.View.StudentView.HomeScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.AnswerKeyEditorScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.AnswerKeyScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.GeneralInfoScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.PreviewScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.PubHomeScreen
import com.anlarsinsoftware.denecoz.View.StudentView.ExamEntryScreen

sealed class Screen(val route: String) {
    // Student
    object LoginScreen : Screen("login_screen")
    object RegisterScreen : Screen("register_screen")
    object HomeScreen : Screen("home_screen")
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
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route
    ) {
        // Enterance
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(navController = navController)
        }

        // ExamEntry
        composable(
            route = Screen.ExamEntryScreen.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) {
            // Bu rotaya gidildiğinde ExamEntryScreen'i göster
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