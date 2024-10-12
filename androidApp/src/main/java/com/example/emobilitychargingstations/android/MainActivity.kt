package com.example.emobilitychargingstations.android

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.comsystoreply.emobilitychargingstations.android.BuildConfig
import com.comsystoreply.emobilitychargingstations.android.MyApplicationTheme
import com.example.emobilitychargingstations.android.ui.composables.NavigationHostComposable
import com.example.emobilitychargingstations.android.ui.composables.CarConnectionComposable
import com.example.emobilitychargingstations.android.ui.utilities.LocationRequestStarter
import com.example.emobilitychargingstations.models.UserLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    private val stationsViewModel: StationsViewModel by viewModel()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let {
                if (checkIsDebugLocationMocked(it))  {
                    stationsViewModel.setUserLocation(
                        UserLocation(
                            it.latitude,
                            it.longitude
                        )
                    )
                    stationsViewModel.startRepeatingStationsRequest()
                }
            }
        }
    }

    private fun checkIsDebugLocationMocked(location: Location) : Boolean {
        return if (BuildConfig.DEBUG) location.isMock else true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userInfo = stationsViewModel.getUserInfo()
        val startDestination =
            if (userInfo?.filterProperties?.chargerType != null) NAVIGATE_TO_MAP_SCREEN else NAVIGATE_TO_CHARGER_SELECTION
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    startRequestingLocation()
                    setContent {
                        MyApplicationTheme {
                            val navController = rememberNavController()
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colors.background
                            ) {
                                Column {
                                    NavigationHostComposable(navController, startDestination)
                                    CarConnectionComposable(
                                        navController,
                                        userInfo?.filterProperties?.chargerType,
                                        stationsViewModel::stopRepeatingStationsRequest,
                                        stationsViewModel::startRepeatingStationsRequest
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun startRequestingLocation() {
        LocationRequestStarter(this, locationCallback)
    }

    companion object {
        const val NAVIGATE_TO_CHARGER_SELECTION = "chargerSelectionScreen"
        const val NAVIGATE_TO_MAP_SCREEN = "mapScreen"
        const val NAVIGATE_TO_FILTER_SCREEN = "filterScreen"

        const val ARGUMENT_NAVIGATE_TO_NEXT = "navigateToNext"
    }
}