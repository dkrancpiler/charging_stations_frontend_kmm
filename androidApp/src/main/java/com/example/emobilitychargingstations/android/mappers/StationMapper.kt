package com.example.emobilitychargingstations.android.mappers

import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import com.example.emobilitychargingstations.models.StationDataModel

fun StationDataModel.toStationUIModel(): StationsUiModel = StationsUiModel(
    id = this.id,
    numberOfChargers = this.numberOfChargers,
    maximumPowerInKw = this.maximumPowerInKw,
    operator = this.operator ?: "",
    listOfChargerTypes = this.listOfChargerTypes,
    street = this.street ?: "",
    town = this.town ?: "",
    latitude = this.latitude ?: 0.0,
    longitude = this.longitude ?: 0.0,
)

fun StationsUiModel.toStationDataModel(): StationDataModel = StationDataModel(
    id = this.id,
    numberOfChargers = this.numberOfChargers,
    maximumPowerInKw = this.maximumPowerInKw,
    operator = this.operator ?: "",
    listOfChargerTypes = this.listOfChargerTypes,
    street = this.street ?: "",
    town = this.town ?: "",
    latitude = this.latitude ?: 0.0,
    longitude = this.longitude ?: 0.0,
    dataSource = null,
    dcSupport = null
)