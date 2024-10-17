package com.example.emobilitychargingstations.android.ui.composables.screens

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.ui.composables.reusables.ProgressBarComposable
import com.example.emobilitychargingstations.android.ui.composables.reusables.getActivityViewModel
import com.example.emobilitychargingstations.android.ui.viewmodels.StationMapViewModel
import com.example.emobilitychargingstations.android.ui.viewmodels.StationsViewModel
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.utils.BonusPackHelper
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MapViewComposable(proceedToSocketSelection: () -> Unit,
                      stationsViewModel: StationsViewModel = getActivityViewModel(),
                      stationMapViewModel: StationMapViewModel = koinViewModel()) {

    val clusterIcon = BonusPackHelper.getBitmapFromVectorDrawable(LocalContext.current, org.osmdroid.bonuspack.R.drawable.marker_cluster)
    val stationIcon = AppCompatResources.getDrawable(LocalContext.current,R.drawable.electric_car_icon)

    val testStations = stationsViewModel.stationsData.value
    val userLocation = stationsViewModel.userLocation.value
    val mapView = mapViewWithLifecycle()
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (map, button, progressBar) = createRefs()
        if (testStations != null && userLocation != null) {
            AndroidView({ mapView },
                Modifier
                    .fillMaxSize()
                    .constrainAs(map) {}) {}
            TextButton(modifier = Modifier.constrainAs(button) {
                top.linkTo(map.top)
                end.linkTo(map.end)
            }, onClick = { proceedToSocketSelection() }) {
                Text(stringResource(R.string.android_map_edit_filters), color = Color.Black)
            }
        } else ProgressBarComposable(modifier = Modifier.constrainAs(progressBar) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        })
    }
    if (testStations != null && userLocation != null) {
        val userLocationAsGeoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
        val newMarkerCluster = RadiusMarkerClusterer(LocalContext.current)
        stationMapViewModel.addMarkersToMap(mapView, userLocationAsGeoPoint, newMarkerCluster, testStations, clusterIcon, stationIcon)
    }
}

@Composable
private fun mapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.layout_map
            clipToOutline = true
        }
    }
    mapView.apply {
        minZoomLevel = 10.00
        maxZoomLevel = 15.00
        isHorizontalMapRepetitionEnabled = false
        isVerticalMapRepetitionEnabled = false
    }
    val lifecycleObserver = rememberMapObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycle.removeObserver(lifecycleObserver) }
    }
    return mapView
}

@Composable
private fun rememberMapObserver(mapView: MapView): LifecycleEventObserver = remember(mapView) {
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> {
                mapView.onPause()
            }
            else -> {}
        }
    }
}