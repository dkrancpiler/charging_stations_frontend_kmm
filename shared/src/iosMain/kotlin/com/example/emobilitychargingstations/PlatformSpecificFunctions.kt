package com.example.emobilitychargingstations

import com.example.emobilitychargingstations.models.StationJson
import com.example.emobilitychargingstations.models.StationsJsonModel
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.darwin.NSObject
import platform.darwin.NSObjectMeta
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
actual class PlatformSpecificFunctions {

    actual fun getStationsFromJson(): StationsJsonModel? {
        val bundle = NSBundle.bundleForClass(BundleMarker)
        val munichStationsPath = bundle.pathForResource("munichStations", "json")
        val regensburgStationsPath = bundle.pathForResource("regensburgStations", "json")
        val combinedStationJsons = mutableListOf<StationJson>()
        memScoped {
            munichStationsPath?.let {path ->
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                val stationsString = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, errorPtr.ptr)
                val munichStationsJsonModel = Json.decodeFromString<StationsJsonModel>(stationsString!!)
                munichStationsJsonModel.features?.let {
                    combinedStationJsons.addAll(it)
                }
            }
            regensburgStationsPath?.let {path ->
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                val stationsString = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, errorPtr.ptr)
                val regensburgStationsJsonModel = Json.decodeFromString<StationsJsonModel>(stationsString!!)
                regensburgStationsJsonModel.features?.let {
                    combinedStationJsons.addAll(it)
                }
            }
            if (combinedStationJsons.isNotEmpty()) {
                combinedStationJsons.filter { it.properties.street != null }
            }
        }
        return StationsJsonModel(type = "", features = combinedStationJsons)
    }

    actual val isDebug = Platform.isDebugBinary

    private class BundleMarker: NSObject() {
        companion object : NSObjectMeta()
    }
}