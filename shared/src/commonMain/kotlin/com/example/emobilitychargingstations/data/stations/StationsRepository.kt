package com.example.emobilitychargingstations.data.stations

import arrow.core.Either
import com.example.emobilitychargingstations.models.Station
import com.example.emobilitychargingstations.models.Stations
import com.example.emobilitychargingstations.models.StationsResponseModel
import com.example.emobilitychargingstations.models.UserLocation

interface StationsRepository {
    suspend fun insertStations(stations: Stations)
    suspend fun checkIfStationsExistLocally(): Boolean?
    suspend fun getStationsLocallyByLatLng(userLocation: UserLocation?): List<Station>

    suspend fun getStationsRemote(userLocation: UserLocation?): Either<Exception, List<StationsResponseModel>>
}