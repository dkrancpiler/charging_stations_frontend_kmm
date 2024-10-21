package com.example.emobilitychargingstations.android.ui.auto.screen

import android.text.SpannableString
import androidx.car.app.CarContext
import androidx.car.app.OnScreenResultListener
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Template
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.mappers.toStationUIModel
import com.example.emobilitychargingstations.android.ui.auto.BaseScreen
import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import com.example.emobilitychargingstations.android.ui.utilities.buildClickableRowWithTextAndIcon
import com.example.emobilitychargingstations.android.ui.utilities.getDrawableAsBitmap
import com.example.emobilitychargingstations.android.ui.utilities.getString

class FavoritesListScreen(carContext: CarContext, private val onScreenResultListener: OnScreenResultListener? = null): BaseScreen(carContext) {

    init {
        userInfoForFavorites.observe(this) {
            invalidate()
        }
        startObservingForFavorites()
    }

    override fun onGetTemplate(): Template {
        val templateTitle = getString(R.string.auto_favorites_list_title)
        val templateBuilder = ListTemplate.Builder()
        templateBuilder.apply {
            setHeaderAction(Action.BACK)
            setTitle(templateTitle)
            if (userInfoForFavorites.value == null) templateBuilder.setLoading(true)
            else setSingleList(ItemList.Builder().apply {
                userInfoForFavorites.value!!.favoriteStationsList!!.forEach { favorite ->
                    addItem(
                        buildClickableRowWithTextAndIcon(
                            title = SpannableString(favorite.nickname ?: favorite.station.street),
                            text = favorite.station.operator ?: "",
                            carIcon = getDrawableAsBitmap(
                                R.drawable.electric_car_icon_white
                            )!!
                        ) {
                            onItemClick(favorite.station.toStationUIModel())
                        }
                    )
                }
            }.build())
        }
        val templateForDisplay = templateBuilder.build()
        return templateForDisplay
    }

    private fun onItemClick(stationJson: StationsUiModel) {
        if (onScreenResultListener != null) screenManager.pushForResult(StationDetailsScreen(carContext, stationJson, true), onScreenResultListener)
        else screenManager.push(StationDetailsScreen(carContext, stationJson, true))
    }
}

