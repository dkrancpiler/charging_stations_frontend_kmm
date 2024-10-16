package com.example.emobilitychargingstations.domain.user

import com.example.emobilitychargingstations.data.users.UsersRepository
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.ChargingTypeEnum
import com.example.emobilitychargingstations.models.StationFilterProperties
import com.example.emobilitychargingstations.models.UserInfo
import kotlinx.coroutines.flow.Flow

class UserUseCase(private val usersRepository: UsersRepository) {
    fun getUserInfo(): UserInfo? {
        return usersRepository.getUserInfo()
    }

    suspend fun getUserInfoAsFlow(): Flow<UserInfo?> {
        return usersRepository.getUserInfoAsFlow()
    }

    suspend fun setUserInfo(userInfo: UserInfo) {
        usersRepository.setUserInfo(userInfo)
    }

    suspend fun setChargerType(chargerTypesEnum: ChargerTypesEnum) {
        var userInfo = getUserInfo()
        userInfo = userInfo?.copy(filterProperties = userInfo.filterProperties?.copy(chargerType = chargerTypesEnum))
            ?: UserInfo(favoriteStationJsons = null, filterProperties = StationFilterProperties(chargerType = chargerTypesEnum))
        setUserInfo(userInfo)
    }

    suspend fun setChargingType(chargingTypeEnum: ChargingTypeEnum) {
        var userInfo = getUserInfo()
        userInfo = userInfo?.copy(filterProperties = userInfo.filterProperties?.copy(chargingType = chargingTypeEnum))
            ?: UserInfo(favoriteStationJsons = null, filterProperties = StationFilterProperties(chargingType = chargingTypeEnum))
        setUserInfo(userInfo)
    }
}