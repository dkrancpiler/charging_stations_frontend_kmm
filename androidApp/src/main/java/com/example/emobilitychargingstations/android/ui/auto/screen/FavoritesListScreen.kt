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
import com.example.emobilitychargingstations.android.ui.utilities.getMessageTemplateBuilderWithTitle
import com.example.emobilitychargingstations.android.ui.utilities.getString

class FavoritesListScreen(carContext: CarContext, private val onScreenResultListener: OnScreenResultListener? = null): BaseScreen(carContext) {

    override fun onGetTemplate(): Template {
        val userInfo = userUseCase.getUserInfo()
        val templateTitle = getString(R.string.auto_favorites_list_title)
        val templateForDisplay: Template =
            if (userInfo?.favoriteStationJsons == null || userInfo.favoriteStationJsons.isNullOrEmpty())
                getMessageTemplateBuilderWithTitle(templateTitle, getString(R.string.auto_favorites_list_empty_message)).build()
            else {
                val listTemplateBuilder = ListTemplate.Builder()
                listTemplateBuilder.apply {
                    setHeaderAction(Action.BACK)
                    setTitle(templateTitle)
                    setSingleList(ItemList.Builder().apply {
                        userInfo.favoriteStationJsons!!.forEach { favorite ->
                            addItem(
                                buildClickableRowWithTextAndIcon(
                                    title = SpannableString(favorite.street),
                                    text = favorite.operator ?: "",
                                    carIcon = getDrawableAsBitmap(
                                        R.drawable.electric_car_icon_white
                                    )!!
                                ) {
                                    onItemClick(favorite.toStationUIModel())
                                }
                            )
                        }
                    }.build())
            }
            listTemplateBuilder.build()
        }
        return templateForDisplay
    }

    private fun onItemClick(stationJson: StationsUiModel) {
        if (onScreenResultListener != null) screenManager.pushForResult(StationDetailsScreen(carContext, stationJson, true), onScreenResultListener)
        else screenManager.push(StationDetailsScreen(carContext, stationJson, true))
    }
}

