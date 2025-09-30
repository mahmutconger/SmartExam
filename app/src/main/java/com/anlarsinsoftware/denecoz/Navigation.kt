package com.anlarsinsoftware.denecoz

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.denecoz.View.EnteranceScreens.LoginScreen
import com.anlarsinsoftware.denecoz.View.EnteranceScreens.RegisterScreen
import com.anlarsinsoftware.denecoz.View.HomeScreen.HomeScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.AnswerKeyEditorScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.AnswerKeyScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.GeneralInfoScreen
import com.anlarsinsoftware.denecoz.View.PublisherView.PubHomeScreen

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object RegisterScreen : Screen("register_screen")
    object HomeScreen : Screen("home_screen")
    object PubHomeScreen : Screen("pub_home_screen")
    object GeneralInfoScreen : Screen("general_info_screen")
    object AnswerKeyScreen : Screen("answer_key_screen")
    object AnswerKeyEditorScreen : Screen("answer_key_editor_screen")

}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route
    ) {
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(navController = navController)
        }
        //Home Screen
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.HomeScreen.route) {
            PubHomeScreen(navController=navController)
        }
        composable(route = Screen.GeneralInfoScreen.route) {
            GeneralInfoScreen(navController=navController)
        }
        composable(route = Screen.AnswerKeyScreen.route) {
            AnswerKeyScreen(navController=navController)
        }
        composable(route = Screen.AnswerKeyEditorScreen.route) {
            AnswerKeyEditorScreen (navController=navController)
        }
    }
}