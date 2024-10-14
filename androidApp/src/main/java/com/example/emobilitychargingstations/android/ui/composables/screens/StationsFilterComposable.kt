package com.example.emobilitychargingstations.android.ui.composables.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.models.ChargingTypeToggleInfo
import com.example.emobilitychargingstations.android.ui.utilities.getStringIdFromChargingType
import com.example.emobilitychargingstations.android.ui.viewmodels.UserViewModel
import com.example.emobilitychargingstations.models.ChargingTypeEnum
import org.koin.androidx.compose.koinViewModel


@Composable
fun StationsFilterComposable(navigateToChargerType: () -> Unit) {
    Box {
        val context = LocalContext.current
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .background(Color.White)) {
            ChargingTypeFilterComposable()
            Button(onClick = { navigateToChargerType() }) {
                Text(stringResource(id = R.string.android_change_charging_type))
            }
            Button(onClick = { Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show() }) {
                Text(text = stringResource(id = R.string.android_edit_favorites))
            }
        }
    }
}

@Composable
fun ChargingTypeFilterComposable(userViewModel: UserViewModel = koinViewModel()) {
    val userInfo = userViewModel.getUserInfo()
    val listOfButtonsInfo = mutableListOf<ChargingTypeToggleInfo>()
    ChargingTypeEnum.entries.forEach {
        listOfButtonsInfo.add(ChargingTypeToggleInfo((userInfo?.filterProperties?.chargingType ?: ChargingTypeEnum.ANY) == it, it))
    }
    val socketTypeButtons = remember {
        val mutableStateList = mutableStateListOf<ChargingTypeToggleInfo>()
        mutableStateList.addAll(listOfButtonsInfo)
        mutableStateList
    }
    Text(stringResource(R.string.android_charging_type_selection))
    ChargingTypeButtonsComposable(socketTypeButtons = socketTypeButtons, userViewModel::setChargingType)
}

@Composable
fun ChargingTypeButtonsComposable(socketTypeButtons: SnapshotStateList<ChargingTypeToggleInfo>, changeChargingType: (ChargingTypeEnum) -> Unit) {
    socketTypeButtons.forEach { toggleInfo ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
            RadioButton(selected = toggleInfo.isChecked, onClick = {
                changeChargingType(toggleInfo.chargingType)
                socketTypeButtons.replaceAll {
                    it.copy(isChecked = it.chargingType == toggleInfo.chargingType)
                }
            })
            Text(stringResource(toggleInfo.chargingType.getStringIdFromChargingType()))
        }
    }
}