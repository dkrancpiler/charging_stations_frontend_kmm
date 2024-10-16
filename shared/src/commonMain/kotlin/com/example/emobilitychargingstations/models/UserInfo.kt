package com.example.emobilitychargingstations.models

data class UserInfo(
    val filterProperties: StationFilterProperties?,
    val favoriteStationJsons: MutableList<StationDataModel>?
) {
    fun initializeEmptyUserInfo(): UserInfo = UserInfo(StationFilterProperties(), mutableListOf())
}
