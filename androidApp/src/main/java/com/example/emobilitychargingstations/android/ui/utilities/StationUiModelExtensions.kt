package com.example.emobilitychargingstations.android.ui.utilities

import com.example.emobilitychargingstations.android.ui.models.StationsUiModel
import kotlin.math.abs

fun List<StationsUiModel>.getTwoStationsClosestToUser(userLat: Double, userLng: Double): List<StationsUiModel> {
    var closestTotalDifference = 180.00
    val resultList = mutableListOf<StationsUiModel>()
    if (this.isNotEmpty()) {
        var currentClosestStation = this.getOneStationClosestToUser(userLat, userLng)
        if (this.size == 1) resultList.add(currentClosestStation) else {
            var secondClosestStation = currentClosestStation
            this.forEach { station ->
                val latDiff = abs(userLat - station.latitude)
                val lngDiff = abs(userLng - station.longitude)
                val totalDiff = latDiff + lngDiff
                if (totalDiff < closestTotalDifference) {
                    closestTotalDifference = totalDiff
                    secondClosestStation = currentClosestStation
                    currentClosestStation = station
                }
            }
            resultList.add(currentClosestStation)
            resultList.add(secondClosestStation)
        }
    }

    return resultList
}

fun List<StationsUiModel>.getOneStationClosestToUser(userLat: Double, userLng: Double): StationsUiModel {
    var closestTotalDifference = 180.00
    var currentClosestStation = get(0)
    this.forEach { station ->
        val latDiff = abs(userLat - station.latitude)
        val lngDiff = abs(userLng - station.longitude)
        val totalDiff = latDiff + lngDiff
        if (totalDiff < closestTotalDifference) {
            closestTotalDifference = totalDiff
            currentClosestStation = station
        }
    }
    return currentClosestStation
}