package com.jonghyeok.ezegot.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jonghyeok.ezegot.ui.screen.MainShell
import com.jonghyeok.ezegot.ui.screen.SplashScreen
import com.jonghyeok.ezegot.ui.screen.station.StationScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")

    /** 하단 탭 4개를 가진 최상위 화면 */
    object Main : Screen("main")

    object Station : Screen("station/{stationName}/{lineNumber}") {
        fun createRoute(name: String, line: String) = "station/$name/$line"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        // 기본 크로스페이드를 끈다.
        // 페이드 중에는 이전 화면과 새 화면이 동시에 반투명으로 그려져,
        // 홈의 즐겨찾기 카드 내용이 검색 화면 위에 유령처럼 겹쳐 보였다.
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 역 상세는 이 바깥에 둔다. 최상위 목적지가 아니라 파고들어간 화면이라
        // 하단바를 유지하면 지금 화면과 무관한 바가 계속 자리를 차지한다.
        composable(Screen.Main.route) {
            MainShell(
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
    }
}
