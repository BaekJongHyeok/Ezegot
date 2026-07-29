package com.jonghyeok.ezegot.viewModel

import app.cash.turbine.test
import com.jonghyeok.ezegot.MainDispatcherRule
import com.jonghyeok.ezegot.alarm.SubwayAlarmManager
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.repository.FavoriteRepository
import com.jonghyeok.ezegot.repository.StationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * 역 상세 화면 상태 관리 테스트.
 *
 * uiState 하나로 로딩·성공·에러를 표현하도록 통합했으므로,
 * 각 동작 후 uiState가 의도한 값으로 바뀌는지를 검증한다.
 */
class StationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var stationRepository: StationRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var alarmManager: SubwayAlarmManager
    private lateinit var alarmDao: SubwayAlarmDao

    @Before
    fun setUp() {
        stationRepository = mockk()
        favoriteRepository = mockk(relaxed = true)
        alarmManager = mockk(relaxed = true)
        alarmDao = mockk()

        // init 블록에서 활성 알람을 collect하므로 Flow를 반드시 stub해야 한다
        every { alarmDao.getActiveAlarms() } returns flowOf(emptyList())
    }

    private fun createViewModel() = StationViewModel(
        stationRepository = stationRepository,
        favoriteRepository = favoriteRepository,
        alarmManager = alarmManager,
        alarmDao = alarmDao
    )

    @Test
    fun `도착 정보를 불러오면 uiState에 반영되고 로딩이 끝난다`() = runTest {
        // given
        val arrivals = listOf(
            RealtimeArrival(subwayId = "1002", updnLine = "내선", bstatnNm = "성수"),
            RealtimeArrival(subwayId = "1002", updnLine = "외선", bstatnNm = "신도림")
        )
        coEvery { stationRepository.getRealtimeArrivalInfo("강남") } returns arrivals
        val viewModel = createViewModel()

        // when
        viewModel.loadArrivalInfo("강남")

        // then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(arrivals, state.arrivals)
            assertFalse("조회가 끝났으므로 로딩은 false여야 한다", state.isLoading)
        }
    }

    @Test
    fun `시간표 상하행이 모두 없으면 에러 메시지가 노출된다`() = runTest {
        // given: 서울 API와 TAGO 폴백이 모두 실패해 상·하행이 전부 null인 상황
        coEvery {
            stationRepository.getStationTimeTable(any(), any(), any())
        } returns Pair(null, null)
        val viewModel = createViewModel()

        // when
        viewModel.loadAdvancedStationInfo("강남", "2호선")

        // then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull("실패 시 errorMessage가 채워져야 한다", state.errorMessage)
        }
    }

    @Test
    fun `즐겨찾기를 토글하면 상태가 반전되고 저장이 호출된다`() = runTest {
        // given: 아직 즐겨찾기가 아닌 역
        coEvery { favoriteRepository.isFavorite(any()) } returns false
        val viewModel = createViewModel()
        viewModel.loadStationInfo("강남", "2호선")

        // when
        viewModel.toggleFavorite()

        // then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("토글 후 즐겨찾기 상태여야 한다", state.isFavorite)
            assertNull("즐겨찾기 토글은 에러를 만들지 않는다", state.errorMessage)
        }
        coVerify(exactly = 1) { favoriteRepository.addFavorite(any()) }
    }
}
