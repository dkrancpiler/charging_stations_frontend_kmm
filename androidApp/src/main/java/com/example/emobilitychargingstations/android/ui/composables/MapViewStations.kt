package com.example.emobilitychargingstations.android.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.StationsViewModel
import com.example.emobilitychargingstations.data.extensions.filterByChargerType
import com.example.emobilitychargingstations.data.extensions.getStationsClosestToUserLocation
import com.example.emobilitychargingstations.models.Stations
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.utils.BonusPackHelper
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
@Composable
fun ComposableMapView(proceedToSocketSelection: () -> Unit, stationsViewModel: StationsViewModel) {
    val testStations = stationsViewModel._stationsData.observeAsState()
    val userLocation = stationsViewModel._userLocation.observeAsState()
    stationsViewModel.getTestStations(LocalContext.current)
    val mapViewState = mapViewWithLifecycle(testStations.value, userLocation.value, stationsViewModel.getUserInfo()?.chargerType)
    ConstraintLayout {
        val (map, button) = createRefs()
        AndroidView({ mapViewState },
            Modifier
                .fillMaxSize()
                .constrainAs(map) {}) {}
        TextButton(modifier = Modifier.constrainAs(button){
                                                          top.linkTo(map.top)
            end.linkTo(map.end)
        }, onClick = { proceedToSocketSelection() }) {
            Text("Change socket", color = Color.Black)
        }
    }
}

@Composable
fun mapViewWithLifecycle(stations: Stations?, userLocation: GeoPoint?, chargerType: String? = null): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.layout_map
            clipToOutline = true
        }
    }
    var userMarker: Marker? = null
    mapView.minZoomLevel = 9.00
    mapView.maxZoomLevel = 16.00
    mapView.isHorizontalMapRepetitionEnabled = false
    mapView.isVerticalMapRepetitionEnabled = false
    val folderOverlay = FolderOverlay()
    val markerCluster = RadiusMarkerClusterer(context)
    markerCluster.setIcon(BonusPackHelper.getBitmapFromVectorDrawable(context, org.osmdroid.bonuspack.R.drawable.marker_cluster))
    if (stations != null && userLocation != null) {
        markerCluster.items.removeAll(markerCluster.items.toSet())
        stations.getStationsClosestToUserLocation(userLocation.latitude, userLocation.longitude).filterByChargerType(chargerType).forEach {
                val stationGeoPoint = GeoPoint(it.geometry.coordinates[1], it.geometry.coordinates[0])
                val stationMarker = Marker(mapView)
                stationMarker.position = stationGeoPoint
                stationMarker.snippet = it.properties.street ?: it.properties.operator
                stationMarker.icon = context.getDrawable(R.drawable.electric_car_icon)
                stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                markerCluster.add(stationMarker)
        }
        if (markerCluster.items.size > 0) folderOverlay.add(markerCluster)
    }
    if (userMarker != null) folderOverlay.remove(userMarker)
    userMarker = Marker(mapView)
    userMarker.position = userLocation
    folderOverlay.add(userMarker)
    mapView.overlays.clear()
    mapView.overlays.add(folderOverlay)
    mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(listOf(userLocation)), false)
    val lifecycleObserver = rememberMapObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycle.removeObserver(lifecycleObserver) }
    }
    return mapView
}

@Composable
fun rememberMapObserver(mapView: MapView): LifecycleEventObserver = remember(mapView) {
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            else -> {}
        }
    }
}