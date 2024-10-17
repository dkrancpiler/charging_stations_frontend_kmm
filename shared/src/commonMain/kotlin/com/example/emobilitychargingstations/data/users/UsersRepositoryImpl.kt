package com.example.emobilitychargingstations.data.users

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import com.emobilitychargingstations.database.StationsDatabase
import com.emobilitychargingstations.database.UserInfoEntity
import com.example.emobilitychargingstations.models.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UsersRepositoryImpl(stationsDatabase: StationsDatabase): UsersRepository {

    private val queries = stationsDatabase.stationsQueries
    override fun getUserInfo(): UserInfo? {
        return queries.getUserInfo().executeAsOneOrNull()?.toUserInfo()
    }

    override suspend fun getUserInfoAsFlow(): Flow<UserInfo?> {
        return queries.getUserInfo().asFlow().map { value: Query<UserInfoEntity> ->
            value.executeAsOneOrNull()?.toUserInfo() }
    }

    override suspend fun setUserInfo(userInfo: UserInfo) {
        queries.insertUserInfo(userInfo.filterProperties, userInfo.favoriteStationJsons, userInfo.userLocation.latitude, userInfo.userLocation.longitude)
    }
}