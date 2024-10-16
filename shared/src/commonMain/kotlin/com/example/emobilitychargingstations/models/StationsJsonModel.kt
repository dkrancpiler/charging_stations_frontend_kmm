package com.example.emobilitychargingstations.models

@kotlinx.serialization.Serializable
data class StationsJsonModel(
    val type: String,
    val features: List<StationJson>?
)

