package com.jonghyeok.ezegot.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary (딥 네이비) ────────────────────────────────────────
val Navy900  = Color(0xFF0D1B2A)   // Primary Main
val Navy800  = Color(0xFF1A2D42)
val Navy700  = Color(0xFF243D57)

// ── Secondary (스카이 블루) ────────────────────────────────────
val SkyBlue400  = Color(0xFF4FC3F7)  // Accent
val SkyBlue300  = Color(0xFF81D4FA)
val SkyBlue100  = Color(0xFFE1F5FE)

// ── Background & Surface ───────────────────────────────────────
val BackgroundLight = Color(0xFFF7F9FC)
val SurfaceWhite    = Color(0xFFFFFFFF)
val CardSurface     = Color(0xFFFFFFFF)

// ── Text ───────────────────────────────────────────────────────
val TextPrimary   = Color(0xFF0D1B2A)
val TextSecondary = Color(0xFF546E7A)
val TextHint      = Color(0xFFB0BEC5)
val TextOnDark    = Color(0xFFFFFFFF)

// ── Arrival colors ─────────────────────────────────────────────
val ArrivalRed    = Color(0xFFEF5350)
val ArrivalGreen  = Color(0xFF4CAF50)

// ── Divider ────────────────────────────────────────────────────
val DividerColor  = Color(0xFFECEFF1)

// Legacy 호환 (기존 코드용)
val App_Background_Color = BackgroundLight

// ── 지하철 호선 색상 ────────────────────────────────────────────
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
        else -> Navy700 // 기본 색상
    }
}