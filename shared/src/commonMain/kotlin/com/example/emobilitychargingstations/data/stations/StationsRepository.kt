package com.example.emobilitychargingstations.data.stations

import arrow.core.Either
import com.example.emobilitychargingstations.models.StationDataModel
import com.example.emobilitychargingstations.models.StationsResponseModel
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation

interface StationsRepository {
    suspend fun insertStations(stationsList: List<StationDataModel>)
    suspend fun checkIfStationsExistLocally(): Boolean?
    suspend fun getStationsLocallyByLatLng(userInfo: UserInfo): List<StationDataModel>
    suspend fun getLimitedStationsLocallyByUserInfo(userInfo: UserInfo, limit: Int): List<StationDataModel>

    suspend fun getStationsRemote(userLocation: UserLocation?): Either<Exception, List<StationsResponseModel>>
}