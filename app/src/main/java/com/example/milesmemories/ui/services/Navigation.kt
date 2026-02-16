package com.example.milesmemories.ui.services

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
                navArgument("page"){ type = NavType.StringType},
                navArgument("title") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType }
            )

        ) {backStackEntry ->
            val args = backStackEntry.arguments
            val page = args?.getString("page") ?: ""
            val title = args?.getString("title") ?: ""
            val description = args?.getString("description") ?: ""
            val date = args?.getString("date") ?: ""


            AddNotePage(
                navController,
                page,
                title,
                description,
                date
            )
        }
        
        composable(
            route = Screen.NoteDetailsPage.route,
            arguments = listOf(
                navArgument("title") { type = NavType.IntType },
                navArgument("description") { type = NavType.IntType },
                navArgument("date") { type = NavType.IntType },
                navArgument("coverImage") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val titleRes = args?.getInt("title") ?: 0
            val descRes = args?.getInt("description") ?: 0
            val dateRes = args?.getInt("date") ?: 0
            val coverImageArg = args?.getInt("coverImage") ?: 0
            val coverImageRes = if (coverImageArg != 0) coverImageArg else null
            
            val title = if (titleRes != 0) stringResource(titleRes) else ""
            val description = if (descRes != 0) stringResource(descRes) else ""
            val date = if (dateRes != 0) stringResource(dateRes) else ""

            NoteDetailsPage(
                title = title,
                description = description,
                date = date,
                coverImageRes = coverImageRes,
                onNavigateBack = {
                    navController.popBackStack()
                },
                navController
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
