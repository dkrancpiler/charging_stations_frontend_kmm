package com.example.emobilitychargingstations.models

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteStationModel (
    val stationId: Long,
    val stationNickname: String? = null
)
