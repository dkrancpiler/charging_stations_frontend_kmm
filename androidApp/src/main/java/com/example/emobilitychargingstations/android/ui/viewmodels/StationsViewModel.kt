package com.example.emobilitychargingstations.android.ui.viewmodels

import android.location.Location
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.CoroutineScope
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

    private val _stationsData: MutableState<List<StationsUiModel>?> = mutableStateOf(null)
    val stationsData: State<List<StationsUiModel>?> = _stationsData

    private val _userLocation : MutableState<UserLocation?> = mutableStateOf(null)
    val userLocation: State<UserLocation?> = _userLocation

    private var stationsJob: Job? = null

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let {
                if (checkIsDebugLocationMocked(it))  {
                    setUserLocation(
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

    private fun setUserLocation(newUserLocation: UserLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.setUserLocation(newUserLocation)
            _userLocation.value = newUserLocation
        }
    }
    fun startRepeatingStationsRequest() {
        if (stationsJob == null) stationsJob =
                stationsUseCase.startRepeatingRequest().onEach { stationList ->
                    if (stationList != _stationsData.value) {
                        _stationsData.value = stationList.map { it.toStationUIModel() }
//                        _stationsData.value = stationList.map { it.toStationUIModel() }
                    }
                }.launchIn(CoroutineScope(Dispatchers.IO))
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