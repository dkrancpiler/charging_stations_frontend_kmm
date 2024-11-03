package com.example.emobilitychargingstations.android.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.emobilitychargingstations.domain.user.UserUseCase
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.ChargingTypeEnum
import com.example.emobilitychargingstations.models.FavoriteStationDataModel
import com.example.emobilitychargingstations.models.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(
    private val userUseCase: UserUseCase
): ViewModel() {

    private val _userInfo = mutableStateOf<UserInfo?>(null)
    val userInfo: State<UserInfo?> = _userInfo

    fun setChargerType(chargerName: ChargerTypesEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.setChargerType(chargerName)
        }
    }

    fun updateFavorites(newFavoriteList: List<FavoriteStationDataModel>) {
        viewModelScope.launch {
            userUseCase.setFavoriteList(newFavoriteList)
        }
    }

    fun setChargingType(chargingType: ChargingTypeEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.setChargingType(chargingType)
        }
    }

    fun startObservingForFavorites() {
        userUseCase.startObservingForFavorites { userInfo ->
            onUserInfoChange(userInfo)
        }
    }

    fun getUserInfo(): UserInfo? = userUseCase.getUserInfo()

    private fun onUserInfoChange(userInfo: UserInfo) {
        if (userInfo != _userInfo.value) {
            _userInfo.value = userInfo
        }
    }
}