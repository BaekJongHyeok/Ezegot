package com.jonghyeok.ezegot.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/*
 * 라이트 팔레트.
 *
 * 화면 코드는 이 값들을 직접 쓰지 않는다. MaterialTheme.colorScheme만 참조한다.
 * 유일한 예외는 노선 공식 색인 [getSubwayLineColor], [onSubwayLineColor]다.
 *
 * 팔레트를 한 벌 더 만들고 Theme.kt에서 고르기만 하면 다른 테마를 추가할 수 있다.
 * 화면 코드는 손대지 않아도 된다.
 */

// ── 바탕 ────────────────────────────────────────────────────────
/** 화면 바탕 */
val LightBackground = Color(0xFFFFFFFF)

/** 카드·섹션 배경. 바탕과 1.05:1이라 경계는 [LightOutline]이 맡는다 */
val LightSurface = Color(0xFFF8FAFC)

/** 구분선·테두리 */
val LightOutline = Color(0xFFE2E8F0)

// ── 텍스트 ──────────────────────────────────────────────────────
/** 주 텍스트. 바탕 대비 17.9:1 */
val LightTextPrimary = Color(0xFF0F172A)

/** 보조 텍스트. 바탕 대비 4.76:1 */
val LightTextSecondary = Color(0xFF64748B)

/** 비활성·지난 정보. 바탕 대비 2.6:1 — 본문용이 아니다 */
val LightTextDisabled = Color(0xFF94A3B8)

// ── 강조 ────────────────────────────────────────────────────────
/** 브랜드·선택·링크. 바탕 대비 5.17:1 */
val LightAccent = Color(0xFF2563EB)

/**
 * 도착 임박 전용. 바탕 대비 4.83:1
 *
 * 브랜드색과 분리한 이유는, 둘이 같으면 어느 쪽도 강조로 기능하지 않기 때문이다.
 */
val LightUrgent = Color(0xFFDC2626)

// ── 지하철 호선 색상 ────────────────────────────────────────────
/**
 * 노선 공식 색. 팔레트와 무관하게 고정이므로 화면에서 직접 쓰는 유일한 예외다.
 * 이 색 위에 얹을 글자색은 [onSubwayLineColor]가 배경 휘도로 고른다.
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
 * 노선 색 위에 얹을 글자색을 배경 휘도로 고른다.
 *
 * 흰 글자를 고정으로 쓰면 밝은 노선에서 읽히지 않는다. 수인분당선(#FABE00) 위
 * 흰 글씨는 1.69:1이었다.
 *
 * 팔레트의 TextPrimary·Background 대신 순수 검정·흰색을 쓴다. 팔레트 색으로는
 * 일부 노선이 어느 쪽을 골라도 4.5:1을 못 넘긴다. 순수색이면 18개 노선 전부
 * 4.5:1 이상이 되고 최악(GTX-A)이 4.63:1이다.
 *
 * 임계 0.179는 두 선택의 명암비가 같아지는 휘도다. 이 값에서 양쪽 모두 4.58:1이라
 * 어떤 배경색이 와도 기준을 밑돌지 않는다.
 */
fun onSubwayLineColor(background: Color): Color =
    if (background.luminance() > 0.179f) SubwayBadgeOnLight else SubwayBadgeOnDark

private val SubwayBadgeOnLight = Color(0xFF000000)
private val SubwayBadgeOnDark = Color(0xFFFFFFFF)
