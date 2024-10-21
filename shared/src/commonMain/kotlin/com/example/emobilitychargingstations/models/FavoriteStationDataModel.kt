package com.example.emobilitychargingstations.models

data class FavoriteStationDataModel(
    val station: StationDataModel,
    val nickname: String? = null
)
