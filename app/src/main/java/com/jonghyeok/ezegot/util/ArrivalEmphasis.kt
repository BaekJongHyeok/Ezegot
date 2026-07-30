package com.jonghyeok.ezegot.util

import com.jonghyeok.ezegot.dto.RealtimeArrival

/**
 * 도착 시간의 강조 단계.
 *
 * 사용자가 이 화면에서 내리는 판단은 사실상 셋이다.
 * 지금 뛴다 / 그냥 간다 / 다음 걸 본다. 그래서 3단계다.
 *
 * 전부 같은 색으로 강조하면 아무것도 강조되지 않는다. 실제로 한 화면에
 * 빨간 굵은 글씨가 9개까지 나왔다.
 */
enum class ArrivalEmphasis {
    /** 도착·진입·3분 이내. 지금 움직여야 한다 */
    URGENT,

    /** 4~9분. 여유 있게 이동 */
    NORMAL,

    /** 10분 이상. 기다려야 한다 */
    DISTANT,

    /** 출발했거나 정보가 없다. 판단 대상이 아니다 */
    INACTIVE
}

/** [URGENT]일 때 "곧 도착" 칩을 붙일지. 분 단위 표시가 없는 즉시 도착만 해당한다 */
val ArrivalEmphasis.showsImminentChip: Boolean
    get() = this == ArrivalEmphasis.URGENT

/**
 * 도착 메시지에서 강조 단계를 판정한다.
 *
 * 앱과 위젯이 같은 규칙을 쓰도록 판정을 여기 한 곳에 둔다.
 *
 * [RealtimeArrival.getFormattedMessage]가 만들어내는 문자열은 아래가 전부다.
 *   "N분 후"   → 분 수로 URGENT(≤3) / NORMAL(4~9) / DISTANT(≥10)
 *   "곧 도착"  → URGENT
 *   "도착"     → URGENT
 *   "진입"     → URGENT
 *   "출발"     → INACTIVE  (이미 떠난 열차)
 *   "정보 없음" → INACTIVE
 *   그 외 원문  → INACTIVE (분류할 수 없으면 강조하지 않는다)
 *
 * "전역"·"[N]번째 전역"은 getFormattedMessage 단계에서 이미 "N분 후"로
 * 환산되므로 여기까지 오지 않는다.
 */
fun arrivalEmphasisOf(message: String): ArrivalEmphasis {
    val text = message.trim()

    Regex("(\\d+)분").find(text)?.let { match ->
        val minutes = match.groupValues[1].toIntOrNull() ?: return ArrivalEmphasis.INACTIVE
        return when {
            minutes <= 3 -> ArrivalEmphasis.URGENT
            minutes <= 9 -> ArrivalEmphasis.NORMAL
            else -> ArrivalEmphasis.DISTANT
        }
    }

    return when {
        text.contains("출발") -> ArrivalEmphasis.INACTIVE
        text.contains("도착") || text.contains("진입") -> ArrivalEmphasis.URGENT
        else -> ArrivalEmphasis.INACTIVE
    }
}

fun RealtimeArrival.emphasis(): ArrivalEmphasis = arrivalEmphasisOf(getFormattedMessage())
