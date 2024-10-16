package com.example.emobilitychargingstations.data.stations

import arrow.core.Either
import com.emobilitychargingstations.database.StationsDatabase
import com.example.emobilitychargingstations.data.stations.api.StationsApi
import com.example.emobilitychargingstations.models.StationDataModel
import com.example.emobilitychargingstations.models.StationsResponseModel
import com.example.emobilitychargingstations.models.UserLocation

class StationsRepositoryImpl(stationsDatabase: StationsDatabase, val stationsApi: StationsApi) :
    StationsRepository {

    private val queries = stationsDatabase.stationsQueries

    override suspend fun insertStations(stationsList: List<StationDataModel>) {
        queries.transaction {
            stationsList.forEach { station ->
                queries.insertStation(id = null,
                    latitude = station.latitude,
                    longitude = station.longitude,
                    numberOfChargers = station.numberOfChargers?.toLong(),
                    maximumPowerInKw = station.maximumPowerInKw,
                    operator_ = station.operator,
                    listOfChargerTypes = station.listOfChargerTypes,
                    street = station.street,
                    town = station.town,
                    dataSource = station.dataSource,
                    dcSupport = station.dcSupport,
                )
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

    override suspend fun getStationsLocallyByLatLng(userLocation: UserLocation?): List<StationDataModel> {
        val maxLng = userLocation?.longitude?.plus(0.5)
        val minLng = userLocation?.longitude?.minus(0.5)
        val maxLat = userLocation?.latitude?.plus(0.5)
        val minLat = userLocation?.latitude?.minus(0.5)
        return queries.getAllStationsByLatLng(minLng, maxLng, minLat, maxLat).executeAsList().map { it.toStationDataModel()}
    }
}