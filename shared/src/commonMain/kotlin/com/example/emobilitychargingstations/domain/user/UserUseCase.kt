package com.example.emobilitychargingstations.domain.user

import com.example.emobilitychargingstations.data.users.UsersRepository
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.ChargingTypeEnum
import com.example.emobilitychargingstations.models.FavoriteStationDataModel
import com.example.emobilitychargingstations.models.StationFilterProperties
import com.example.emobilitychargingstations.models.UserInfo
import com.example.emobilitychargingstations.models.UserLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserUseCase(private val usersRepository: UsersRepository) {
    fun getUserInfo(): UserInfo? {
        return usersRepository.getUserInfo()
    }

    fun startObservingForFavorites(test: (userInfo: UserInfo) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            getUserInfoWithFavoritesAsFlow().collectLatest { userInfoData ->
                userInfoData?.let {
                    test(it)
//                    if (it.favoriteStationsList != userInfoForFavorites.value?.favoriteStationsList) {
//                        userInfoForFavorites.postValue(it)
//                    }
                }
            }
        }
    }

    suspend fun getUserInfoWithFavoritesAsFlow(): Flow<UserInfo?> {
        return usersRepository.getUserInfoAsFlow(shouldReturnFavorites = true)
    }

    suspend fun getUserInfoAsFlow(): Flow<UserInfo?> {
        return usersRepository.getUserInfoAsFlow()
    }

    suspend fun setChargerType(chargerTypesEnum: ChargerTypesEnum) {
        val userInfo = getUserInfo()
        val newFilterProperties = userInfo?.filterProperties?.copy(chargerType = chargerTypesEnum)?: StationFilterProperties(chargerType = chargerTypesEnum)
        usersRepository.updateUserFilters(newFilterProperties)
    }

    suspend fun setChargingType(chargingTypeEnum: ChargingTypeEnum) {
        val userInfo = getUserInfo()
        val newFilterProperties = userInfo?.filterProperties?.copy(chargingType = chargingTypeEnum) ?: StationFilterProperties(chargingType = chargingTypeEnum)
        usersRepository.updateUserFilters(newFilterProperties)

    }

    suspend fun setUserLocation(userLocation: UserLocation) {
        usersRepository.updateUserLocation(userLocation)
    }

    suspend fun setFavoriteList(favoritesList: List<FavoriteStationDataModel>) {
        usersRepository.updateUserFavorites(favoritesList)
    }
}