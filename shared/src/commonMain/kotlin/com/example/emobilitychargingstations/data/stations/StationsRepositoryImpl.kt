package com.example.emobilitychargingstations.data.stations

import arrow.core.Either
import com.emobilitychargingstations.database.StationsDatabase
import com.example.emobilitychargingstations.data.extensions.getPowerRangeFromChargingType
import com.example.emobilitychargingstations.data.stations.api.StationsApi
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.StationDataModel
import com.example.emobilitychargingstations.models.StationsResponseModel
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StationsRepositoryImpl(stationsDatabase: StationsDatabase, private val stationsApi: StationsApi) :
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

    override suspend fun getStationsLocallyByLatLng(userInfo: UserInfo): List<StationDataModel> {
        val powerRange = userInfo.filterProperties?.chargingType.getPowerRangeFromChargingType()
        val maxLng = userInfo.userLocation.longitude.plus(0.5)
        val minLng = userInfo.userLocation.longitude.minus(0.5)
        val maxLat = userInfo.userLocation.latitude.plus(0.5)
        val minLat = userInfo.userLocation.latitude.minus(0.5)
        val chargerTypeString = if (userInfo.filterProperties?.chargerType != ChargerTypesEnum.ALL && userInfo.filterProperties != null) ("%" + Json.encodeToString(userInfo.filterProperties.chargerType?.name) + "%")
        else "%%"
        return queries.getAllStationsByLatLng(
            longitudeMin = minLng,
            longitudeMax = maxLng,
            latitudeMin = minLat,
            latitudeMax = maxLat,
            minimumPower = powerRange.first.toDouble(),
            maximumPower = powerRange.second.toDouble(),
            chargerType = chargerTypeString

        ).executeAsList().map { it.toStationDataModel()}
    }

    override suspend fun getLimitedStationsLocallyByUserInfo(
        userInfo: UserInfo,
        limit: Int
    ): List<StationDataModel> {
        val powerRange = userInfo.filterProperties?.chargingType.getPowerRangeFromChargingType()
        val chargerTypeString = if (userInfo.filterProperties?.chargerType != ChargerTypesEnum.ALL && userInfo.filterProperties != null) ("%" + Json.encodeToString(userInfo.filterProperties.chargerType?.name) + "%")
        else "%%"
        return queries.getLimitedClosestStationsByLatLngAndUserFilters(
            userLongitude = userInfo.userLocation.longitude,
            userLatitude = userInfo.userLocation.latitude,
            minimumPower = powerRange.first.toDouble(),
            maximumPower = powerRange.second.toDouble(),
            chargerType = chargerTypeString,
            limit = limit.toLong()
        ).executeAsList().map { it.toStationDataModel() }
    }
}