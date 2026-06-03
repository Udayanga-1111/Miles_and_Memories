package com.example.milesmemories.models


sealed class Screen(val route: String) {
    object HomePage : Screen ("home_screen")
    object FavoritePage : Screen ("fav_screen")
    object AlbumPage: Screen("album_page")
    object ProfilePage: Screen("profile_page")
    object LoginPage : Screen("login_page")
    object SignupPage : Screen("signup_page")
    object AddNotePage : Screen("add_note_page/{page}?noteId={noteId}")
    object NoteDetailsPage : Screen("note_details_page/{noteId}")
    object PicturePage : Screen("picture_page/{title}")
    object WeatherPage : Screen("weather_page")
    object SecurityPage : Screen("security_page")
}
