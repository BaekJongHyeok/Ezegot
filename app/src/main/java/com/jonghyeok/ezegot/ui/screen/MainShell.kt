package com.jonghyeok.ezegot.ui.screen

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 하단 탭 4개를 가진 최상위 껍데기.
 *
 * 역 상세는 이 껍데기 밖(바깥 NavHost)에 있다. 최상위 목적지가 아니라
 * 파고들어간 화면이라, 하단바를 유지하면 56dp를 계속 쓰면서도 그 바가
 * 지금 보고 있는 화면과 무관해진다.
 *
 * 중첩 NavHost 대신 탭 상태만 들고 콘텐츠를 바꾼다. 탭 사이에 뒤로가기
 * 히스토리를 만들 이유가 없어서다.
 */
private enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("홈", Icons.Default.Home),
    NEARBY("근처", Icons.Default.Place),
    SEARCH("검색", Icons.Default.Search),
    ALARM("알림", Icons.Default.NotificationsNone)
}

@Composable
fun MainShell(
    onStationClick: (String, String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

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
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    onSearchClick = { selectedTab = MainTab.SEARCH },
                    onMapClick = { selectedTab = MainTab.NEARBY },
                    onStationClick = onStationClick
                )
                MainTab.NEARBY -> NearbyMapScreen(onStationClick = onStationClick)
                MainTab.SEARCH -> SearchScreen(onStationClick = onStationClick)
                MainTab.ALARM -> AlarmScreen()
            }
        }
    }
}
