package com.example.emobilitychargingstations.models

data class UserInfo(
    val filterProperties: StationFilterProperties?,
    val favoriteStationsList: List<FavoriteStationDataModel>?,
    val userLocation: UserLocation
)
