package com.example.milesmemories.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.lint.Names.Runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.milesmemories.data.CountryImages
import com.example.milesmemories.ui.components.AlbumCard
import com.example.milesmemories.ui.components.Header
import com.example.milesmemories.ui.components.NavigationBar
import com.example.milesmemories.ui.components.TitleHeader

@Composable
fun AlbumPage(navController: NavController){


    val verticalScroll: ScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TitleHeader()
        },
        bottomBar = {
            NavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .verticalScroll(verticalScroll)
                .padding(innerPadding)
        ) {

            Box(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp)
            ){
                Header("Album")
            }

            // Main Content
            FlowRow(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 15.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CountryImages.countryImages.keys.forEach { destination ->
                    AlbumCard(navController, destination)
                }
            }
        }
    }
}
