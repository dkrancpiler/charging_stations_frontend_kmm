package com.example.emobilitychargingstations.data.users

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import com.emobilitychargingstations.database.StationsDatabase
import com.emobilitychargingstations.database.UserInfoEntity
import com.example.emobilitychargingstations.data.stations.toStationDataModel
import com.example.emobilitychargingstations.models.FavoriteStationDataModel
import com.example.emobilitychargingstations.models.StationFilterProperties
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UsersRepositoryImpl(stationsDatabase: StationsDatabase): UsersRepository {

    private val queries = stationsDatabase.stationsQueries

    override fun getUserInfo(): UserInfo? {
        return queries.getUserInfo().executeAsOneOrNull()?.toUserInfo()
    }

    override suspend fun getUserInfoAsFlow(shouldReturnFavorites: Boolean): Flow<UserInfo?> {
        return queries.getUserInfo().asFlow().map { value: Query<UserInfoEntity> ->
            val userInfo = value.executeAsOneOrNull()
            val favoriteStations: MutableList<FavoriteStationDataModel> = mutableListOf()
            if (shouldReturnFavorites) userInfo?.favoriteStations?.forEach {
                val station = queries.getStationById(it.stationId).executeAsOne().toStationDataModel()
                favoriteStations.add(FavoriteStationDataModel(
                    nickname = it.stationNickname,
                    station = station
                ))
            }
            userInfo?.toUserInfo(favoriteStations) }
    }

    override suspend fun updateUserLocation(userLocation: UserLocation) {
        val userInfo = getUserInfo()
        if (userInfo != null) queries.updateUserLocation(lastKnownLatitude = userLocation.latitude, lastKnownLongitude = userLocation.longitude)
        else queries.insertUserInfo(null, null, userLocation.latitude, userLocation.longitude)
    }

    override suspend fun updateUserFilters(filterProperties: StationFilterProperties) {
        val userInfo = getUserInfo()
        if (userInfo != null) queries.updateFilterProperties(filterProperties)
        else queries.insertUserInfo(filterProperties, null, null, null)
    }

    override suspend fun updateUserFavorites(favoriteList: List<FavoriteStationDataModel>) {
        val userInfo = getUserInfo()
        val mappedFavorites = favoriteList.map { it.toFavoriteStationModel() }
        if (userInfo != null) queries.updateFavoriteStations(mappedFavorites)
        else queries.insertUserInfo(null, mappedFavorites, null, null)
    }

    override suspend fun setUserInfo(userInfo: UserInfo) {
        queries.insertUserInfo(userInfo.filterProperties, userInfo.favoriteStationsList?.map { it.toFavoriteStationModel() }, userInfo.userLocation.latitude, userInfo.userLocation.longitude)
    }
}