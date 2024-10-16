package com.example.emobilitychargingstations.data.stations

import arrow.core.Either
import com.example.emobilitychargingstations.models.StationDataModel
import com.example.emobilitychargingstations.models.StationsResponseModel
import com.example.emobilitychargingstations.models.UserLocation

interface StationsRepository {
    suspend fun insertStations(stationsJsonModel: List<StationDataModel>)
    suspend fun checkIfStationsExistLocally(): Boolean?
    suspend fun getStationsLocallyByLatLng(userLocation: UserLocation?): List<StationDataModel>

    suspend fun getStationsRemote(userLocation: UserLocation?): Either<Exception, List<StationsResponseModel>>
}