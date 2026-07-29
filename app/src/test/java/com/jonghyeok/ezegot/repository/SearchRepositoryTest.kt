package com.jonghyeok.ezegot.repository

import app.cash.turbine.test
import com.jonghyeok.ezegot.db.RecentSearchDao
import com.jonghyeok.ezegot.db.RecentSearchEntity
import io.mockk.coJustRun
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 최근 검색 Repository 테스트.
 *
 * FavoriteRepository 대신 이쪽을 고른 이유는, FavoriteRepository의 변경 메서드가
 * 위젯 갱신(SharedPreferences, AppWidgetManager)을 호출해 순수 JVM 테스트로
 * 검증할 수 없기 때문이다. SearchRepository는 Android 의존이 없다.
 */
class SearchRepositoryTest {

    private val dao: RecentSearchDao = mockk()

    @Test
    fun `Room 엔티티가 화면용 모델로 변환되어 방출된다`() = runTest {
        // given
        every { dao.getAll() } returns flowOf(
            listOf(
                RecentSearchEntity(stationName = "강남", lineNumber = "02호선"),
                RecentSearchEntity(stationName = "서울역", lineNumber = "01호선")
            )
        )
        val repository = SearchRepository(dao)

        // when / then
        repository.recentSearches.test {
            val items = awaitItem()

            assertEquals(2, items.size)
            assertEquals("강남", items[0].stationName)
            assertEquals("02호선", items[0].lineNumber)
            assertEquals("서울역", items[1].stationName)
            awaitComplete()
        }
    }

    @Test
    fun `같은 역을 다시 검색하면 기존 항목을 지운 뒤 새로 넣는다`() = runTest {
        // given: 중복 제거 후 최신화가 목적이므로 삭제가 삽입보다 먼저여야 한다
        every { dao.getAll() } returns flowOf(emptyList())
        coJustRun { dao.deleteOne(any(), any()) }
        coJustRun { dao.insert(any()) }
        val repository = SearchRepository(dao)

        // when
        repository.saveRecentSearch("강남", "02호선")

        // then
        coVerifyOrder {
            dao.deleteOne("강남", "02호선")
            dao.insert(any())
        }
    }
}
