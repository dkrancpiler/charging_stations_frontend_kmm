package com.example.emobilitychargingstations.android.ui.viewmodels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emobilitychargingstations.data.extensions.getLatitude
import com.example.emobilitychargingstations.data.extensions.getLongitude
import com.example.emobilitychargingstations.models.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

class StationMapViewModel(): ViewModel() {

    private val userMarkerOverlayName = "userOverlay"
    private val stationsMarkersOverlayName = "stationsOverlay"

    fun addMarkersToMap(mapView: MapView, userLocation: GeoPoint,
                        markerCluster: RadiusMarkerClusterer,
                        stations: List<Station>,
                        clusterIconBitmap: Bitmap,
                        stationsIconDrawable: Drawable?) {
        viewModelScope.launch(Dispatchers.Unconfined) {
            val previousMarkerOverlay = mapView.overlays.firstOrNull { it is FolderOverlay && it.name == stationsMarkersOverlayName }
            val didStationDataChange = previousMarkerOverlay?.findNumberOfMarkersOnMap() != stations.size
            if (didStationDataChange && stations.isNotEmpty()) {
                val folderOverlay = FolderOverlay()
                folderOverlay.name = stationsMarkersOverlayName
                markerCluster.apply {
                    setIcon(clusterIconBitmap)
                    items.removeAll(markerCluster.items.toSet())
                }
                stations.forEach {
                    val stationGeoPoint = GeoPoint(it.geometry.getLatitude(), it.geometry.getLongitude())
                    val stationMarker = Marker(mapView).apply {
                        position = stationGeoPoint
                        snippet = it.properties.street ?: it.properties.operator
                        icon = stationsIconDrawable
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    markerCluster.add(stationMarker)
                }

                if (markerCluster.items.isNotEmpty()) folderOverlay.add(markerCluster)
                mapView.overlays.apply {
                    remove(previousMarkerOverlay)
                    add(folderOverlay)
                }
            }
            addUserMarker(
                mapView,
                userLocation,
                didStationDataChange
            )
        }
    }

    private fun addUserMarker (mapView: MapView, userLocation: GeoPoint, shouldZoomIn: Boolean) {
        val userLocationAsGeoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
        val folderOverlay = FolderOverlay()
        val previousUserOverlay = mapView.overlays.firstOrNull { it is FolderOverlay && it.name == userMarkerOverlayName }
        val userMarker = Marker(mapView)
        userMarker.position = userLocationAsGeoPoint
        folderOverlay.apply {
            name = userMarkerOverlayName
            add(userMarker)
        }
        mapView.overlays.apply {
            remove(previousUserOverlay)
            add(folderOverlay)
        }
        if (shouldZoomIn) mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(listOf(userLocationAsGeoPoint)), false)
    }

    private fun Overlay.findNumberOfMarkersOnMap() = ((this as FolderOverlay?)?.items?.first() as RadiusMarkerClusterer?)?.items?.size

}