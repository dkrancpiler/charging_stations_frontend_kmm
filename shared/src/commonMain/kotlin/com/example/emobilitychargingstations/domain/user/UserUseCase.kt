package com.example.emobilitychargingstations.domain.user

import com.example.emobilitychargingstations.data.users.UsersRepository
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.ChargingTypeEnum
import com.example.emobilitychargingstations.models.StationFilterProperties
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation
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
        val userInfo = getUserInfo()
        val newUserInfo = userInfo?.copy(filterProperties = userInfo.filterProperties?.copy(chargerType = chargerTypesEnum)?: StationFilterProperties(chargerType = chargerTypesEnum))
        newUserInfo?.let {
            setUserInfo(it)
        }
    }

    suspend fun setChargingType(chargingTypeEnum: ChargingTypeEnum) {
        val userInfo = getUserInfo()
        val newUserInfo = userInfo?.copy(filterProperties = userInfo.filterProperties?.copy(chargingType = chargingTypeEnum) ?: StationFilterProperties(chargingType = chargingTypeEnum))
        newUserInfo?.let {
            setUserInfo(it)
        }
    }

    suspend fun setUserLocation(userLocation: UserLocation) {
        val userInfo = getUserInfo()
        val newUserInfo = userInfo?.copy(userLocation = userLocation)
            ?: UserInfo(favoriteStationJsons = null, filterProperties = null, userLocation = userLocation)
        setUserInfo(newUserInfo)
    }
}