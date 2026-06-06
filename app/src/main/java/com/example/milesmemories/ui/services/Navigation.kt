package com.example.milesmemories.ui.services

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.milesmemories.models.Screen
import com.example.milesmemories.ui.screens.AddNotePage
import com.example.milesmemories.ui.screens.AlbumPage
import com.example.milesmemories.ui.screens.FavoritePage
import com.example.milesmemories.ui.screens.HomePage
import com.example.milesmemories.ui.screens.LoginPage
import com.example.milesmemories.ui.screens.ProfilePage
import com.example.milesmemories.ui.screens.NoteDetailsPage
import com.example.milesmemories.ui.screens.PicturePage
import com.example.milesmemories.ui.screens.SignupPage
import com.example.milesmemories.ui.screens.WeatherPage
import com.example.milesmemories.ui.screens.SecurityPage
import com.example.milesmemories.ui.screens.AccountPage
import com.example.milesmemories.ui.screens.NotificationsPage
import com.example.milesmemories.ui.screens.AboutPage

@Composable
fun Navigation(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.LoginPage.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.HomePage.route) {
            HomePage(navController)
        }
        composable(Screen.FavoritePage.route) {
            FavoritePage(navController)
        }
        composable(Screen.AlbumPage.route) {
            AlbumPage(navController)
        }
        composable(Screen.WeatherPage.route) {
            WeatherPage(navController)
        }
        composable(Screen.SecurityPage.route) {
            SecurityPage(navController)
        }
        composable(Screen.AccountPage.route) {
            AccountPage(navController)
        }
        composable(Screen.NotificationsPage.route) {
            NotificationsPage(navController)
        }
        composable(Screen.AboutPage.route) {
            AboutPage(navController)
        }
        composable(Screen.ProfilePage.route) {
            ProfilePage(navController, isDarkTheme, onThemeChange)
        }
        composable(Screen.LoginPage.route) {
            LoginPage(navController)
        }
        composable(Screen.SignupPage.route) {
            SignupPage(navController)
        }

        composable(
            route = Screen.AddNotePage.route,
            arguments = listOf(
                navArgument("page"){ type = NavType.StringType },
                navArgument("noteId") { type = NavType.StringType; nullable = true; defaultValue = "" }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val page = args?.getString("page") ?: ""
            val noteId = args?.getString("noteId") ?: ""

            AddNotePage(
                navController,
                page,
                noteId
            )
        }
        
        composable(
            route = Screen.NoteDetailsPage.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""

            NoteDetailsPage(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                navController = navController
            )
        }

        composable(
            Screen.PicturePage.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType }
            )
        ) {
            backStackEntry ->
            val args = backStackEntry.arguments
            val title = args?.getString("title") ?: ""

            PicturePage(
                navController,
                title
            )
        }
    }
}
