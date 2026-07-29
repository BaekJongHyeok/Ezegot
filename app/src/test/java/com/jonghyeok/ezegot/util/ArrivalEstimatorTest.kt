package com.jonghyeok.ezegot.util

import com.jonghyeok.ezegot.dto.RealtimeArrival
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * 도착 시간 추정 로직 테스트.
 *
 * 실시간 API가 `barvlDt`(남은 초)를 비워서 주는 경우가 많아, "[N]번째 전역"
 * 문자열만으로 소요 시간을 추정하는 것이 이 앱의 핵심 로직이다.
 *
 * 기대값을 상수로 박으면 가중치를 조정할 때마다 테스트가 깨지므로,
 * 계산 근거가 드러나도록 수식으로 표현하거나 조건 간 관계로 검증한다.
 */
class ArrivalEstimatorTest {

    private val seoul = ZoneId.of("Asia/Seoul")

    /** 혼잡 가중치가 없는 평시(평일 14:00)로 고정한 시계 */
    private val offPeakClock: Clock =
        Clock.fixed(Instant.parse("2026-07-29T05:00:00Z"), seoul) // KST 14:00 수요일

    /** 출근 러시아워(평일 08:00)로 고정한 시계 */
    private val morningRushClock: Clock =
        Clock.fixed(Instant.parse("2026-07-28T23:00:00Z"), seoul) // KST 08:00 수요일

    private fun arrival(subwayId: String, trainStatus: String = "일반") =
        RealtimeArrival(subwayId = subwayId, btrainSttus = trainStatus)

    @Test
    fun `호선별 기본 소요시간이 정거장 수에 비례해 적용된다`() {
        // given: 2호선은 1정거장당 2.0분으로 정의되어 있다
        val twoLine = arrival(subwayId = "1002")
        val stationCount = 3

        // when
        val actual = ArrivalEstimator.estimateMinutesFromStations(
            twoLine, stationCount, offPeakClock
        )

        // then: 3정거장 * 2.0분 = 6분 (평시라 가중치 1.0)
        val expected = Math.round(stationCount * 2.0).toInt()
        assertEquals(expected, actual)
    }

    @Test
    fun `등록되지 않은 호선이면 기본값 2_0분으로 폴백한다`() {
        // given: lineAverageTimes에 없는 호선 ID
        val unknownLine = arrival(subwayId = "9999")
        val knownTwoMinuteLine = arrival(subwayId = "1002") // 2호선도 2.0분
        val stationCount = 4

        // when
        val unknownResult = ArrivalEstimator.estimateMinutesFromStations(
            unknownLine, stationCount, offPeakClock
        )
        val knownResult = ArrivalEstimator.estimateMinutesFromStations(
            knownTwoMinuteLine, stationCount, offPeakClock
        )

        // then: 폴백값이 2.0이므로 동일하게 2.0분인 2호선과 결과가 같아야 한다
        assertEquals(knownResult, unknownResult)
    }

    @Test
    fun `급행과 특급은 일반보다 소요시간이 짧고 특급이 가장 빠르다`() {
        // given: 같은 호선, 같은 정거장 수에서 열차 종류만 다르다
        val stationCount = 10
        val normal = arrival(subwayId = "1001", trainStatus = "일반")
        val express = arrival(subwayId = "1001", trainStatus = "급행")
        val limitedExpress = arrival(subwayId = "1001", trainStatus = "특급")

        // when
        val normalMinutes = ArrivalEstimator.estimateMinutesFromStations(normal, stationCount, offPeakClock)
        val expressMinutes = ArrivalEstimator.estimateMinutesFromStations(express, stationCount, offPeakClock)
        val limitedMinutes = ArrivalEstimator.estimateMinutesFromStations(limitedExpress, stationCount, offPeakClock)

        // then: 특급 < 급행 < 일반
        assertTrue(
            "급행($expressMinutes)은 일반($normalMinutes)보다 짧아야 한다",
            expressMinutes < normalMinutes
        )
        assertTrue(
            "특급($limitedMinutes)은 급행($expressMinutes)보다 짧아야 한다",
            limitedMinutes < expressMinutes
        )
    }

    @Test
    fun `출근 시간대에는 혼잡 가중치로 평시보다 오래 걸린다`() {
        // given: 같은 열차, 같은 정거장 수. 시각만 다르다
        val train = arrival(subwayId = "1001")
        val stationCount = 10

        // when
        val offPeak = ArrivalEstimator.estimateMinutesFromStations(train, stationCount, offPeakClock)
        val morningRush = ArrivalEstimator.estimateMinutesFromStations(train, stationCount, morningRushClock)

        // then: 출근 시간대(+15%)가 평시보다 커야 한다
        assertTrue(
            "출근 시간대($morningRush)가 평시($offPeak)보다 커야 한다",
            morningRush > offPeak
        )
    }
}
