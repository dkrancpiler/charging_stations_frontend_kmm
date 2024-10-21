package com.example.emobilitychargingstations.data.users

import com.emobilitychargingstations.database.UserInfoEntity
import com.example.emobilitychargingstations.models.FavoriteStationDataModel
import com.example.emobilitychargingstations.models.FavoriteStationModel
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation

fun UserInfoEntity.toUserInfo(favoriteStations: List<FavoriteStationDataModel>? = null) = UserInfo(
    filterProperties = this.filterProperties,
    favoriteStationsList = favoriteStations?.toMutableList(),
    userLocation = UserLocation(this.lastKnownLatitude ?: 0.0, this.lastKnownLongitude ?: 0.0)
)

fun FavoriteStationDataModel.toFavoriteStationModel() = FavoriteStationModel(
    stationId = this.station.id,
    stationNickname = this.nickname
)