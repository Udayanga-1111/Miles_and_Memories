package com.example.milesmemories.data


import com.example.milesmemories.R
import com.example.milesmemories.models.NavItems
import com.example.milesmemories.models.Screen

object NavigationList {
    val items = listOf(
        NavItems(Screen.HomePage.route, R.drawable.nav_home_icon, "Home"),
        NavItems(Screen.FavoritePage.route, R.drawable.nav_fav_icon, "Favorites"),
        NavItems(Screen.AlbumPage.route, R.drawable.nav_album_icon, "Album"),
        NavItems(Screen.ProfilePage.route, R.drawable.nav_profile_icon, "Profile")
    )
}