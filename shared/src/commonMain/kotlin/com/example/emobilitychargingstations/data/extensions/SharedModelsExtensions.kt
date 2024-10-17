package com.example.emobilitychargingstations.data.extensions

import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.ChargingTypeEnum
import com.example.emobilitychargingstations.models.StationDataModel

fun List<StationDataModel>.filterByChargerType(chargerType: ChargerTypesEnum?): List<StationDataModel> {
    return if (chargerType == ChargerTypesEnum.ALL) this else {
        this.filter { station ->
            station.listOfChargerTypes?.any { stationChargerType ->
                if (stationChargerType == ChargerTypesEnum.TESLA) {
                    station.operator.lowercase().contains("tesla") || stationChargerType == chargerType
                }
                else stationChargerType == chargerType
//                        || stationChargerType == ChargerTypesEnum.UNKNOWN
            } ?: false
        }
    }
//    val keywords = mutableListOf<String>()
//    when (chargerType) {
//        ChargerTypesEnum.AC_TYPE_2 -> {
//            keywords.add("typ 2")
//            keywords.add("typ2")
//        }
//        ChargerTypesEnum.AC_TYPE_1 -> {
//            keywords.add("Typ1")
//            keywords.add("Typ 1")
//        }
//        ChargerTypesEnum.DC_EU -> {
//            keywords.add("DC Kupplung Combo")
//        }
//        ChargerTypesEnum.DC_CHADEMO -> {
//            keywords.add("chademo")
//        }
//        ChargerTypesEnum.TESLA -> {
//            keywords.add("tesla")
//        }
//        ChargerTypesEnum.ALL -> {
//        }
//        else -> {}
//    }
//    return if (keywords.isEmpty()) this
//    else this.filter { station ->
//        var result = false
////        if (station.properties.socket_type_list == null) result = true
////        else {
//            keywords.forEach { keyword ->
//                if (keyword == "tesla" && station.operator?.contains(keyword, true) == true) result = true
//                else station.listOfChargerTypes?.forEach {
//                    if (it.contains(keyword, true)) result = true
//                }
//            }
////        }
//        result
//    }
}

fun List<StationDataModel>.filterByChargingType(chargingTypeEnum: ChargingTypeEnum): List<StationDataModel> {
    return filter { it.checkIsStationOfChargingType(chargingTypeEnum) }
}

fun StationDataModel.randomizeAvailability() {
    this.availableChargingStations = (0..(this.numberOfChargers ?: 1)).random()
}

fun StationDataModel.checkIsStationOfChargingType(chargingTypeEnum: ChargingTypeEnum): Boolean {
    var result = true
    this.maximumPowerInKw?.let {
        result = when (chargingTypeEnum) {
            ChargingTypeEnum.NORMAL -> {
                it <= 6.99
            }
            ChargingTypeEnum.FAST -> {
               it in 7.0 .. 42.99
            }
            ChargingTypeEnum.RAPID -> {
                it >= 43
            }
            ChargingTypeEnum.ANY -> {
                true
            }
        }
    }
    return result
}

fun ChargingTypeEnum?.getPowerRangeFromChargingType(): Pair<Float, Float> {
        return when (this) {
            ChargingTypeEnum.NORMAL -> {
                Pair(0f, 6.99f)
            }
            ChargingTypeEnum.FAST -> {
                Pair(7f, 42.99f)
            }
            ChargingTypeEnum.RAPID -> {
                Pair(43f, 2000f)
            }
            else -> {
                Pair(0f, 2000f)
            }
        }
}