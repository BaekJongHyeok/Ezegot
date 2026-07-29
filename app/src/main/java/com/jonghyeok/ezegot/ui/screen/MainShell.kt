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
 * 역 상세는 이 밖(바깥 NavHost)에 둔다. 최상위 목적지가 아니라 파고들어간
 * 화면이라, 하단바를 유지하면 어느 탭도 선택되지 않은 모순된 상태가 되고
 * 56dp를 지금 화면과 무관한 바에 계속 쓴다.
 */
private enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "홈", Icons.Default.Home),
    NEARBY("nearby", "근처", Icons.Default.Place),
    SEARCH("search", "검색", Icons.Default.Search),
    ALARM("alarm", "알림", Icons.Default.NotificationsNone)
}



@Composable
fun MainShell(onStationClick: (String, String) -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

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
                    onStationClick = onStationClick
                )
            }
            composable(MainTab.NEARBY.route) {
                NearbyMapScreen(onStationClick = onStationClick)
            }
            composable(MainTab.SEARCH.route) {
                SearchScreen(onStationClick = onStationClick)
            }
            composable(MainTab.ALARM.route) {
                AlarmScreen()
            }
        }
    }
}
