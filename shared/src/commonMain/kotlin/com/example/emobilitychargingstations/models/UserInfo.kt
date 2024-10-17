package com.example.emobilitychargingstations.models

data class UserInfo(
    val filterProperties: StationFilterProperties?,
    val favoriteStationJsons: MutableList<StationDataModel>?,
    val userLocation: UserLocation
)
