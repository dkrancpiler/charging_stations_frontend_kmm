package com.example.emobilitychargingstations.android.ui.composables

import androidx.car.app.connection.CarConnection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.emobilitychargingstations.android.MainActivity.Companion.NAVIGATE_TO_FILTER_SCREEN
import com.example.emobilitychargingstations.android.MainActivity.Companion.NAVIGATE_TO_MAP_SCREEN
import com.example.emobilitychargingstations.models.ChargerTypesEnum

@Composable
fun CarConnectionComposable(navController: NavHostController, chargerType: ChargerTypesEnum?, stopRequest: () -> Unit, startRequest: () -> Unit) {
    val carConnection = CarConnection(LocalContext.current).type.observeAsState()
    if (chargerType != null) when (carConnection.value) {
        CarConnection.CONNECTION_TYPE_PROJECTION -> {
            stopRequest()
            navController.currentDestination?.route?.let {
                navController.popBackStack(
                    it, true)
            }
            navController.navigate(NAVIGATE_TO_FILTER_SCREEN)
        }
        else -> {
            if (navController.currentBackStackEntry?.destination?.route != NAVIGATE_TO_MAP_SCREEN) {
                startRequest()
                navController.navigate(NAVIGATE_TO_MAP_SCREEN)
            }
        }
    }
}