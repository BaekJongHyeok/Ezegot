package com.jonghyeok.ezegot.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jonghyeok.ezegot.ui.screen.station.StationScreen

/**
 * 하단 탭 4개를 가진 최상위 껍데기.
 *
 * 역 상세도 이 안에 둔다. 목업이 역 상세에서도 하단바를 유지하므로,
 * 탭 전환과 상세 진입을 같은 NavHost가 다룬다.
 * 상세에 들어가면 어느 탭도 선택 상태가 아니다 — 탭 중 하나를 억지로
 * 켜두면 지금 보는 화면과 어긋난다.
 */
private enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "홈", Icons.Default.Home),
    NEARBY("nearby", "근처", Icons.Default.Place),
    SEARCH("search", "검색", Icons.Default.Search),
    ALARM("alarm", "알림", Icons.Default.NotificationsNone)
}

private const val STATION_ROUTE = "station/{stationName}/{lineNumber}"

private fun stationRoute(name: String, line: String) = "station/$name/$line"

@Composable
fun MainShell() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val openStation: (String, String) -> Unit = { name, line ->
        navController.navigate(stationRoute(name, line))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    // 탭 사이를 오갈 때 백스택이 쌓이지 않게 한다
                                    popUpTo(MainTab.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.tertiary,
                                unselectedTextColor = MaterialTheme.colorScheme.tertiary,
                                // 알약 표시자를 쓰면 선택 상태가 두 번 표현된다
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            // 크로스페이드를 끄면 전환 중 두 화면이 겹쳐 비치지 않는다
            enterTransition = { androidx.compose.animation.EnterTransition.None },
            exitTransition = { androidx.compose.animation.ExitTransition.None },
            popEnterTransition = { androidx.compose.animation.EnterTransition.None },
            popExitTransition = { androidx.compose.animation.ExitTransition.None }
        ) {
            composable(MainTab.HOME.route) {
                HomeScreen(
                    onSearchClick = { navController.navigate(MainTab.SEARCH.route) },
                    onMapClick = { navController.navigate(MainTab.NEARBY.route) },
                    onStationClick = openStation
                )
            }
            composable(MainTab.NEARBY.route) {
                NearbyMapScreen(onStationClick = openStation)
            }
            composable(MainTab.SEARCH.route) {
                SearchScreen(onStationClick = openStation)
            }
            composable(MainTab.ALARM.route) {
                AlarmScreen()
            }
            composable(
                route = STATION_ROUTE,
                arguments = listOf(
                    navArgument("stationName") { type = NavType.StringType },
                    navArgument("lineNumber") { type = NavType.StringType }
                )
            ) { entry ->
                StationScreen(
                    stationName = entry.arguments?.getString("stationName") ?: "",
                    lineNumber = entry.arguments?.getString("lineNumber") ?: "",
                    onBack = { navController.popBackStack() },
                    onStationClick = { name, line ->
                        navController.navigate(stationRoute(name, line)) {
                            popUpTo(STATION_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
