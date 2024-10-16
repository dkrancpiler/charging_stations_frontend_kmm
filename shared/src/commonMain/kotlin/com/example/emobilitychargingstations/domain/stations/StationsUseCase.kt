package com.example.emobilitychargingstations.domain.stations

import com.example.emobilitychargingstations.PlatformSpecificFunctions
import com.example.emobilitychargingstations.SHOULD_TRY_API_REQUEST
import com.example.emobilitychargingstations.STATION_REQUEST_REPEAT_TIME_MS
import com.example.emobilitychargingstations.data.extensions.filterByChargerType
import com.example.emobilitychargingstations.data.extensions.filterByChargingType
import com.example.emobilitychargingstations.data.extensions.randomizeAvailability
import com.example.emobilitychargingstations.data.stations.StationsRepository
import com.example.emobilitychargingstations.data.stations.toStationDataModel
import com.example.emobilitychargingstations.domain.user.UserUseCase
import com.example.emobilitychargingstations.models.StationDataModel
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

    private suspend fun insertStations(stationList: List<StationDataModel>) {
        stationsRepository.insertStations(
            stationList
        )
    }

    private suspend fun getStationsLocal(userLocation: UserLocation): List<StationDataModel>? {
        val checkIfStationsExist = stationsRepository.checkIfStationsExistLocally()
        var localStations: List<StationDataModel>? =
            stationsRepository.getStationsLocallyByLatLng(userLocation)
        if (checkIfStationsExist != true) {
            val stationsFromJson = PlatformSpecificFunctions().getStationsFromJson()
            stationsFromJson?.let {
                it.features?.let { stationsJsonList ->
                    localStations = stationsJsonList.map { stationJson ->
                        stationJson.toStationDataModel()
                    }
                    insertStations(localStations!!)
                }
            }
        }
        return localStations
    }

    fun setTemporaryLocation(newLocation: UserLocation?) {
        userLocation = newLocation
    }

    fun startRepeatingRequest(initialLocation: UserLocation?) = channelFlow {
        launch(Dispatchers.IO) {
            val localStations = getStationsLocal(initialLocation!!)
            var userInfo = userUseCase.getUserInfo()
            val localStationsWithUserFilters = localStations?.applyUserFiltersToStations(userInfo)
            userLocation = initialLocation
            send(localStationsWithUserFilters)
            launch {
                userUseCase.getUserInfoAsFlow().onEach { userInfoChange ->
                    if ((userInfo?.filterProperties?.chargingType != userInfoChange?.filterProperties?.chargingType
                                || userInfo?.filterProperties?.chargerType != userInfoChange?.filterProperties?.chargerType)) {
                        userInfo = userInfoChange
                        localStations?.let { localStations ->
                            val resultingList = localStations.applyUserFiltersToStations(userInfo)
                            send(resultingList)
                        }
                    }
                }.collect()
            }
            while (true) {
                val remoteStationJsons = mutableListOf<StationDataModel>()
                if (PlatformSpecificFunctions().isDebug && SHOULD_TRY_API_REQUEST) {
                    stationsRepository.getStationsRemote(userLocation).onRight {
                        remoteStationJsons.addAll(it.map { stationsResponseModel ->
                            stationsResponseModel.toStationDataModel() }
                        )
                    }.onLeft {
                        print(it.toString())
                    }
                }
                val resultingList = combineRemoteAndLocalStations(localStations ?: listOf(), remoteStationJsons)
//                    .getStationsClosestToUserLocation(userLocation)
                    .applyUserFiltersToStations(userInfo)
                send(resultingList)
                delay(STATION_REQUEST_REPEAT_TIME_MS)
            }
        }
    }

    private fun combineRemoteAndLocalStations(localStationJsons: List<StationDataModel>, remoteStationJsons: List<StationDataModel>): List<StationDataModel> {
//        val stationJsonList = mutableListOf<StationJson>()
//        remoteStationJsons?.let {
//            stationJsonList.addAll(it)
//        }
        val resultingList = mutableListOf<StationDataModel>()
        resultingList.addAll(remoteStationJsons)
        localStationJsons.let {
            it.forEach { station ->
                station.randomizeAvailability()
            }
            resultingList.addAll(it)
        }
        return resultingList
    }

    private fun List<StationDataModel>.applyUserFiltersToStations(userInfo: UserInfo?): List<StationDataModel> {
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