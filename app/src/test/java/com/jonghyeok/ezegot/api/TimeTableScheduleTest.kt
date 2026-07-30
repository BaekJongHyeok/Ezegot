package com.jonghyeok.ezegot.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 급행 판정.
 *
 * 예전에는 "Y"만 보고 있어서 서울 API 응답에서는 한 번도 참이 나오지 않았다.
 * 실측 값은 "D"(급행)와 "G"(일반)뿐이다. 화면에서 급행 표식을 보고 확인하려면
 * 급행이 다니는 역까지 가야 해서, 판정 자체를 여기서 고정한다.
 */
class TimeTableScheduleTest {

    private fun schedule(express: String) =
        TimeTableSchedule(leftTime = "18:09:00", destination = "석남", express = express)

    @Test
    fun `서울 API의 D는 급행이다`() {
        assertTrue(schedule("D").isExpressTrain())
    }

    @Test
    fun `서울 API의 G는 일반이다`() {
        assertFalse(schedule("G").isExpressTrain())
    }

    @Test
    fun `TAGO 폴백의 Y도 급행으로 받는다`() {
        assertTrue(schedule("Y").isExpressTrain())
    }

    @Test
    fun `TAGO 폴백의 N과 빈 값은 일반이다`() {
        assertFalse(schedule("N").isExpressTrain())
        assertFalse(schedule("").isExpressTrain())
    }
}
