package com.example.milesmemories.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.milesmemories.R
import com.example.milesmemories.data.CountryImages

@Composable
fun AlbumCard(
    navController: NavController,
    destination:String){

    // getting 3 images to make the Album cover from the data package
    // might no be the correct but this is what came to my mind for the convenient of me
    val imageList = CountryImages.countryImages[destination]
    val image1 = painterResource(imageList?.getOrNull(0) ?: R.drawable.cardexample)
    val image2 = painterResource(imageList?.getOrNull(1) ?: R.drawable.cardexample)
    val image3 = painterResource(imageList?.getOrNull(2) ?: R.drawable.cardexample)

    // Album Card
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 5.dp)
            .clickable(onClick = { navController.navigate(route = "picture_page/$destination")})
    ) {
        ElevatedCard(
            modifier = Modifier
                .height(180.dp)
                .width(180.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ){
            Column(
                modifier = Modifier
                    .border(
                        BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(20.dp)
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ){
                // Card Cover
                Image(
                    painter = image1,
                    contentDescription = "pic 1",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .weight(1f)
                        .width(180.dp) // Ensure full width fill
                )
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Image(
                    painter = image2,
                    contentDescription = "pic2",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomStart = 20.dp))
                        .weight(1f)
                        .border(
                            BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(bottomStart = 20.dp)
                        ),
                )
                    Image(
                    painter = image3,
                    contentDescription = "pic3",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomEnd = 20.dp))
                        .weight(1f)
                        .border(
                            BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(bottomEnd = 20.dp)
                        ),
                )
                }
            }
        }
        // Album Name
        Text(
            text = destination,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}