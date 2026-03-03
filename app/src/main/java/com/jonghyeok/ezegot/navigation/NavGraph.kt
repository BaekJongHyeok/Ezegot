package com.jonghyeok.ezegot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jonghyeok.ezegot.ui.screen.HomeScreen
import com.jonghyeok.ezegot.ui.screen.MapScreen
import com.jonghyeok.ezegot.ui.screen.SearchScreen
import com.jonghyeok.ezegot.ui.screen.SplashScreen
import com.jonghyeok.ezegot.ui.screen.StationScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Search : Screen("search")
    object Map : Screen("map")
    object Station : Screen("station/{stationName}/{lineNumber}") {
        fun createRoute(name: String, line: String) = "station/$name/$line"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onMapClick = { navController.navigate(Screen.Map.route) },
                onStationClick = { name, line ->
                    navController.navigate(Screen.Station.createRoute(name, line))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onStationClick = { name, line ->
                    navController.navigate(Screen.Station.createRoute(name, line))
                }
            )
        }

        composable(
            route = Screen.Station.route,
            arguments = listOf(
                navArgument("stationName") { type = NavType.StringType },
                navArgument("lineNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stationName = backStackEntry.arguments?.getString("stationName") ?: ""
            val lineNumber = backStackEntry.arguments?.getString("lineNumber") ?: ""
            StationScreen(
                stationName = stationName,
                lineNumber = lineNumber,
                onBack = { navController.popBackStack() },
                onStationClick = { name, line ->
                    navController.navigate(Screen.Station.createRoute(name, line)) {
                        popUpTo(Screen.Station.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(onBack = { navController.popBackStack() })
        }
    }
}
