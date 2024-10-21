package com.example.emobilitychargingstations.models

data class StationDataModel(
    val id: Long,
    val numberOfChargers: Int?,
    val maximumPowerInKw: Double?,
    val operator: String,
    val listOfChargerTypes: List<ChargerTypesEnum>?,
    val street: String,
    val town: String,
    val dataSource: String?,
    val dcSupport: Boolean?,
    val latitude: Double?,
    val longitude: Double?,
    var availableChargingStations: Int = 0
)
