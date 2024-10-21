package com.example.emobilitychargingstations.android.ui.auto

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Template
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.emobilitychargingstations.domain.stations.StationsUseCase
import com.example.emobilitychargingstations.domain.user.UserUseCase
import com.example.emobilitychargingstations.models.FavoriteStationDataModel
import com.example.emobilitychargingstations.models.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseScreen(carContext: CarContext): Screen(carContext), KoinComponent {

    val userInfoForFavorites: MutableLiveData<UserInfo> = MutableLiveData()

    val userUseCase by inject<UserUseCase> ()
    val stationsUseCase by inject<StationsUseCase> ()

    fun startObservingForFavorites() {
        CoroutineScope(Dispatchers.IO).launch {
            userUseCase.getUserInfoWithFavoritesAsFlow().collectLatest { userInfoData ->
                userInfoData?.let {
                    if (it.favoriteStationsList != userInfoForFavorites.value?.favoriteStationsList) {
                        userInfoForFavorites.postValue(it)
                    }
                }
            }
        }
    }

    fun onFavoriteRemoved(favoriteStationDataModel: FavoriteStationDataModel, userInfo: UserInfo) {
        val newList:MutableList<FavoriteStationDataModel>? = userInfo.favoriteStationsList?.toMutableList()
        newList?.let {
            it.remove(favoriteStationDataModel)
            lifecycleScope.launch {
                userUseCase.setFavoriteList(it)
                screenManager.pop()
            }
        }

    }

    fun onFavoriteAdded(favoriteStationDataModel: FavoriteStationDataModel, userInfo: UserInfo) {
        val newList:MutableList<FavoriteStationDataModel> = userInfo.favoriteStationsList?.toMutableList() ?: mutableListOf()
        newList.add(favoriteStationDataModel)
        lifecycleScope.launch {
            userUseCase.setFavoriteList(newList)
            screenManager.pop()
        }
    }


    abstract override fun onGetTemplate(): Template
}