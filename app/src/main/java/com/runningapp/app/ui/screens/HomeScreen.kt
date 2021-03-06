package com.runningapp.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.runningapp.app.R
import com.runningapp.app.service.TrackingService
import com.runningapp.app.ui.components.MonthlyGoalComponent
import com.runningapp.app.ui.map.rememberMapViewWithLifecycle
import com.runningapp.app.ui.navigation.NavigationItem
import com.runningapp.app.ui.utils.Constants
import com.runningapp.app.ui.viewmodel.ChallengesViewModel
import com.runningapp.app.ui.viewmodel.HomeMapViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: HomeMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var map: GoogleMap? = null

    val mapPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PermissionsRequired(
            multiplePermissionsState = mapPermissionsState,
            permissionsNotGrantedContent = {
                MapPermissionsNotGrantedUI(
                    onRequestPermission = {
                        mapPermissionsState.launchMultiplePermissionRequest()
                    },
                    onDenyRequestPermission = {
                        navHostController.popBackStack()
                    }
                )
            },
            permissionsNotAvailableContent = {
                MapPermissionsNotAvailableUI(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        context.startActivity(intent)
                    }
                )
            },
        ) {
            MonthlyGoalComponent()

            Box(
                modifier = Modifier
                    .height(400.dp)
                    .fillMaxWidth()
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .height(350.dp)
                ) {
                    val lastKnownLocation by viewModel.lastKnownLocation.observeAsState()
                    val dark = isSystemInDarkTheme()
                    val mapViewWithLifecycle = rememberMapViewWithLifecycle()
                    AndroidView(factory = { mapViewWithLifecycle }) { mapView ->
                        mapView.getMapAsync {
                            map = it
                            map!!.isMyLocationEnabled = true
                            if (dark) {
                                map!!.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                        context, R.raw.style_json
                                    )
                                )
                            }

                            viewModel.requestForLastKnownLocation()

                            if (lastKnownLocation != null) {
                                map?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        lastKnownLocation!!,
                                        Constants.MAP_ZOOM
                                    )
                                )
                            }

                        }
                    }
                }

                Button(
                    onClick = { navHostController.navigate("runMode") },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(100.dp),
                    shape = CircleShape
                ) {
                    Text(
                        text = "GO!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}


@Composable
private fun MapPermissionsNotGrantedUI(
    onRequestPermission: () -> Unit,
    onDenyRequestPermission: () -> Unit,
) {

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "This application needs your location",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            FilledTonalButton(
                onClick = onRequestPermission,
            ) {
                Text("Ok")
            }

            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = onDenyRequestPermission,
            ) {
                Text("No")
            }
        }
    }
}

@Composable
private fun MapPermissionsNotAvailableUI(
    onOpenSettings: () -> Unit
) {

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Permission not available",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Open settings",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
