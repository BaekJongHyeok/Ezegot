package com.jonghyeok.ezegot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 앱 색 체계. 라이트 한 벌.
 *
 * 화면 코드가 MaterialTheme.colorScheme만 참조하므로, 다른 테마를 추가할 때는
 * 이 파일에 팔레트와 ColorScheme을 한 벌 더 만들고 [EzegotTheme]에서 고르면 된다.
 *
 * ## 슬롯 배정 메모
 *
 * 텍스트가 4단계(Primary/Secondary/Tertiary/Disabled)인데 Material3에는
 * onSurface·onSurfaceVariant 둘뿐이다. 남는 슬롯을 텍스트 단계로 돌려 썼다.
 *   onSurface        → TextPrimary
 *   onSurfaceVariant → TextSecondary
 *   secondary        → TextTertiary
 *   tertiary         → TextDisabled
 * 규격대로 alpha를 얹으면 화면마다 상수가 흩어지므로, 색 결정을 이 파일에 모은다.
 *
 * 표면도 마찬가지다.
 *   background     → 페이지 바탕 (#F5F6F8)
 *   surface        → 카드 (#FFFFFF)
 *   surfaceVariant → 카드 안 헤더 영역 (#FAFBFC)
 *   outline        → 카드 테두리 (#E6E8EC)
 *   outlineVariant → 리스트 구분선 (#F0F1F4)
 *
 * 이 팔레트에는 브랜드 액센트가 없다. 선택 상태는 TextPrimary로 표현하므로
 * primary도 같은 값이다.
 */
private val EzegotLightColorScheme = lightColorScheme(
    // 선택·강조 (별도 브랜드색 없음)
    primary            = LightTextPrimary,
    onPrimary          = LightCard,
    secondary          = LightTextTertiary,
    onSecondary        = LightCard,
    tertiary           = LightTextDisabled,
    onTertiary         = LightCard,

    // 바탕
    background         = LightPageBackground,
    onBackground       = LightTextPrimary,
    surface            = LightCard,
    onSurface          = LightTextPrimary,
    surfaceVariant     = LightCardHeaderTint,
    onSurfaceVariant   = LightTextSecondary,

    // 경계
    outline            = LightCardBorder,
    outlineVariant     = LightDivider,

    // 도착 임박
    error              = LightUrgent,
    onError            = LightCard,
    errorContainer     = LightUrgentBg,
    onErrorContainer   = LightUrgentText,

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
