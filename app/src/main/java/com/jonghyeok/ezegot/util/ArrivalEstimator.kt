package com.jonghyeok.ezegot.util

import com.jonghyeok.ezegot.dto.RealtimeArrival
import java.util.Calendar

object ArrivalEstimator {
    
    // 호선별 1정거장당 평균 소요 시간 (단위: 분)
    // 서울교통공사 및 일반적인 철도 운행 정보 기반 추정치 (보수적)
    private val lineAverageTimes = mapOf(
        "1001" to 2.2, // 1호선 (지상 구간 등 지연 요소 고려)
        "1002" to 2.0, // 2호선
        "1003" to 2.1, // 3호선
        "1004" to 2.1, // 4호선
        "1005" to 2.2, // 5호선
        "1006" to 2.0, // 6호선
        "1007" to 2.0, // 7호선
        "1008" to 2.0, // 8호선
        "1009" to 1.9, // 9호선 (역간 거리 비교적 짧음)
        "1061" to 2.3, // 중앙선
        "1063" to 2.3, // 경의중앙선
        "1065" to 2.5, // 공항철도 (역간 거리가 긺)
        "1067" to 2.4, // 경춘선
        "1075" to 2.2, // 수인분당선
        "1077" to 2.6, // 신분당선 (매우 빠르나 역간 거리 긺)
        "1092" to 1.8, // 우이신설선 (경전철)
        "1093" to 2.2, // 서해선
        "1081" to 2.5, // 경강선
        "1032" to 3.0  // GTX-A (속도는 빠르나 역간 거리가 대폭 긺)
    )

    /**
     * 현재 시간이 출퇴근(Rush Hour) 시간대인지 판단하여 가중치를 반환
     * 출근: 평일 07:00 ~ 09:30 (+15% 지연)
     * 퇴근: 평일 17:30 ~ 20:00 (+20% 지연)
     */
    private fun getCongestionMultiplier(): Double {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // 주말(토, 일)은 해당 없음
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return 1.0

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute

        val morningRushStart = 7 * 60         // 07:00
        val morningRushEnd = 9 * 60 + 30      // 09:30
        val eveningRushStart = 17 * 60 + 30   // 17:30
        val eveningRushEnd = 20 * 60          // 20:00

        return when {
            timeInMinutes in morningRushStart..morningRushEnd -> 1.15
            timeInMinutes in eveningRushStart..eveningRushEnd -> 1.20
            else -> 1.0
        }
    }

    /**
     * 역 개수와 열차 정보를 바탕으로 정교한 소요 시간(분)을 계산
     */
    fun estimateMinutesFromStations(arrival: RealtimeArrival, stationCount: Int): Int {
        // 1. 호선별 베이스 타임 찾기 (없으면 기본값 2.0)
        val baseTimePerStation = lineAverageTimes[arrival.subwayId] ?: 2.0

        // 2. 급행(Express/ITX 등) 여부 가중치 적용
        // btrainSttus: "일반", "급행", "ITX", "특급" 등 (API 데이터 형태에 무관하게 키워드 기반 감소)
        val expressMultiplier = when {
            arrival.btrainSttus.contains("특급") -> 0.6  // 40% 시간 단축
            arrival.btrainSttus.contains("급행") || arrival.btrainSttus.contains("ITX") -> 0.75 // 25% 단축
            else -> 1.0 // 일반
        }

        // 3. 시간대별 혼잡도 지연 가중치 적용
        val congestionMultiplier = getCongestionMultiplier()

        // 최종 수식: (역 개수 * 호선 기본 소요시간) * 급행 단축 * 혼잡 지연
        val rawEstimatedMinutes = (stationCount * baseTimePerStation) * expressMultiplier * congestionMultiplier

        // 반올림 처리하여 최소 1분은 보장
        return Math.max(1, Math.round(rawEstimatedMinutes).toInt())
    }
}
