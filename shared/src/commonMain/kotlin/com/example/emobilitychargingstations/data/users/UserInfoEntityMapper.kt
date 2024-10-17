package com.example.emobilitychargingstations.data.users

import com.emobilitychargingstations.database.UserInfoEntity
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation

fun UserInfoEntity.toUserInfo() = UserInfo(
    filterProperties = this.filterProperties,
    favoriteStationJsons = this.favoriteStations?.toMutableList(),
    userLocation = UserLocation(this.lastKnownLatitude ?: 0.0, this.lastKnownLongitude ?: 0.0)
)