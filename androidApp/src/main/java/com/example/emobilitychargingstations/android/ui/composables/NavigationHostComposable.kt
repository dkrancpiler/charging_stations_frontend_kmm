package com.example.emobilitychargingstations.android.ui.composables

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.emobilitychargingstations.android.MainActivity.Companion.ARGUMENT_NAVIGATE_TO_NEXT
import com.example.emobilitychargingstations.android.MainActivity.Companion.NAVIGATE_TO_CHARGER_SELECTION
import com.example.emobilitychargingstations.android.MainActivity.Companion.NAVIGATE_TO_FILTER_SCREEN
import com.example.emobilitychargingstations.android.MainActivity.Companion.NAVIGATE_TO_MAP_SCREEN
import com.example.emobilitychargingstations.android.ui.composables.screens.FilteringOptionsComposable
import com.example.emobilitychargingstations.android.ui.composables.screens.MapViewComposable
import com.example.emobilitychargingstations.android.ui.composables.screens.StationsFilterComposable

@Composable
fun NavigationHostComposable(navController: NavHostController, startDestination: String) = NavHost(
    navController = navController,
    startDestination = startDestination
) {
    composable("$NAVIGATE_TO_CHARGER_SELECTION?$ARGUMENT_NAVIGATE_TO_NEXT={$ARGUMENT_NAVIGATE_TO_NEXT}", arguments = listOf(
        navArgument(ARGUMENT_NAVIGATE_TO_NEXT) {
            type = NavType.BoolType
            defaultValue = true
        }
    )) { backStackEntry ->
        val shouldNavigateToMap = backStackEntry.arguments?.getBoolean(ARGUMENT_NAVIGATE_TO_NEXT)
        FilteringOptionsComposable(proceedToNextScreen = {
            if (shouldNavigateToMap == true) navController.navigate(
                NAVIGATE_TO_MAP_SCREEN
            ) else navController.popBackStack()
        })
    }
    composable(NAVIGATE_TO_MAP_SCREEN) {
        MapViewComposable(proceedToSocketSelection = {
            navController.navigate(
                NAVIGATE_TO_FILTER_SCREEN
            )
        })
    }
    composable(NAVIGATE_TO_FILTER_SCREEN) {
        StationsFilterComposable(navigateToChargerType = {
            navController.navigate(
                "$NAVIGATE_TO_CHARGER_SELECTION?$ARGUMENT_NAVIGATE_TO_NEXT=false")
        })
    }
}