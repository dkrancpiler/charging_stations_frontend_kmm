package com.example.emobilitychargingstations.android.ui.composables.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.emobilitychargingstations.android.ui.composables.reusables.DragDropColumn
import com.example.emobilitychargingstations.android.ui.viewmodels.UserViewModel
import com.example.emobilitychargingstations.models.FavoriteStationDataModel
import com.example.emobilitychargingstations.models.UserInfo
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFavoritesComposable(userViewModel: UserViewModel = koinViewModel()) {
    userViewModel.startObservingForFavorites()
    val favorites = userViewModel.userInfo.value?.favoriteStationsList
    fun swapItems(from: Int, to: Int) {
        favorites?.let {
            val newFavoriteList = it.toMutableList()
            val fromItem = it[from].copy()
            val toItem = it[to].copy()
            newFavoriteList[from] = toItem
            newFavoriteList[to] = fromItem
            userViewModel.updateFavorites(newFavoriteList)
        }
    }
    Box {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .background(Color.White)) {
            if (favorites != null) DragDropColumn (
                items = favorites,
                onSwap = ::swapItems,
            ) { item ->
                FavoriteItem(item)
            } else Text(text = "noFavs")
        }
    }

}

@Composable
private fun FavoriteItem(favoriteStation: FavoriteStationDataModel) {
    Column(Modifier.background(Color.White).padding(12.dp)) {
        Text(text = favoriteStation.nickname ?: favoriteStation.station.street)
    }
}
