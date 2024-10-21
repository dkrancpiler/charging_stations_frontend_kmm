package com.example.emobilitychargingstations.android.ui.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.model.Template
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.ui.auto.BaseScreen
import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import com.example.emobilitychargingstations.android.ui.utilities.getFavoritesAction
import com.example.emobilitychargingstations.android.ui.utilities.getMessageTemplateBuilderWithTitle
import com.example.emobilitychargingstations.android.ui.utilities.getString

class NavigationCompleteScreen(carContext: CarContext, private val stationJson: StationsUiModel): BaseScreen(carContext) {

    init {
        userInfoForFavorites.observe(this) {
            invalidate()
        }
        startObservingForFavorites()
    }

    override fun onGetTemplate(): Template {
        val isAlreadyInFavorites = userInfoForFavorites.value?.favoriteStationsList?.firstOrNull { it.station.id == stationJson.id }?.let { true } ?: false
        val title = getString(R.string.auto_navigation_complete_title)
        val body = if (isAlreadyInFavorites) getString(R.string.auto_navigation_complete_already_in_favorite_message) else getString(R.string.auto_navigation_complete_message)
        val messageTemplateBuilder = getMessageTemplateBuilderWithTitle(title, body)
        messageTemplateBuilder.apply {
            if (userInfoForFavorites.value == null) setLoading(true)
            else addAction(getFavoritesAction(stationJson, userInfoForFavorites.value!!, ::onFavoriteAdded, ::onFavoriteRemoved))
        }
        return messageTemplateBuilder.build()
    }

}