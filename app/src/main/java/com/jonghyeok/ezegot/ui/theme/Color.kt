package com.jonghyeok.ezegot.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * 다크 팔레트.
 *
 * 위젯(ArrivalWidget)이 쓰던 색을 앱 전체의 기준으로 삼았다. 앱 상단만 네이비이고
 * 본문은 밝던 구조를 없애, 앱과 위젯이 같은 시각 언어를 쓰게 한다.
 *
 * 이름에 밝기를 넣지 않는다. 예전 SurfaceWhite / BackgroundLight / TextOnDark는
 * 이름이 값을 못 박고 있어서 팔레트를 바꾸는 순간 이름이 거짓이 됐다.
 * 접두사 Dark는 "어떤 팔레트에 속하는가"를 가리키며, 라이트 팔레트를 추가하면
 * Light* 한 벌을 더 만들고 Theme.kt에서 고르기만 하면 된다.
 *
 * 화면 코드는 이 값들을 직접 쓰지 않는다. MaterialTheme.colorScheme만 참조한다.
 * 유일한 예외는 노선 공식 색인 [getSubwayLineColor]다.
 */

// ── 바탕 ────────────────────────────────────────────────────────
/** 화면 바탕 */
val DarkBackground = Color(0xFF0C1624)

/** 바탕 위 한 단계. 헤더·카드·리스트 배경 */
val DarkSurface = Color(0xFF16273A)

/** 표면 위 한 단계. 입력 필드·칩처럼 눌러지는 것 */
val DarkSurfaceElevated = Color(0xFF1C2E40)

/** 구분선·테두리. 다크에서는 그림자가 보이지 않아 경계를 이 색이 맡는다 */
val DarkOutline = Color(0xFF2E4560)

// ── 텍스트 ──────────────────────────────────────────────────────
/** 주 텍스트. 바탕 대비 16.6:1 */
val DarkTextPrimary = Color(0xFFF0F5FF)

/** 보조 텍스트. 바탕 대비 8.3:1 */
val DarkTextSecondary = Color(0xFF8EB4D4)

/** 비활성·지난 정보. 바탕 대비 3.6:1 — 본문용이 아니라 부차 정보용 */
val DarkTextDisabled = Color(0xFF4A7291)

// ── 강조 ────────────────────────────────────────────────────────
/** 브랜드 액센트. 탭 선택, 링크, 아이콘. 바탕 대비 10.9:1 */
val DarkAccent = Color(0xFF4DD9F5)

/**
 * 도착 임박.
 *
 * 액센트와 분리한 이유는, 브랜드색과 긴급색이 같으면 둘 다 의미를 잃기 때문이다.
 * 기존 ArrivalRed(#EF5350)보다 밝혀 어두운 바탕에서 6.6:1을 확보했다.
 */
val DarkUrgent = Color(0xFFFF6B6B)

// ── 지하철 호선 색상 ────────────────────────────────────────────
/**
 * 노선 공식 색. 팔레트와 무관하게 고정이므로 화면에서 직접 쓰는 유일한 예외다.
 * 이 색 위에 얹을 글자색은 배경 휘도로 자동 선택한다([onSubwayLineColor]).
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
        else -> DarkOutline // 기본 색상
    }
}

// ─────────────────────────────────────────────────────────────────
// 아래는 colorScheme 마이그레이션이 끝나면 삭제한다.
// 화면 13개를 한 번에 고치면 중간 빌드가 불가능해, 파일 단위로 옮기는 동안만 남겨둔다.
// ─────────────────────────────────────────────────────────────────
@Deprecated("MaterialTheme.colorScheme를 쓸 것", ReplaceWith("MaterialTheme.colorScheme.surface"))
val Navy900 = Color(0xFF0D1B2A)

@Deprecated("MaterialTheme.colorScheme를 쓸 것")
val Navy800 = Color(0xFF1A2D42)

@Deprecated("MaterialTheme.colorScheme를 쓸 것")
val Navy700 = Color(0xFF243D57)

@Deprecated("MaterialTheme.colorScheme.primary를 쓸 것")
val SkyBlue400 = Color(0xFF4FC3F7)

@Deprecated("MaterialTheme.colorScheme.primary를 쓸 것")
val SkyBlue300 = Color(0xFF81D4FA)

@Deprecated("MaterialTheme.colorScheme.background를 쓸 것")
val BackgroundLight = Color(0xFFF7F9FC)

@Deprecated("MaterialTheme.colorScheme.surface를 쓸 것")
val SurfaceWhite = Color(0xFFFFFFFF)

@Deprecated("MaterialTheme.colorScheme.onSurface를 쓸 것")
val TextPrimary = Color(0xFF0D1B2A)

@Deprecated("MaterialTheme.colorScheme.onSurfaceVariant를 쓸 것")
val TextSecondary = Color(0xFF546E7A)

@Deprecated("MaterialTheme.colorScheme.tertiary를 쓸 것")
val TextHint = Color(0xFF5C7A88)

@Deprecated("MaterialTheme.colorScheme.onPrimary를 쓸 것")
val TextOnDark = Color(0xFFFFFFFF)

@Deprecated("MaterialTheme.colorScheme.error를 쓸 것")
val ArrivalRed = Color(0xFFEF5350)

@Deprecated("MaterialTheme.colorScheme.outline를 쓸 것")
val DividerColor = Color(0xFFECEFF1)
