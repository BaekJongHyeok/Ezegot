package com.jonghyeok.ezegot.util

import com.jonghyeok.ezegot.dto.RealtimeArrival
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 도착 목록을 화면에 올리기 전 거르고 정렬한다.
 *
 * 앱과 위젯이 같은 순서를 보여주도록 규칙을 여기 한 곳에 둔다.
 *
 * ## 왜 정렬이 필요한가
 *
 * 서울 API는 도착 시간이 아니라 **(급행 여부 → 순번)** 순으로 내려준다.
 * `ordkey`가 그 구조를 그대로 드러낸다.
 *
 * ```
 * 1 1 001 인천 0
 * │ │  │   │   └ 급행 여부 (0=일반, 1=급행)
 * │ │  │   └───── 종착역
 * │ │  └───────── 남은 역 수
 * │ └──────────── 그 계통 안에서의 순번
 * └────────────── 0=상행, 1=하행
 * ```
 *
 * 수원역 하행을 그대로 나열하면 일반(1역) → 급행(12역) → 일반(1역) → 급행(16역)이
 * 되어 화면에 "진입 / 24분 / 진입 / 31분"처럼 찍힌다.
 */

/** 도착·진입 상태인데 이만큼 지난 데이터면 그 열차는 이미 떠났다고 본다 */
private val DEPARTED_THRESHOLD: Duration = Duration.ofMinutes(3)

private val RECEPTION_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/**
 * 표시용 목록. [direction] 방향만 남기고 떠난 열차를 걸러 도착 순으로 정렬한다.
 *
 * @param lineId 이 화면이 보여줄 노선. 환승역은 한 응답에 여러 노선이 섞여 온다.
 */
fun List<RealtimeArrival>.forDisplay(
    lineId: String?,
    clock: Clock = Clock.systemDefaultZone(),
    matchesDirection: (String) -> Boolean
): List<RealtimeArrival> =
    filter { it.subwayId == lineId && matchesDirection(it.updnLine) && !it.hasLeft(clock) }
        // 표시값이 단조 증가하도록 화면에 찍히는 추정 분을 그대로 정렬 키로 쓴다.
        // 같은 분이면 실제로 더 가까운 쪽(남은 역 수)이 위로 온다.
        .sortedWith(compareBy({ it.minutesAway() }, { it.remainingStations() }))

/**
 * 이미 떠났는가.
 *
 * `"출발"`은 API가 명시해 준다. 문제는 `"도착"`·`"진입"`인데 데이터가 낡은 경우다.
 * 급행 계통은 일반보다 4.6~5.0분 뒤처져 내려오므로(수원역 실측), 5분 전에
 * "도착"이던 열차가 여전히 "도착"으로 남아 있다. 그 열차는 이미 떠났다.
 *
 * 임계를 90초로 잡으면 급행이 상시 사라져 "이 역에 급행이 안 온다"로 오해된다.
 * 3분이면 실제로 떠난 열차만 걸러진다.
 */
private fun RealtimeArrival.hasLeft(clock: Clock): Boolean {
    if (getFormattedMessage() == "출발") return true

    val message = getFormattedMessage()
    if (message != "도착" && message != "진입" && message != "곧 도착") return false

    val received = runCatching {
        LocalDateTime.parse(receptionTime, RECEPTION_FORMAT)
    }.getOrNull() ?: return false

    val now = LocalDateTime.now(clock.withZone(ZoneId.systemDefault()))
    return Duration.between(received, now) >= DEPARTED_THRESHOLD
}

/**
 * 화면에 찍히는 추정 분.
 *
 * [RealtimeArrival.getFormattedMessage]가 만드는 문자열과 같은 값을 숫자로 낸다.
 * 문자열을 다시 파싱하지 않고 같은 규칙을 따라 계산한다.
 */
private fun RealtimeArrival.minutesAway(): Int {
    val seconds = barvlDt.toIntOrNull() ?: 0
    if (seconds > 0) return seconds / 60

    val raw = arrivalMessage1.substringBefore("(").trim()

    Regex("\\[(\\d+)]번째 전역").find(raw)?.let { match ->
        val count = match.groupValues[1].toIntOrNull() ?: return 0
        return ArrivalEstimator.estimateMinutesFromStations(this, count)
    }

    // "전역 도착"·"전역 진입"·"전역"은 한 정거장 앞이다.
    // 현재 역의 "도착"·"진입"(0분)보다 뒤에 와야 한다.
    if (raw.startsWith("전역")) {
        return ArrivalEstimator.estimateMinutesFromStations(this, 1)
    }

    return 0
}

/**
 * 남은 역 수. [RealtimeArrival.ordkey]의 3~5번째 자리다.
 *
 * 추정 분이 같을 때만 쓰는 보조 키다. 값이 없으면 뒤로 보낸다.
 */
private fun RealtimeArrival.remainingStations(): Int =
    ordkey.takeIf { it.length >= 5 }
        ?.substring(2, 5)
        ?.toIntOrNull()
        ?: Int.MAX_VALUE
