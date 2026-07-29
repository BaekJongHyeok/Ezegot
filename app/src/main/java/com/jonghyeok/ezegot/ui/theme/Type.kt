package com.jonghyeok.ezegot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.R

/**
 * Pretendard Std Variable.
 *
 * 가변 폰트라 파일 하나로 모든 웨이트를 낸다. 정적 웨이트를 여러 개 넣는 것보다
 * 용량이 작다. FontVariation은 API 26+가 필요한데 minSdk가 33이라 제약이 없다.
 *
 * 웨이트는 400(Normal)과 500(Medium) 두 단계만 쓴다. 크기와 색으로 위계를
 * 이미 만들고 있어서, 굵기까지 5단계로 벌리면 화면이 시끄러워진다.
 * 예전에는 Medium/SemiBold/Bold/ExtraBold/Black이 섞여 있었다.
 *
 * 라이선스: SIL Open Font License 1.1 (assets/pretendard_OFL.txt)
 */
@OptIn(ExperimentalTextApi::class)
private val Pretendard = FontFamily(
    Font(
        R.font.pretendard_variable,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.pretendard_variable,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    )
)

/**
 * 워드마크 전용 패밀리. 700을 갖는 유일한 곳이다.
 *
 * 본문은 400/500 두 단계로 제한하지만 로고까지 500이면 타이틀로 읽히지 않고
 * 그냥 대문자 본문처럼 보인다. 예외를 여기 한 곳에 가둬 두어 호출부가
 * 임의로 Bold를 쓰지 못하게 한다.
 */
@OptIn(ExperimentalTextApi::class)
private val PretendardWordmark = FontFamily(
    Font(
        R.font.pretendard_variable,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

/**
 * "EZEGOT" 워드마크.
 *
 * letterSpacing은 굵어질수록 좁혀야 한다. 15sp/500에서 1.5sp(0.1em)였는데
 * 19sp/700에 같은 비율을 주면 글자가 흩어져 보인다. 0.06em으로 낮춘다.
 */
val EzegotWordmark = TextStyle(
    fontFamily = PretendardWordmark,
    fontWeight = FontWeight.Bold,
    fontSize = 19.sp,
    lineHeight = 24.sp,
    letterSpacing = 1.2.sp
)

/**
 * 숫자를 고정폭으로 뽑는다.
 *
 * 도착 시간이 매초 바뀌는데 비례폭 숫자를 쓰면 "1분"과 "11분"에서 열이 흔들린다.
 * 한글에는 영향이 없다.
 */
private const val TABULAR = "tnum"

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = Pretendard,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    fontFeatureSettings = TABULAR
)

val Typography = Typography(
    displayLarge   = style(40, 48, FontWeight.Medium, -0.5),
    displayMedium  = style(32, 40, FontWeight.Medium),
    displaySmall   = style(28, 36, FontWeight.Medium),
    headlineLarge  = style(28, 36, FontWeight.Medium),
    headlineMedium = style(24, 32, FontWeight.Medium),
    headlineSmall  = style(20, 28, FontWeight.Medium),
    titleLarge     = style(18, 26, FontWeight.Medium),
    titleMedium    = style(16, 24, FontWeight.Medium),
    titleSmall     = style(14, 20, FontWeight.Medium),
    bodyLarge      = style(16, 24, FontWeight.Normal, 0.15),
    bodyMedium     = style(14, 20, FontWeight.Normal, 0.25),
    bodySmall      = style(12, 16, FontWeight.Normal, 0.4),
    labelLarge     = style(14, 20, FontWeight.Medium),
    labelMedium    = style(12, 16, FontWeight.Medium, 0.5),
    labelSmall     = style(11, 16, FontWeight.Medium, 0.5),
)
