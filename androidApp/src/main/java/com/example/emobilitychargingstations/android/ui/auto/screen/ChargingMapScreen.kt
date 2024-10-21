package com.example.emobilitychargingstations.android.ui.auto.screen

import android.graphics.Bitmap
import android.location.Location
import androidx.car.app.CarContext
import androidx.car.app.OnScreenResultListener
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.ItemList
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.comsystoreply.emobilitychargingstations.android.BuildConfig
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.mappers.toStationUIModel
import com.example.emobilitychargingstations.android.ui.auto.BaseScreen
import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import com.example.emobilitychargingstations.android.ui.utilities.AUTO_POI_MAP_SCREEN_MARKER
import com.example.emobilitychargingstations.android.ui.utilities.LocationRequestStarter
import com.example.emobilitychargingstations.android.ui.utilities.NAVIGATION_DISTANCE_VALUE_FOR_COMPLETION_IN_METERS
import com.example.emobilitychargingstations.android.ui.utilities.buildRowWithPlace
import com.example.emobilitychargingstations.android.ui.utilities.createCarIconFromBitmap
import com.example.emobilitychargingstations.android.ui.utilities.getDrawableAsBitmap
import com.example.emobilitychargingstations.android.ui.utilities.getPlaceWithMarker
import com.example.emobilitychargingstations.android.ui.utilities.getString
import com.example.emobilitychargingstations.android.ui.utilities.getTitleAsSpannableStringAndAddDistance
import com.example.emobilitychargingstations.android.ui.utilities.getTransparentCarColor
import com.example.emobilitychargingstations.models.UserLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChargingMapScreen(carContext: CarContext): BaseScreen(carContext), OnScreenResultListener {

    private var initialUserLocation: UserLocation? = null
    private var twoClosestStations: List<StationsUiModel> = listOf()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let { location ->
                if (checkIsLocationMockDebug(location)) {
                    if (twoClosestStations.any { it.isNavigatingTo }  && getDistanceValue(location, twoClosestStations.first()) < NAVIGATION_DISTANCE_VALUE_FOR_COMPLETION_IN_METERS) {
                        pushDestinationReachedScreen(twoClosestStations.first())
                    }
                    else {
                        val userLocation = UserLocation(location.latitude, location.longitude)
                        lifecycleScope.launch(Dispatchers.IO) {
                            userUseCase.setUserLocation(userLocation)
                            initialUserLocation = userLocation
                            invalidate()
                        }
                    }
                }
            }
        }
    }
    private val locationRequestStarter = LocationRequestStarter(carContext, locationCallback)

    init {
        startStationsRepeatingRequest()
        locationRequestStarter.startRequestingLocation()
        marker = AUTO_POI_MAP_SCREEN_MARKER
    }

    override fun onGetTemplate(): Template {
        val mapTemplateBuilder = fillMapTemplateBuilder()
        return mapTemplateBuilder.build()
    }

    override fun onScreenResult(result: Any?) {
        result?.let {stationResult ->
            val stationJson = stationResult as StationsUiModel
            if (stationJson.isNavigatingTo) {
                twoClosestStations = twoClosestStations.mapIndexed { index, station ->
                    if (station.id == stationJson.id) stationJson
                    else if (twoClosestStations.first().id != stationJson.id && index == 1) stationJson
                    else station
                }
            }
            else {
                twoClosestStations.forEach { it.isNavigatingTo = false }
            }
            invalidate()
        }
    }

    private fun startStationsRepeatingRequest () {
        stationsUseCase.startRepeatingRequest(
            2
        ).onEach { stationList ->
            if ((twoClosestStations.any { !it.isNavigatingTo } || twoClosestStations.isEmpty()) && stationList != twoClosestStations) {
                stationList.let { stations ->
                    twoClosestStations = stations.map { it.toStationUIModel() }
                    invalidate()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun pushDestinationReachedScreen(stationJson: StationsUiModel) {
        twoClosestStations = twoClosestStations.map {
            if (it.id == stationJson.id) it.isNavigatingTo = false
            it
        }
        invalidate()
        screenManager.push(NavigationCompleteScreen(carContext, stationJson))
    }

    private fun getDistanceValue(location: Location, stationsUiModel: StationsUiModel): Float {
        val distanceResult: FloatArray = floatArrayOf(0.0f)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            stationsUiModel.latitude,
            stationsUiModel.longitude,
            distanceResult
        )
        return distanceResult[0]
    }

    private fun checkIsLocationMockDebug(location: Location) : Boolean {
        return if (BuildConfig.DEBUG) location.isMock else true
//        return true
    }

    private fun fillMapTemplateBuilder(): PlaceListMapTemplate.Builder {
        val actionStrip = ActionStrip.Builder().addAction(openFavoritesListScreenAction()).build()
        val mapTemplateBuilder = PlaceListMapTemplate.Builder().setActionStrip(actionStrip)
        val carIcon = getDrawableAsBitmap(R.drawable.electric_car_icon)
        mapTemplateBuilder.setTitle(getString(R.string.auto_map_title))
        if (initialUserLocation == null || twoClosestStations.isEmpty())
            return mapTemplateBuilder.setLoading(true)

        mapTemplateBuilder.setAnchor(
            getPlaceWithMarker(
                initialUserLocation!!.latitude,
                initialUserLocation!!.longitude,
                CarColor.PRIMARY
            )
        )
        if (twoClosestStations.isEmpty())
            return mapTemplateBuilder.setItemList(
                ItemList.Builder().setNoItemsMessage(getString(R.string.auto_map_empty_list_message))
                    .build()
            )
        val navigatingToStation = twoClosestStations.firstOrNull { it.isNavigatingTo }
        val firstStationJson: StationsUiModel = navigatingToStation ?: twoClosestStations.first()
        val secondStation = if (navigatingToStation == null) twoClosestStations[1] else null
        val firstItemIcon = if (firstStationJson.isNavigatingTo) {
            mapTemplateBuilder.setTitle(getString(R.string.auto_map_navigation_title))
            getDrawableAsBitmap(R.drawable.navigating_to_icon)
        } else carIcon
        mapTemplateBuilder.apply {
            setItemList(
                ItemList.Builder().apply {
                    firstStationJson.let {
                        addItem(
                            getStationItem(firstItemIcon, if (it.isNavigatingTo) CarColor.GREEN else getTransparentCarColor(), it )
                        )
                    }
                    secondStation?.let {
                        addItem(
                            getStationItem(carIcon, getTransparentCarColor(), it)
                        )
                    }
                }.build()
            )
        }
        return mapTemplateBuilder
    }

    private fun getStationItem(itemIcon: Bitmap?, itemColor: CarColor, stationJson: StationsUiModel): Row {
        stationJson.let {
            return buildRowWithPlace(
                title = it.getTitleAsSpannableStringAndAddDistance(initialUserLocation!!),
                place = getPlaceWithMarker(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    carColor = itemColor,
                    markerIcon = itemIcon
                )
            ) {
                onItemClick(it)
            }
        }
    }

    private fun openFavoritesListScreenAction() = Action.Builder().apply {
        setIcon(createCarIconFromBitmap(
                getDrawableAsBitmap(R.drawable.favorites_star_icon)!!
            ))
        setOnClickListener(ParkedOnlyOnClickListener.create {
            screenManager.push(FavoritesListScreen(carContext, this@ChargingMapScreen))
        })
    }.build()

    private fun onItemClick(stationsUiModel: StationsUiModel) {
            screenManager.pushForResult(StationDetailsScreen(carContext, stationsUiModel = stationsUiModel), this)
    }

}
