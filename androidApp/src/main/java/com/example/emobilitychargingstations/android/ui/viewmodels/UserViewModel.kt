package com.example.emobilitychargingstations.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emobilitychargingstations.domain.user.UserUseCase
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.ChargingTypeEnum
import com.example.emobilitychargingstations.models.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(
    private val userUseCase: UserUseCase
): ViewModel() {

    fun setChargerType(chargerName: ChargerTypesEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.setChargerType(chargerName)
        }
    }

    fun setChargingType(chargingType: ChargingTypeEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.setChargingType(chargingType)
        }
    }

    fun getUserInfo(): UserInfo? = userUseCase.getUserInfo()

}