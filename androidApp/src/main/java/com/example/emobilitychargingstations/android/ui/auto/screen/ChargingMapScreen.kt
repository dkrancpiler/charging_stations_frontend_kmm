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
import com.example.emobilitychargingstations.android.ui.utilities.getTwoStationsClosestToUser
import com.example.emobilitychargingstations.models.UserLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChargingMapScreen(carContext: CarContext) : BaseScreen(carContext), OnScreenResultListener {

    private var initialUserLocation: UserLocation? = null
    private var closestStationJsons: List<StationsUiModel> = listOf()
    private var initialStationJsonList: List<StationsUiModel>? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let {
                if (checkIsLocationMockDebug(it)) {
                    if (closestStationJsons.firstOrNull()?.isNavigatingTo == true && getDistanceValue(it, closestStationJsons.first()) < NAVIGATION_DISTANCE_VALUE_FOR_COMPLETION_IN_METERS) {
                        pushDestinationReachedScreen(closestStationJsons.first())
                    }
                    else {
                        val userLocation = UserLocation(it.latitude, it.longitude)
                        if (initialUserLocation == null) startStationsRepeatingRequest(userLocation)
                        initialUserLocation = userLocation
                        stationsUseCase.setTemporaryLocation(initialUserLocation)
                        filterStations()
                        invalidate()
                    }
                }
            }
        }
    }

    init {
        LocationRequestStarter(carContext, locationCallback)
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
                closestStationJsons = listOf(stationJson)
            }
            else {
                closestStationJsons.forEach { it.isNavigatingTo = false }
                filterStations()
            }
            invalidate()
        }
    }

    private fun startStationsRepeatingRequest (userLocation: UserLocation) {
        stationsUseCase.startRepeatingRequest(
            userLocation
        ).onEach { stationList ->
            if (closestStationJsons.firstOrNull()?.isNavigatingTo != true && stationList != initialStationJsonList) {
                initialStationJsonList = stationList?.map { it.toStationUIModel() }
                filterStations()
                invalidate()
            }
        }.launchIn(lifecycleScope)
    }

    private fun pushDestinationReachedScreen(stationJson: StationsUiModel) {
        closestStationJsons.firstOrNull()?.isNavigatingTo = false
        screenManager.push(NavigationCompleteScreen(carContext, stationJson))
        filterStations()
        invalidate()
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

    private fun filterStations() {
        if (closestStationJsons.firstOrNull()?.isNavigatingTo != true) {
            closestStationJsons = if (initialStationJsonList.isNullOrEmpty() || initialUserLocation == null) listOf()
            else initialStationJsonList!!.getTwoStationsClosestToUser(initialUserLocation!!.latitude, initialUserLocation!!.longitude)
        }
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
        if (initialUserLocation == null || initialStationJsonList == null)
            return mapTemplateBuilder.setLoading(true)

        mapTemplateBuilder.setAnchor(
            getPlaceWithMarker(
                initialUserLocation!!.latitude,
                initialUserLocation!!.longitude,
                CarColor.PRIMARY
            )
        )
        if (closestStationJsons.isEmpty())
            return mapTemplateBuilder.setItemList(
                ItemList.Builder().setNoItemsMessage(getString(R.string.auto_map_empty_list_message))
                    .build()
            )

        val firstStationJson: StationsUiModel = closestStationJsons.first()
        val secondStation = if (closestStationJsons.size > 1) closestStationJsons[1] else null
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
