package com.example.emobilitychargingstations.domain.stations

import com.example.emobilitychargingstations.SHOULD_TRY_API_REQUEST
import com.example.emobilitychargingstations.STATION_REQUEST_REPEAT_TIME_MS
import com.example.emobilitychargingstations.data.extensions.getStationsClosestToUserLocation
import com.example.emobilitychargingstations.PlatformSpecificFunctions
import com.example.emobilitychargingstations.data.extensions.filterByChargerType
import com.example.emobilitychargingstations.data.extensions.filterByChargingType
import com.example.emobilitychargingstations.data.extensions.randomizeAvailability
import com.example.emobilitychargingstations.data.stations.StationsRepository
import com.example.emobilitychargingstations.data.stations.toStationList
import com.example.emobilitychargingstations.domain.user.UserUseCase
import com.example.emobilitychargingstations.models.Station
import com.example.emobilitychargingstations.models.Stations
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StationsUseCase(private val stationsRepository: StationsRepository, private val userUseCase: UserUseCase) {

    private var userLocation: UserLocation? = null

    private suspend fun insertStations(stations: Stations) {
        stationsRepository.insertStations(
            stations
        )
    }

    private suspend fun getStationsLocal(): Stations? {
        var localStations = stationsRepository.getStationsLocal()
        if (localStations == null) {
            localStations = PlatformSpecificFunctions().getStationsFromJson()
            localStations?.let {
                insertStations(it)
            }
        }
        return localStations
    }

    fun setTemporaryLocation(newLocation: UserLocation?) {
        userLocation = newLocation
    }

    fun startRepeatingRequest(initialLocation: UserLocation?) = channelFlow {
        launch(Dispatchers.IO) {
            val localStations = getStationsLocal()
            var userInfo = userUseCase.getUserInfo()
            val localStationsWithUserFilters = localStations?.copy()?.features?.applyUserFiltersToStations(userInfo)
            userLocation = initialLocation
            if (userLocation != null) send(localStationsWithUserFilters?.getStationsClosestToUserLocation(userLocation))
            else send(localStationsWithUserFilters)
            launch {
                userUseCase.getUserInfoAsFlow().onEach { userInfoChange ->
                    if ((userInfo?.filterProperties?.chargingType != userInfoChange?.filterProperties?.chargingType
                                || userInfo?.filterProperties?.chargerType != userInfoChange?.filterProperties?.chargerType)) {
                        userInfo = userInfoChange
                        localStations?.features?.let { localStations ->
                            val resultingList = localStations.applyUserFiltersToStations(userInfo)
                            send(resultingList)
                        }
                    }
                }.collect()
            }
            while (true) {
                val remoteStations = mutableListOf<Station>()
                if (PlatformSpecificFunctions().isDebug && SHOULD_TRY_API_REQUEST) {
                    stationsRepository.getStationsRemote(userLocation).onRight {
                        remoteStations.addAll(it.toStationList())
                    }.onLeft {
                        print(it.toString())
                    }
                }
                val resultingList = combineRemoteAndLocalStations(localStations?.features, remoteStations)
                    .getStationsClosestToUserLocation(userLocation)
                    .applyUserFiltersToStations(userInfo)
                send(resultingList)
                delay(STATION_REQUEST_REPEAT_TIME_MS)
            }
        }
    }

    private fun combineRemoteAndLocalStations(localStations: List<Station>?, remoteStations: List<Station>?): List<Station> {
        val stationList = mutableListOf<Station>()
        localStations?.let {
            it.forEach { station ->
                station.randomizeAvailability()
            }
            stationList.addAll(it)
        }
        remoteStations?.let {
            stationList.addAll(it)
        }
        return stationList.toList()
    }

    private fun List<Station>.applyUserFiltersToStations(userInfo: UserInfo?): List<Station> {
        var resultingList = this
        userInfo?.filterProperties?.chargingType?.let {
            resultingList = resultingList.filterByChargingType(it)
        }
        userInfo?.filterProperties?.chargerType?.let {
            resultingList = resultingList.filterByChargerType(it)
        }
        return resultingList
    }

}