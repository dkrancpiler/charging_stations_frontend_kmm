package com.example.emobilitychargingstations.models

import kotlinx.serialization.Transient

@kotlinx.serialization.Serializable
data class StationJson(
    val id: Long,
    val type: String?,
    val properties: StationProperties,
    val geometry: StationGeoData,
    @Transient
    var isNavigatingTo: Boolean = false
)
