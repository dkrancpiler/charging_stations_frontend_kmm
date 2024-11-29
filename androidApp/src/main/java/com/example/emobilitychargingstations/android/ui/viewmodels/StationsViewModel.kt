package com.example.emobilitychargingstations.android.ui.viewmodels

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comsystoreply.emobilitychargingstations.android.BuildConfig
import com.example.emobilitychargingstations.android.mappers.toStationUIModel
import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import com.example.emobilitychargingstations.domain.stations.StationsUseCase
import com.example.emobilitychargingstations.domain.user.UserUseCase
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class StationsViewModel(
    private val userUseCase: UserUseCase,
    private val stationsUseCase: StationsUseCase
) : ViewModel() {

    init {
        startRepeatingStationsRequest()
    }

    var stationsData by mutableStateOf<List<StationsUiModel>?>(null)
        private set

    var userLocation by mutableStateOf<UserLocation?>(null)
        private set

    private var stationsJob: Job? = null

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let {
                if (checkIsDebugLocationMocked(it))  {
                    updateUserLocation(
                        UserLocation(
                            it.latitude,
                            it.longitude
                        )
                    )
                }
            }
        }
    }

    private fun checkIsDebugLocationMocked(location: Location) : Boolean {
        return if (BuildConfig.DEBUG) location.isMock else true
    }

    private fun updateUserLocation(newUserLocation: UserLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.setUserLocation(newUserLocation)
            userLocation = newUserLocation
        }
    }
    fun startRepeatingStationsRequest() {
        if (stationsJob == null) stationsJob =
                stationsUseCase.startRepeatingRequest().onEach { stationList ->
                    if (stationList != stationsData) {
                        stationsData = stationList.map { it.toStationUIModel() }
                    }
                }.launchIn(viewModelScope)
    }

    fun stopRepeatingStationsRequest() {
        stationsJob?.cancel()
        stationsJob = null
    }

    fun getUserInfo(): UserInfo? = userUseCase.getUserInfo()

    override fun onCleared() {
        stationsJob?.cancel()
    }
}