package com.example.emobilitychargingstations.android.ui.viewmodels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class StationsViewModel(
    private val userUseCase: UserUseCase,
    private val stationsUseCase: StationsUseCase
) : ViewModel() {

    private val _stationsData: MutableLiveData<List<StationsUiModel>> = MutableLiveData()
    val stationsData: LiveData<List<StationsUiModel>> = _stationsData

    private val _userLocation : MutableLiveData<UserLocation> = MutableLiveData()
    val userLocation: LiveData<UserLocation> = _userLocation

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
                    startRepeatingStationsRequest()
                }
            }
        }
    }

    private fun checkIsDebugLocationMocked(location: Location) : Boolean {
        return if (BuildConfig.DEBUG) location.isMock else true
    }

    private fun setUserLocation(newUserLocation: UserLocation) {
        stationsUseCase.setTemporaryLocation(newUserLocation)
        _userLocation.value = newUserLocation
    }
    fun startRepeatingStationsRequest() {
        if (stationsJob == null) stationsJob =
            stationsUseCase.startRepeatingRequest(userLocation.value).onEach { stationList ->
                if (stationList != null && stationList != _stationsData.value) {
                    _stationsData.postValue(stationList.map { it.toStationUIModel() })
                }
            }.launchIn(viewModelScope)
    }

    fun stopRepeatingStationsRequest() {
        stationsJob?.cancel()
        stationsJob = null
    }

    fun getUserInfo(): UserInfo? = userUseCase.getUserInfo()

}