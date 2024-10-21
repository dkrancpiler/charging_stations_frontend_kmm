package com.example.emobilitychargingstations.data.users

import com.example.emobilitychargingstations.models.FavoriteStationDataModel
import com.example.emobilitychargingstations.models.StationFilterProperties
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    fun getUserInfo(): UserInfo?

    suspend fun getUserInfoAsFlow(shouldReturnFavorites: Boolean = false): Flow<UserInfo?>

    suspend fun updateUserLocation(userLocation: UserLocation)

    suspend fun updateUserFilters(filterProperties: StationFilterProperties)

    suspend fun updateUserFavorites(favoriteList: List<FavoriteStationDataModel>)

    suspend fun setUserInfo(userInfo: UserInfo)
}