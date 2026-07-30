package com.jonghyeok.ezegot.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.sp
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
/**
 * 하단바 높이. Material3 기본은 80dp인데 4탭짜리 얕은 구조에 과하다.
 * 항목의 터치 영역이 바 높이를 그대로 채우므로 이 값이 곧 세로 터치 크기다.
 * 48dp 아래로 내리면 최소 터치 크기가 깨진다.
 */
private val BOTTOM_BAR_HEIGHT = 64.dp

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
            // 시스템 내비게이션 여백은 바 바깥에서 준다. NavigationBar에 그대로 두면
            // 높이를 고정했을 때 인셋이 콘텐츠 높이를 먹어 아이콘·라벨이 눌린다.
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                NavigationBar(
                    // 기본 80dp는 4탭짜리 얕은 앱에 과하다. 터치 영역은 항목이
                    // 바 높이를 꽉 채우므로 64dp에서도 최소 48dp를 넘는다.
                    modifier = Modifier.height(BOTTOM_BAR_HEIGHT),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
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
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(21.dp)
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        letterSpacing = 0.sp
                                    )
                                )
                            },
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
                // 탭으로 들어와도 홈이 항상 스택 아래에 있으므로 popBackStack이
                // 시스템 뒤로가기와 같은 곳으로 간다. 두 진입 경로의 동작이 갈리지 않는다.
                SearchScreen(
                    onStationClick = onStationClick,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(MainTab.ALARM.route) {
                AlarmScreen()
            }
        }
    }
}
