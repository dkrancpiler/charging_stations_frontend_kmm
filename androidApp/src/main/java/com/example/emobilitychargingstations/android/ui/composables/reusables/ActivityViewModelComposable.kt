package com.example.emobilitychargingstations.android.ui.composables.reusables

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.emobilitychargingstations.android.ui.viewmodels.StationsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
inline fun getActivityViewModel(): StationsViewModel = getViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity )