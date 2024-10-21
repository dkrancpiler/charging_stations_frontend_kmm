package com.example.emobilitychargingstations.android.ui.models

import com.example.emobilitychargingstations.models.ChargerTypesEnum

data class StationsUiModel (
    val id: Long,
    val numberOfChargers: Int?,
    val maximumPowerInKw: Double?,
    val operator: String,
    val listOfChargerTypes: List<ChargerTypesEnum>?,
    val street: String,
    val town: String,
    val latitude: Double,
    val longitude: Double,
    var availableChargingStations: Int = 0,
    var isNavigatingTo: Boolean = false
)
