package com.jonghyeok.ezegot.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jonghyeok.ezegot.ui.screen.MainShell
import com.jonghyeok.ezegot.ui.screen.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")

    /** 하단 탭과 역 상세를 모두 품는 최상위 화면 */
    object Main : Screen("main")
}

/**
 * 최상위 그래프. 스플래시에서 본 화면으로 한 번 넘어가면 끝이다.
 * 탭 전환과 역 상세는 [MainShell] 안쪽 NavHost가 다룬다 —
 * 목업이 역 상세에서도 하단바를 유지하기 때문이다.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        // 기본 크로스페이드를 끈다.
        // 페이드 중에는 이전 화면과 새 화면이 동시에 반투명으로 그려져,
        // 앞 화면의 내용이 유령처럼 겹쳐 보였다.
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

        composable(Screen.Main.route) {
            MainShell()
        }
    }
}
