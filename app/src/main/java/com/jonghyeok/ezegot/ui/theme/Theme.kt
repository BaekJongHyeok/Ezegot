package com.jonghyeok.ezegot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 앱 색 체계.
 *
 * 현재는 다크 한 벌만 있다. 화면 코드가 MaterialTheme.colorScheme만 참조하므로,
 * 라이트를 추가할 때는 이 파일에 lightColorScheme 한 벌을 더 만들고
 * [EzegotTheme]에서 isSystemInDarkTheme()으로 고르기만 하면 된다.
 * 화면 코드는 한 줄도 손대지 않아도 된다.
 *
 * ## 슬롯 배정 메모
 *
 * Material3에는 "비활성 텍스트" 슬롯이 없다. 규격은 onSurface에 alpha 38%를
 * 얹으라고 하지만, 그러면 화면마다 alpha 상수가 흩어진다.
 * 대신 이 앱에서 쓰지 않는 tertiary를 비활성 텍스트로 배정했다.
 * 색 결정이 이 파일 한 곳에 모이고, 화면에서는 colorScheme.tertiary로 읽는다.
 */
private val EzegotDarkColorScheme = darkColorScheme(
    // 액센트 (브랜드)
    primary            = DarkAccent,
    onPrimary          = DarkBackground,
    primaryContainer   = DarkSurfaceElevated,
    onPrimaryContainer = DarkAccent,
    secondary          = DarkAccent,
    onSecondary        = DarkBackground,

    // 비활성·부차 정보 (위 메모 참고)
    tertiary           = DarkTextDisabled,
    onTertiary         = DarkBackground,

    // 바탕
    background         = DarkBackground,
    onBackground       = DarkTextPrimary,
    surface            = DarkSurface,
    onSurface          = DarkTextPrimary,
    surfaceVariant     = DarkSurfaceElevated,
    onSurfaceVariant   = DarkTextSecondary,

    // 경계
    outline            = DarkOutline,
    outlineVariant     = DarkOutline,

    // 상태
    error              = DarkUrgent,
    onError            = DarkBackground,

    scrim              = Color.Black,
)

@Composable
fun EzegotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EzegotDarkColorScheme,
        typography  = Typography,
        content     = content
    )
}
