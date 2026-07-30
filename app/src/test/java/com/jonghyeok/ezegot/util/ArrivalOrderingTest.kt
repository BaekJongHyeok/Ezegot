package com.jonghyeok.ezegot.util

import com.jonghyeok.ezegot.dto.RealtimeArrival
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * 도착 목록 거르기·정렬.
 *
 * 표본은 수원역 실측 응답이다. 서울 API가 도착 시간이 아니라
 * (급행 여부 → 순번) 순으로 내려주기 때문에 화면에서 "진입 / 24분 / 진입 / 31분"처럼
 * 뒤섞여 보였다.
 */
class ArrivalOrderingTest {

    private val zone = ZoneId.of("Asia/Seoul")
    private fun clockAt(time: String): Clock =
        Clock.fixed(Instant.parse(time), zone)

    private fun arrival(
        trainNo: String,
        updn: String = "하행",
        message: String,
        ordkey: String,
        received: String,
        barvl: String = "0",
        sttus: String = "일반"
    ) = RealtimeArrival(
        subwayId = "1075",
        updnLine = updn,
        ordkey = ordkey,
        btrainSttus = sttus,
        bstatnNm = "고색",
        barvlDt = barvl,
        trainNumber = trainNo,
        arrivalMessage1 = message,
        receptionTime = received
    )

    @Test
    fun `급행 계통이 끼어들어도 도착 순으로 정렬된다`() {
        // given: API가 내려주는 순서 그대로 (일반 1역 → 급행 12역 → 일반 1역 → 급행 16역)
        val rows = listOf(
            arrival("6583", message = "전역 진입", ordkey = "11001인천0", received = "2026-07-29 18:57:00"),
            arrival("6405", message = "[12]번째 전역 (죽전)", ordkey = "11012고색1", received = "2026-07-29 18:57:00"),
            arrival("6169", message = "전역 진입", ordkey = "12001고색0", received = "2026-07-29 18:57:00"),
            arrival("6407", message = "[16]번째 전역 (수내)", ordkey = "12016고색1", received = "2026-07-29 18:57:00")
        )

        // when
        val sorted = rows.forDisplay("1075", clockAt("2026-07-29T09:57:30Z")) { true }

        // then: 가까운 두 대가 먼저, 그다음 12역·16역 순
        assertEquals(listOf("6583", "6169", "6405", "6407"), sorted.map { it.trainNumber })
    }

    @Test
    fun `추정 분이 같으면 남은 역 수가 적은 쪽이 앞선다`() {
        val rows = listOf(
            arrival("far", message = "[2]번째 전역 (A)", ordkey = "11002고색0", received = "2026-07-29 18:57:00"),
            arrival("near", message = "전역 진입", ordkey = "11001고색0", received = "2026-07-29 18:57:00")
        )

        val sorted = rows.forDisplay("1075", clockAt("2026-07-29T09:57:30Z")) { true }

        // 둘 다 몇 분 안쪽이지만 1역 남은 쪽이 먼저다
        assertEquals("near", sorted.first().trainNumber)
    }

    @Test
    fun `도착 상태인데 3분 넘게 낡은 행은 이미 떠난 열차로 본다`() {
        // given: 급행 피드는 일반보다 5분 가까이 뒤처져 내려온다
        val rows = listOf(
            arrival("fresh", message = "수원 진입", ordkey = "01000왕십리0", received = "2026-07-29 18:57:00"),
            arrival("stale", message = "수원 도착", ordkey = "01000왕십리1", received = "2026-07-29 18:52:00", sttus = "급행")
        )

        val sorted = rows.forDisplay("1075", clockAt("2026-07-29T09:57:30Z")) { true }

        assertEquals(listOf("fresh"), sorted.map { it.trainNumber })
    }

    @Test
    fun `3분 이내면 급행이라도 남긴다`() {
        // 90초로 자르면 급행이 상시 사라져 "이 역에 급행이 안 온다"로 오해된다
        val rows = listOf(
            arrival("express", message = "수원 도착", ordkey = "01000왕십리1", received = "2026-07-29 18:55:30", sttus = "급행")
        )

        val sorted = rows.forDisplay("1075", clockAt("2026-07-29T09:57:30Z")) { true }

        assertEquals(1, sorted.size)
    }

    @Test
    fun `출발한 열차와 다른 노선은 제외된다`() {
        val rows = listOf(
            arrival("gone", message = "출발", ordkey = "11001고색0", received = "2026-07-29 18:57:00"),
            arrival("keep", message = "전역 진입", ordkey = "11001고색0", received = "2026-07-29 18:57:00"),
            arrival("other", message = "전역 진입", ordkey = "11001고색0", received = "2026-07-29 18:57:00")
                .apply { subwayId = "1001" }
        )

        val sorted = rows.forDisplay("1075", clockAt("2026-07-29T09:57:30Z")) { true }

        assertEquals(listOf("keep"), sorted.map { it.trainNumber })
    }

    @Test
    fun `방향 필터가 적용된다`() {
        val rows = listOf(
            arrival("up", updn = "상행", message = "전역 진입", ordkey = "01001왕십리0", received = "2026-07-29 18:57:00"),
            arrival("down", updn = "하행", message = "전역 진입", ordkey = "11001고색0", received = "2026-07-29 18:57:00")
        )

        val sorted = rows.forDisplay("1075", clockAt("2026-07-29T09:57:30Z")) { it == "하행" }

        assertEquals(listOf("down"), sorted.map { it.trainNumber })
    }
}
