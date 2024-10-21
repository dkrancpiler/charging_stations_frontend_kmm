package com.example.emobilitychargingstations.data.stations

import com.emobilitychargingstations.database.StationEntity
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.StationDataModel
import com.example.emobilitychargingstations.models.StationGeoData
import com.example.emobilitychargingstations.models.StationJson
import com.example.emobilitychargingstations.models.StationProperties
import com.example.emobilitychargingstations.models.StationsResponseModel

fun StationEntity.toStationDataModel(): StationDataModel {
    return StationDataModel(
        id = this.id,
        numberOfChargers = this.numberOfChargers?.toInt(),
        maximumPowerInKw = this.maximumPowerInKw,
        operator = this.operator_ ?: "",
        listOfChargerTypes = this.listOfChargerTypes,
        street = this.street ?: "",
        town = this.town ?: "",
        dataSource = this.dataSource,
        dcSupport = this.dcSupport,
        latitude = this.latitude,
        longitude = this.longitude,
    )
}

fun StationJson.toStationDataModel(): StationDataModel = StationDataModel(
    id = this.id,
    numberOfChargers = this.properties.capacity?.toInt(),
    maximumPowerInKw = this.properties.max_kw,
    operator = this.properties.operator ?: "",
    listOfChargerTypes = this.properties.socket_type_list?.map
    {
        stringToChargerType(it)
    } ?: if (this.properties.operator?.lowercase()?.contains("tesla") == true) listOf(ChargerTypesEnum.TESLA)
    else null,
    street = this.properties.street ?: "",
    town = this.properties.town ?: "",
    dataSource = this.properties.data_source,
    dcSupport = this.properties.dc_support,
    latitude = this.geometry.coordinates[1],
    longitude = this.geometry.coordinates[0],
)

fun StationsResponseModel.toStationDataModel(): StationDataModel = StationDataModel(
    id = this.id,
    numberOfChargers = this.totalChargingStations,
    maximumPowerInKw = null,
    operator = this.operator ?: "",
    listOfChargerTypes = null,
    street = this.street ?: "",
    town = this.town ?: "",
    dataSource = this.dataSource,
    dcSupport = null,
    latitude = this.latitude,
    longitude = this.longitude,
)

private fun stringToChargerType(value: String): ChargerTypesEnum {
    with(value.lowercase()) {
        print(value.lowercase())
        return when {
            contains("typ2") || contains("typ 2") -> ChargerTypesEnum.AC_TYPE_2
            contains("typ1") || contains("typ 1") -> ChargerTypesEnum.AC_TYPE_1
            contains("dc kupplung combo") -> ChargerTypesEnum.DC_EU
            contains("chademo") -> ChargerTypesEnum.DC_CHADEMO
            contains("tesla") || contains("tesla".uppercase()) -> ChargerTypesEnum.TESLA
            else -> ChargerTypesEnum.UNKNOWN
        }
    }
}

fun List<StationsResponseModel>.toStationList(): List<StationJson> {
    val resultingList = mutableListOf<StationJson>()
    this.forEach {
        val stationJson = StationJson(
            id = it.id,
            type = null,
            properties = StationProperties(
                capacity = it.totalChargingStations.toDouble(),
                data_source = it.dataSource,
                dc_support = null,
                max_kw = null,
                operator = it.operator,
//                socket_type_list = it.connections,
                socket_type_list = null,
                station_id = it.stationId,
                street = it.street,
                total_kw = null,
                town = it.town,
                availableChargingStations = it.availableChargingStations
            ),
            geometry = StationGeoData(
                type = "POINT",
                coordinates = arrayOf(it.longitude, it.latitude)
            )
        )
        resultingList.add(stationJson)
    }
    return resultingList
}