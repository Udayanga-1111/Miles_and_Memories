package com.example.milesmemories.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.milesmemories.data.CardList
import com.example.milesmemories.ui.components.FAB
import com.example.milesmemories.ui.components.Header
import com.example.milesmemories.ui.components.LandscapeCard
import com.example.milesmemories.ui.components.NavigationBar
import com.example.milesmemories.ui.components.PortraitCard
import com.example.milesmemories.ui.components.SearchBar
import com.example.milesmemories.ui.components.TitleHeader

@Composable
fun HomePage(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TitleHeader(true)
        },
        bottomBar = {
            NavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_note_page/New Note///Select Date")},
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                FAB()
            }
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Main Content
            LazyColumn(
                modifier = Modifier
                    .padding(
                        horizontal = if (isLandscape) 40.dp else 15.dp
                    )
                    .weight(1f)
            ) {
                item {
                    if (isLandscape) {
                        Header("Journeys", "${CardList.cardList.count()}")
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Header("Journeys", "${CardList.cardList.count()}")
                        Spacer(modifier = Modifier.height(10.dp))
                        SearchBar()
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                CardList.cardList.forEach { card ->
                    item {
                        if (isLandscape) {
                            LandscapeCard(
                                title = card.title,
                                description = card.description,
                                date = card.date,
                                coverImage = card.coverImage,
                                {navController.navigate("note_details_page/${card.title}/${card.description}/${card.date}/${card.coverImage}")}
                            )
                        }else{
                            PortraitCard(
                                title = card.title,
                                description = card.description,
                                date = card.date,
                                coverImage = card.coverImage,
                                {navController.navigate("note_details_page/${card.title}/${card.description}/${card.date}/${card.coverImage}")}
                            )
                        }
                    }
                }
            }
        }
    }
}
