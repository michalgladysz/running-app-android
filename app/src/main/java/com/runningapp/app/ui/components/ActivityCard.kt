package com.runningapp.app.ui.components

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tour
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.runningapp.app.R
import com.runningapp.app.domain.model.RunActivity
import com.runningapp.app.ui.theme.custom_color_red
import com.runningapp.app.ui.viewmodel.RunActivityViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityCard(
    runActivity: RunActivity,
    viewModel: RunActivityViewModel = hiltViewModel()
) {
    val userId = viewModel.userId.observeAsState()

    val mapExpanded = remember { mutableStateOf(false) }

    val mapSize by animateDpAsState(
        if (mapExpanded.value) 300.dp else 100.dp
    )


    val liked = remember { mutableStateOf(false) }
    val frontLikes = remember { mutableStateOf(runActivity.likesAmount) }

    val likesState = viewModel.likesState.value

    LaunchedEffect(key1 = true) {
        userId.value?.let { viewModel.getUserLikedPosts(it) }
    }

    if (likesState.likedActivities.any { it.activityId == runActivity.id }) {
        liked.value = true
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.profile_picture),
                        contentDescription = "Contact profile picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Row {
                            val parsedDate = LocalDateTime.parse(runActivity.date, DateTimeFormatter.ISO_DATE_TIME)
                            val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm"))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Row {
                            Text(
                                text = runActivity.user.username,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = " shared activity"
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        liked.value = !liked.value
                        if (liked.value) frontLikes.value++
                        else frontLikes.value--
                        userId.value?.let { viewModel.updateLike(it, runActivity.id) }
                    }
                ) {
                    Icon(
                        imageVector = if (liked.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (liked.value) {
                            "Like"
                        } else {
                            "Dislike"
                        },
                        tint = custom_color_red
                    )
                }

                Text(text = frontLikes.value.toString())
            }
            Row(
                modifier = Modifier
                    .padding(bottom = 0.dp, end = 24.dp, start = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tour,
                        contentDescription = "Distance"
                    )
                    val distanceInKm = ((runActivity.distance / 1000f).toDouble())
                    val distanceInKmRounded =
                        BigDecimal(distanceInKm).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                    Text(
                        text = "$distanceInKmRounded km",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Total time"
                    )
                    Text(
                        text = runActivity.totalTime,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsRun,
                        contentDescription = "Avg Pace"
                    )
                    Text(
                        text = runActivity.pace,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

            }
            val imageBytes = Base64.decode(runActivity.mapImage.data, 0)
            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            Row(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                    .fillMaxWidth(),
            ) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "Activity map preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RoundedCornerShape(0.dp, 0.dp, 20.dp, 20.dp))
                        .weight(1f)
                        .size(mapSize)
                        .clickable(
                            enabled = true,
                            onClickLabel = "Expand map",
                            onClick = {
                                mapExpanded.value = !mapExpanded.value
                            }
                        )
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ActivityCardPreview() {
    // ActivityCard()
}