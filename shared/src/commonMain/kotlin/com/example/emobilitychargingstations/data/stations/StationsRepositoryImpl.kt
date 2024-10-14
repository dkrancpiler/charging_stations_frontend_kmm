package com.example.emobilitychargingstations.data.stations

import app.cash.sqldelight.coroutines.asFlow
import arrow.core.Either
import com.emobilitychargingstations.database.StationEntity
import com.emobilitychargingstations.database.StationsDatabase
import com.example.emobilitychargingstations.data.stations.api.StationsApi
import com.example.emobilitychargingstations.models.Station
import com.example.emobilitychargingstations.models.Stations
import com.example.emobilitychargingstations.models.StationsResponseModel
import com.example.emobilitychargingstations.models.UserLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

class StationsRepositoryImpl(stationsDatabase: StationsDatabase, val stationsApi: StationsApi) :
    StationsRepository {

    private val queries = stationsDatabase.stationsQueries

    override suspend fun insertStations(stations: Stations) {
        queries.transaction {
            stations.features?.forEach { station ->
                queries.insertStation(id = null, latitude = station.geometry.coordinates[1], longitude = station.geometry.coordinates[0], properties = station.properties)
            }
        }
    }

    override suspend fun checkIfStationsExistLocally(): Boolean {
        val randomStation = queries.checkIfStationsExist().executeAsOneOrNull()
        return randomStation != null
    }

    override suspend fun getStationsRemote(userLocation: UserLocation?): Either<Exception, List<StationsResponseModel>> {
        return stationsApi.requestStationsWithLocation(userLocation)
    }

    override suspend fun getStationsLocallyByLatLng(userLocation: UserLocation?): List<Station> {
        val maxLng = userLocation?.longitude?.plus(0.5)
        val minLng = userLocation?.longitude?.minus(0.5)
        val maxLat = userLocation?.latitude?.plus(0.5)
        val minLat = userLocation?.latitude?.minus(0.5)
        return queries.getAllStationsByLatLng(minLng, maxLng, minLat, maxLat).executeAsList().map { it.toStation()}
    }
}