package com.jonghyeok.ezegot.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 도착 메시지 변환.
 *
 * `"전역 도착"`이 `endsWith("도착")`에 먼저 걸려 현재 역 도착으로 표시되던 버그가
 * 있었다. 한 정거장 떨어진 열차를 "지금 들어온다"고 알렸고, 인접한 두 역에서
 * 같은 열차의 표시가 뒤집혀 보였다. 패턴별로 고정해 다시 들어오지 못하게 한다.
 *
 * 추정 분은 `역 수 × 노선 평균 × 급행 계수 × 혼잡 계수`라 시각에 따라 달라진다.
 * 여기서는 "분 후"로 끝나는지만 보고 정확한 값은 ArrivalEstimator 쪽에서 다룬다.
 */
class RealtimeArrivalTest {

    private fun arrival(message: String, barvl: String = "0") = RealtimeArrival(
        subwayId = "1075",
        btrainSttus = "일반",
        barvlDt = barvl,
        arrivalMessage1 = message
    )

    @Test
    fun `전역 도착은 현재 역 도착이 아니라 한 정거장 전이다`() {
        val message = arrival("전역 도착").getFormattedMessage()
        assertNotEquals("이전 역 도착을 현재 역 도착으로 보면 안 된다", "도착", message)
        assertTrue("N분 후 형태여야 한다: $message", message.endsWith("분 후"))
    }

    @Test
    fun `전역 진입도 한 정거장 전이다`() {
        val message = arrival("전역 진입").getFormattedMessage()
        assertTrue("N분 후 형태여야 한다: $message", message.endsWith("분 후"))
    }

    @Test
    fun `전역 단독도 한 정거장 전이다`() {
        val message = arrival("전역").getFormattedMessage()
        assertTrue("N분 후 형태여야 한다: $message", message.endsWith("분 후"))
    }

    @Test
    fun `N번째 전역은 그 역 수만큼 떨어진 것이다`() {
        val message = arrival("[3]번째 전역 (매탄권선)").getFormattedMessage()
        assertTrue("N분 후 형태여야 한다: $message", message.endsWith("분 후"))
    }

    @Test
    fun `역명 도착은 현재 역 도착이다`() {
        assertEquals("도착", arrival("수원 도착").getFormattedMessage())
    }

    @Test
    fun `역명 진입은 현재 역 진입이다`() {
        assertEquals("진입", arrival("수원 진입").getFormattedMessage())
    }

    @Test
    fun `역명 출발은 이미 떠난 것이다`() {
        assertEquals("출발", arrival("수원 출발").getFormattedMessage())
    }

    @Test
    fun `barvlDt가 있으면 그 값을 우선한다`() {
        assertEquals("4분 후", arrival("전역 도착", barvl = "260").getFormattedMessage())
        assertEquals("곧 도착", arrival("전역 도착", barvl = "40").getFormattedMessage())
    }

    @Test
    fun `빈 메시지는 정보 없음이다`() {
        assertEquals("정보 없음", arrival("").getFormattedMessage())
    }
}
