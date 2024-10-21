package com.example.emobilitychargingstations.android.ui.auto.screen

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import androidx.car.app.CarContext
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Template
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.ui.auto.BaseScreen
import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import com.example.emobilitychargingstations.android.ui.utilities.AUTO_POI_MAP_SCREEN_MARKER
import com.example.emobilitychargingstations.android.ui.utilities.buildRowWithText
import com.example.emobilitychargingstations.android.ui.utilities.getFavoritesAction
import com.example.emobilitychargingstations.android.ui.utilities.getString
import com.example.emobilitychargingstations.android.ui.utilities.getStringIdFromChargingType
import com.example.emobilitychargingstations.data.extensions.getChargingTypeFromMaxKW

class StationDetailsScreen(carContext: CarContext, private val stationsUiModel: StationsUiModel, private val showFavoritesAction: Boolean = false) : BaseScreen(carContext) {

    init {
        if (showFavoritesAction) {
            userInfoForFavorites.observe(this) {
                invalidate()
            }
            startObservingForFavorites()
        }
    }

    override fun onGetTemplate(): Template {
        val actionTitle = if (stationsUiModel.isNavigatingTo) getString(R.string.auto_station_details_stop_navigation) else getString(R.string.auto_station_details_start_navigation)
        val stationsPane = Pane.Builder().apply {
            addAction(
                Action.Builder().apply{
                    setTitle(actionTitle)
                    setBackgroundColor(CarColor.GREEN)
                    setOnClickListener(this@StationDetailsScreen::changeNavigation).build()
                }.build()
            )
            if (showFavoritesAction && userInfoForFavorites.value != null) addAction(getFavoritesAction(stationsUiModel, userInfoForFavorites.value!!, ::onFavoriteAdded, :: onFavoriteRemoved))
            addRow(
                buildRowWithText(
                    title = SpannableString(getString(R.string.auto_station_details_station_capacity)),
                    text = stationsUiModel.availableChargingStations.toString() + "/" + stationsUiModel.numberOfChargers?.toString()
                )
            )
            addRow(
                buildRowWithText(
                    title = SpannableString(getString(R.string.auto_station_details_operator)),
                    text = stationsUiModel.operator ?: "-"
                )
            )
            addRow(buildRowWithText(SpannableString(getString(R.string.auto_station_details_station_charger_type_list)), getSocketTypeString()))

        }.build()
        return PaneTemplate.Builder(stationsPane).setTitle(getPaneTitle())
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun getPaneTitle(): String {
        val chargingType = stationsUiModel.maximumPowerInKw.getChargingTypeFromMaxKW()
        val chargingTypeString = getString(chargingType.getStringIdFromChargingType())
        return "${stationsUiModel.street} - $chargingTypeString"
    }

    private fun getSocketTypeString() : String {
        val socketTypeString = if (stationsUiModel.listOfChargerTypes == null) getString(R.string.auto_station_details_unknown_charger) else {
            var resultingString = ""
            stationsUiModel.listOfChargerTypes.groupingBy { it }.eachCount().filterValues { it >= 1 }.keys.forEach {
                resultingString = if (resultingString.isEmpty()) it.name
                else "$resultingString, ${it.name}"
            }
            resultingString
        }
        return socketTypeString
    }

    private fun changeNavigation() {
        if (stationsUiModel.isNavigatingTo)  {
            stationsUiModel.isNavigatingTo = false
            setResult(stationsUiModel)
        }
        else {
            stationsUiModel.isNavigatingTo = true
            setResult(stationsUiModel)
            val latitude = stationsUiModel.latitude
            val longitude = stationsUiModel.longitude
            val name = getString(R.string.auto_station_details_navigating_to, stationsUiModel.street)
            val intent = Intent(CarContext.ACTION_NAVIGATE, Uri.parse("geo:0,0?q=${latitude},${longitude}(${name})"))
            carContext.startCarApp(intent)
        }
        screenManager.popTo(AUTO_POI_MAP_SCREEN_MARKER)
    }

}