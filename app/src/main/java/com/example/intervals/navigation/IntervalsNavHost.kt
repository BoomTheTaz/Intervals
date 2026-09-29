package com.example.intervals.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.intervals.ui.presetedit.PresetEditScreen
import com.example.intervals.ui.presetlist.PresetListScreen
import com.example.intervals.ui.run.RunScreen

private const val ROUTE_LIST = "list"
private const val ROUTE_EDIT = "edit/{presetId}"
private const val ROUTE_RUN = "run/{presetId}"

fun editRoute(presetId: Long) = "edit/$presetId"
fun runRoute(presetId: Long) = "run/$presetId"

@Composable
fun IntervalsNavHost(startAtRun: Long?)
{
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startAtRun?.let { runRoute(it) } ?: ROUTE_LIST
    ) {
        composable(ROUTE_LIST) {
            PresetListScreen(
                onAddPreset = { navController.navigate(editRoute(0L)) },
                onEditPreset = { id -> navController.navigate(editRoute(id)) },
                onRunPreset = { id -> navController.navigate(runRoute(id)) }
            )
        }
        composable(
            ROUTE_EDIT,
            arguments = listOf(navArgument("presetId") { type = NavType.LongType })
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getLong("presetId") ?: 0L
            PresetEditScreen(
                presetId = presetId,
                onDone = { navController.popBackStack() }
            )
        }
        composable(
            ROUTE_RUN,
            arguments = listOf(navArgument("presetId") { type = NavType.LongType })
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getLong("presetId") ?: 0L
            RunScreen(
                presetId = presetId,
                onExit = {
                    navController.navigate(ROUTE_LIST) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
