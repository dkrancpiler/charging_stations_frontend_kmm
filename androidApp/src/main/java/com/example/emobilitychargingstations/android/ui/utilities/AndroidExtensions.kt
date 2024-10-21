package com.example.emobilitychargingstations.android.ui.utilities

import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import com.example.emobilitychargingstations.models.ChargingTypeEnum

fun ChargerTypesEnum.getStringIdFromChargerType(): Int = when (this) {
    ChargerTypesEnum.AC_TYPE_1 -> R.string.android_charger_type_ac_1
    ChargerTypesEnum.AC_TYPE_2 -> R.string.android_charger_type_ac_2
    ChargerTypesEnum.DC_EU -> R.string.android_charger_type_dc_eu
    ChargerTypesEnum.DC_CHADEMO -> R.string.android_charger_type_dc_chademo
    ChargerTypesEnum.TESLA -> R.string.android_charger_type_tesla
    ChargerTypesEnum.ALL -> R.string.android_charger_type_all
    else -> R.string.android_charger_type_unknown
}

fun ChargingTypeEnum?.getStringIdFromChargingType(): Int = when (this) {
    ChargingTypeEnum.NORMAL -> { R.string.android_charging_type_normal}
    ChargingTypeEnum.FAST -> {R.string.android_charging_type_fast}
    ChargingTypeEnum.RAPID -> {R.string.android_charging_type_rapid}
    ChargingTypeEnum.ANY -> {R.string.android_charging_type_all}
    null -> {R.string.android_charging_type_unknown}
    
}