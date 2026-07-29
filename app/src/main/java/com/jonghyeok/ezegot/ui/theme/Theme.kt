package com.jonghyeok.ezegot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 앱 색 체계. 라이트 한 벌만 둔다.
 *
 * 화면 코드가 MaterialTheme.colorScheme만 참조하므로, 다른 테마를 추가할 때는
 * 이 파일에 팔레트 한 벌과 ColorScheme 하나를 더 만들고 [EzegotTheme]에서
 * 고르기만 하면 된다. 화면 코드는 한 줄도 손대지 않는다.
 *
 * ## 슬롯 배정 메모
 *
 * Material3에는 "비활성 텍스트" 슬롯이 없다. 규격은 onSurface에 alpha 38%를
 * 얹으라고 하지만 그러면 화면마다 alpha 상수가 흩어진다. 대신 이 앱이 쓰지 않는
 * tertiary를 비활성 텍스트로 배정했다. 색 결정이 이 파일 한 곳에 모인다.
 *
 * surfaceVariant는 surface와 같은 값이다. 라이트에서는 표면 단계를 색으로 나누면
 * 1.05:1까지밖에 안 벌어져서, 경계는 outline이 맡고 표면은 한 단계만 쓴다.
 */
private val EzegotLightColorScheme = lightColorScheme(
    // 액센트 (브랜드·선택·링크)
    primary            = LightAccent,
    onPrimary          = LightBackground,
    primaryContainer   = LightSurface,
    onPrimaryContainer = LightAccent,
    secondary          = LightAccent,
    onSecondary        = LightBackground,

    // 비활성·부차 정보 (위 메모 참고)
    tertiary           = LightTextDisabled,
    onTertiary         = LightBackground,

    // 바탕
    background         = LightBackground,
    onBackground       = LightTextPrimary,
    surface            = LightSurface,
    onSurface          = LightTextPrimary,
    surfaceVariant     = LightSurface,
    onSurfaceVariant   = LightTextSecondary,

    // 경계
    outline            = LightOutline,
    outlineVariant     = LightOutline,

    // 상태
    error              = LightUrgent,
    onError            = LightBackground,

    scrim              = Color.Black,
)

@Composable
fun EzegotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EzegotLightColorScheme,
        typography  = Typography,
        content     = content
    )
}
