package com.example.emobilitychargingstations.android.ui.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.ui.auto.BaseScreen
import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import com.example.emobilitychargingstations.android.ui.utilities.getFavoritesAction
import com.example.emobilitychargingstations.android.ui.utilities.getMessageTemplateBuilderWithTitle
import com.example.emobilitychargingstations.android.ui.utilities.getString
import com.example.emobilitychargingstations.models.UserInfo
import kotlinx.coroutines.launch

class NavigationCompleteScreen(carContext: CarContext, private val stationJson: StationsUiModel): BaseScreen(carContext) {

    override fun onGetTemplate(): Template {
        val userInfo = userUseCase.getUserInfo()
        val isAlreadyInFavorites = userInfo?.favoriteStationJsons?.firstOrNull { it.id == stationJson.id }?.let { true } ?: false
        val title = getString(R.string.auto_navigation_complete_title)
        val body = if (isAlreadyInFavorites) getString(R.string.auto_navigation_complete_already_in_favorite_message) else getString(R.string.auto_navigation_complete_message)
        val messageTemplateBuilder = getMessageTemplateBuilderWithTitle(title, body)
        messageTemplateBuilder.apply {
            addAction(getFavoritesAction(stationJson, userInfo, ::onFavoriteChanged))
        }
        return messageTemplateBuilder.build()
    }

    private fun onFavoriteChanged(userInfo: UserInfo) {
        lifecycleScope.launch {
            userUseCase.setUserInfo(userInfo)
            screenManager.pop()
        }
    }
}