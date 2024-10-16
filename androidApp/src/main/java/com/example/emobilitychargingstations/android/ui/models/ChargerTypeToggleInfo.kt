package com.example.emobilitychargingstations.android.ui.models

import com.example.emobilitychargingstations.models.ChargerTypesEnum

data class ChargerTypeToggleInfo (
    val isChecked: Boolean,
    val chargerType: ChargerTypesEnum
)
