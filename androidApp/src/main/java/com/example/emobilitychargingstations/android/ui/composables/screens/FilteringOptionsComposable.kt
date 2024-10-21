package com.example.emobilitychargingstations.android.ui.composables.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.comsystoreply.emobilitychargingstations.android.R
import com.example.emobilitychargingstations.android.ui.models.ChargerTypeToggleInfo
import com.example.emobilitychargingstations.android.ui.utilities.getStringIdFromChargerType
import com.example.emobilitychargingstations.android.ui.viewmodels.UserViewModel
import com.example.emobilitychargingstations.models.ChargerTypesEnum
import org.koin.androidx.compose.koinViewModel

@Composable
fun FilteringOptionsComposable(proceedToNextScreen: () -> Unit, userViewModel: UserViewModel = koinViewModel()) {
    val listOfButtonsInfo = mutableListOf<ChargerTypeToggleInfo>()
    val chargerType = userViewModel.getUserInfo()?.filterProperties?.chargerType
    ChargerTypesEnum.entries.forEach {
        val chargerTypeNotSet = chargerType == null && it == ChargerTypesEnum.ALL
        if (it != ChargerTypesEnum.UNKNOWN) listOfButtonsInfo.add(ChargerTypeToggleInfo(chargerType == it || chargerTypeNotSet , it))
    }
    val socketTypeButtons = remember {
        val listOfButtonsAsStateList = mutableStateListOf<ChargerTypeToggleInfo>()
        listOfButtonsAsStateList.addAll(listOfButtonsInfo)
        listOfButtonsAsStateList
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ChargerTypeButtonsComposable(socketTypeButtons)
            Row {
                Button(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    enabled = socketTypeButtons.any { toggleInfo -> toggleInfo.isChecked },
                    onClick = {
                        socketTypeButtons.firstOrNull { toggleInfo -> toggleInfo.isChecked }?.chargerType?.let {
                            userViewModel.setChargerType(it)
                        }
                        proceedToNextScreen()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}

@Composable
fun ChargerTypeButtonsComposable(socketTypeButtons: SnapshotStateList<ChargerTypeToggleInfo>) {
    Text(stringResource(R.string.android_charger_type_selection))
    Column(modifier = Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.Start) {
        socketTypeButtons.forEach { toggleInfo ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                RadioButton(selected = toggleInfo.isChecked, onClick = {
                    socketTypeButtons.replaceAll {
                        it.copy(isChecked = it.chargerType == toggleInfo.chargerType)
                    }
                })
                Text(text = stringResource(id = toggleInfo.chargerType.getStringIdFromChargerType()))
            }
        }
    }
}

