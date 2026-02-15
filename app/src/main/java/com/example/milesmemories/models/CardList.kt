package com.example.milesmemories.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class CardList(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val date: Int,
    @DrawableRes val coverImage: Int
)
