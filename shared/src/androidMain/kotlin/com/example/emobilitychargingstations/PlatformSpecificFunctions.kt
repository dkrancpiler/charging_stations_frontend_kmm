package com.example.emobilitychargingstations

import com.comsystoreply.emobilitychargingstations.BuildConfig
import com.example.emobilitychargingstations.models.StationJson
import com.example.emobilitychargingstations.models.StationsJsonModel
import kotlinx.serialization.json.Json

actual class PlatformSpecificFunctions {
    actual fun getStationsFromJson(): StationsJsonModel? {
        val munichStationsJson = javaClass.classLoader!!.getResourceAsStream("munichStations.json")!!.bufferedReader().use { it.readText() }
        val regensburgStationsJson = javaClass.classLoader!!.getResourceAsStream("regensburgStations.json")!!.bufferedReader().use { it.readText() }
        val munichStationsJsonModelFromJson = Json.decodeFromString<StationsJsonModel>(munichStationsJson)
        val regensburgStationsJsonModelFromJson = Json.decodeFromString<StationsJsonModel>(regensburgStationsJson)
        val combinedStationJsons = mutableListOf<StationJson>()
        munichStationsJsonModelFromJson.features?.let { combinedStationJsons.addAll(it) }
        regensburgStationsJsonModelFromJson.features?.let { combinedStationJsons.addAll(it) }
        return StationsJsonModel(type = "", features = combinedStationJsons.filter { it.properties.street != null })
    }

    actual val isDebug = BuildConfig.DEBUG
}