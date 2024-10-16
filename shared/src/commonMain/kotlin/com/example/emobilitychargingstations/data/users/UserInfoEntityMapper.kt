package com.example.emobilitychargingstations.data.users

import com.emobilitychargingstations.database.UserInfoEntity
import com.example.emobilitychargingstations.models.UserInfo

fun UserInfoEntity.toUserInfo() = UserInfo(
    filterProperties = this.filterProperties,
    favoriteStationJsons = this.favoriteStations?.toMutableList()
)