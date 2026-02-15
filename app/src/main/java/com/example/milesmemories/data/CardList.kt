package com.example.milesmemories.data

import com.example.milesmemories.R
import com.example.milesmemories.models.CardList

object CardList {
    val cardList = listOf<CardList>(
        CardList(R.string.swissalps_title, R.string.swissalps_description, R.string.swissalps_date, R.drawable.swissalps_img_1),
        CardList(R.string.tokyo_title, R.string.tokyo_description, R.string.tokyo_date, R.drawable.tokyo_img_1),
        CardList(R.string.amazon_title, R.string.amazon_description, R.string.amazon_date, R.drawable.amazon_img_1),
        CardList(R.string.maldives_title, R.string.maldives_description, R.string.maldives_date, R.drawable.maldives_img_1)
    )

    val favList = listOf<CardList>(
        CardList(R.string.swissalps_title, R.string.swissalps_description, R.string.swissalps_date, R.drawable.swissalps_img_1)
    )
}