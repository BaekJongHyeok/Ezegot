package com.jonghyeok.ezegot.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/*
 * 라이트 팔레트 (목업 v5).
 *
 * 화면 코드는 이 값들을 직접 쓰지 않는다. MaterialTheme.colorScheme만 참조한다.
 * 예외는 노선 공식 색 함수들뿐이다.
 */

// ── 바탕 ────────────────────────────────────────────────────────
/** 페이지 바탕. 카드가 흰색이라 바탕은 한 톤 낮춘다 */
val LightPageBackground = Color(0xFFF5F6F8)

/** 카드 */
val LightCard = Color(0xFFFFFFFF)

/** 카드 테두리 1dp */
val LightCardBorder = Color(0xFFE6E8EC)

/** 카드 안 헤더 영역 */
val LightCardHeaderTint = Color(0xFFFAFBFC)

/** 리스트 행 구분선 */
val LightDivider = Color(0xFFF0F1F4)

// ── 텍스트 4단계 ────────────────────────────────────────────────
val LightTextPrimary = Color(0xFF16181D)
val LightTextSecondary = Color(0xFF5F6570)
val LightTextTertiary = Color(0xFF8A9098)
val LightTextDisabled = Color(0xFFB4B9C0)

// ── 도착 임박 ───────────────────────────────────────────────────
val LightUrgent = Color(0xFFD6202A)

/** "곧 도착" 칩 배경 */
val LightUrgentBg = Color(0xFFFDECEE)

/** "곧 도착" 칩 텍스트. 배경이 연해서 본문 Urgent보다 어둡다 */
val LightUrgentText = Color(0xFFA6122F)

// ── 지하철 호선 색상 ────────────────────────────────────────────
/**
 * 노선 공식 색. 팔레트와 무관하게 고정이므로 화면에서 직접 쓰는 유일한 예외다.
 */
fun getSubwayLineColor(lineName: String): Color {
    return when (lineName) {
        "1호선", "01호선" -> Color(0xFF0052A4)
        "2호선", "02호선" -> Color(0xFF009D3E)
        "3호선", "03호선" -> Color(0xFFEF7C1C)
        "4호선", "04호선" -> Color(0xFF00A5DE)
        "5호선", "05호선" -> Color(0xFF996CAC)
        "6호선", "06호선" -> Color(0xFFCD7C2F)
        "7호선", "07호선" -> Color(0xFF747F00)
        "8호선", "08호선" -> Color(0xFFEA545D)
        "9호선", "09호선" -> Color(0xFFBDB092)
        "경의중앙선" -> Color(0xFF77C4A3)
        "공항철도" -> Color(0xFF0090D2)
        "경춘선" -> Color(0xFF0C8E72)
        "수인분당선" -> Color(0xFFFABE00)
        "신분당선" -> Color(0xFFD4003B)
        "우이신설선" -> Color(0xFFB0CE18)
        "경강선" -> Color(0xFF003DA5)
        "서해선" -> Color(0xFF81A914)
        "GTX-A" -> Color(0xFF9A6292)
        else -> LightTextSecondary // 기본 색상
    }
}

/**
 * 노선 원색 위에 얹을 글자색.
 *
 * 흰 글자를 고정으로 쓰면 밝은 노선에서 읽히지 않는다. 수인분당선(#FABE00) 위
 * 흰 글씨는 1.69:1이다. 배경 휘도로 순수 검정·흰색 중 대비가 큰 쪽을 고른다.
 *
 * 팔레트 색(#16181D / #FFFFFF)이 아니라 순수 검정을 쓰는 이유는, 근사색으로는
 * 일부 노선이 어느 쪽을 골라도 4.5:1을 못 넘기기 때문이다.
 * 임계 0.179는 두 선택의 명암비가 같아지는 휘도이며, 그 지점에서 양쪽 모두
 * 4.58:1이라 어떤 배경색이 와도 기준을 밑돌지 않는다.
 */
fun onSubwayLineColor(background: Color): Color =
    if (background.luminance() > 0.179f) Color.Black else Color.White

/**
 * 큰 글자·아이콘용 노선 위 글자색. 역 상세 헤더처럼 22sp 글자와 아이콘이 얹히는 곳에 쓴다.
 *
 * WCAG는 큰 텍스트(18sp+)와 아이콘 같은 비텍스트 요소에 3:1을 요구한다.
 * 작은 뱃지와 같은 임계(4.5:1)를 쓰면 2호선(#009D3E)처럼 어두운 노선까지
 * 검정으로 뒤집혀, 지하철 표기 관행과 어긋나고 헤더가 탁해 보인다.
 *
 * 임계 0.30은 흰 글자가 3:1을 만족하는 최대 배경 휘도다.
 * 이 값 이하면 흰색(관행), 넘으면 검정으로 간다.
 */
fun onSubwayLineColorLarge(background: Color): Color =
    if (background.luminance() > 0.30f) Color.Black else Color.White

/**
 * 연한 노선색 배경. 주변 역 정사각 뱃지처럼 작은 면적에 쓴다.
 * 원색을 흰색과 섞어 12% 농도로 만든다.
 */
fun subwayLineTint(lineColor: Color): Color = lineColor.copy(alpha = 0.12f)

/**
 * 연한 배경 위에 얹을 진한 노선색.
 *
 * 원색 그대로는 밝은 노선(수인분당선·9호선 등)에서 연한 배경과 붙어 읽히지 않는다.
 * 검정과 섞어 70% 수준으로 어둡게 만든 뒤, 그래도 4.5:1에 못 미치면
 * 기준을 넘을 때까지 더 어둡게 한다.
 */
fun subwayLineOnTint(lineColor: Color, background: Color): Color {
    var factor = 0.70f
    var candidate = lineColor.darken(factor)
    // 최대 10회. 매번 30%씩 더 어둡게 하며 4.5:1을 찾는다
    repeat(10) {
        if (contrastRatio(candidate, background) >= 4.5f) return candidate
        factor *= 0.70f
        candidate = lineColor.darken(factor)
    }
    return candidate
}

/** 원색을 검정 쪽으로 [factor]만큼 남긴다 (0에 가까울수록 어둡다) */
private fun Color.darken(factor: Float): Color =
    Color(red * factor, green * factor, blue * factor, alpha)

/** WCAG 상대 명암비 */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    val hi = maxOf(la, lb)
    val lo = minOf(la, lb)
    return (hi + 0.05f) / (lo + 0.05f)
}
