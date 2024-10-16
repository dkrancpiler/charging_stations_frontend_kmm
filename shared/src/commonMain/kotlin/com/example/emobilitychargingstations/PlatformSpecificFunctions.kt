package com.example.emobilitychargingstations

import com.example.emobilitychargingstations.models.StationsJsonModel

expect class PlatformSpecificFunctions() {
    fun getStationsFromJson(): StationsJsonModel?

    val isDebug: Boolean
}