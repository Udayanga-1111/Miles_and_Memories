package com.example.milesmemories.models


sealed class Screen(val route: String) {
    object HomePage : Screen ("home_screen")
    object FavoritePage : Screen ("fav_screen")
    object AlbumPage: Screen("album_page")
    object ProfilePage: Screen("profile_page")
    object LoginPage : Screen("login_page")
    object SignupPage : Screen("signup_page")
    object AddNotePage : Screen("add_note_page/{page}?title={title}&description={description}&date={date}")
    object NoteDetailsPage : Screen("note_details_page/{noteId}")
    object PicturePage : Screen("picture_page/{title}")

}
