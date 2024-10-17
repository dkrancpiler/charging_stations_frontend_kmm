package com.example.emobilitychargingstations.android.ui.composables

import androidx.car.app.connection.CarConnection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.emobilitychargingstations.android.MainActivity.Companion.NAVIGATE_TO_FILTER_SCREEN
import com.example.emobilitychargingstations.android.MainActivity.Companion.NAVIGATE_TO_MAP_SCREEN

@Composable
fun CarConnectionComposable(navController: NavHostController, stopStationRequest: () -> Unit, startStationRequest: () -> Unit, stopLocationRequest: () -> Unit, startLocationRequest: () -> Unit) {
    val carConnection = CarConnection(LocalContext.current).type.observeAsState()
    when (carConnection.value) {
        CarConnection.CONNECTION_TYPE_PROJECTION -> {
            stopStationRequest()
            stopLocationRequest()
            navController.currentDestination?.route?.let {
                navController.popBackStack(
                    it, true)
            }
            navController.navigate(NAVIGATE_TO_FILTER_SCREEN)
        }
        else -> {
            if (navController.currentBackStackEntry?.destination?.route != NAVIGATE_TO_MAP_SCREEN) {
                startStationRequest()
                startLocationRequest()
                navController.navigate(NAVIGATE_TO_MAP_SCREEN)
            }
        }
    }
}