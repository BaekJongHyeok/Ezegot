package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.db.RecentSearchDao
import com.jonghyeok.ezegot.db.RecentSearchEntity
import com.jonghyeok.ezegot.dto.BasicStationInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 검색 관련 Repository.
 *
 * 전체 역 목록(getAllStations)은 MainRepository의 캐시를 사용해야 하므로
 * SearchViewModel에서 MainRepository를 함께 주입받아 사용한다.
 * 이 Repository는 최근 검색 CRUD만 담당한다 (단일 책임 원칙).
 */
@Singleton
class SearchRepository @Inject constructor(
    private val recentSearchDao: RecentSearchDao
) {
    /** 최근 검색 목록 – Room Flow로 자동 갱신 (최신순 20개) */
    val recentSearches: Flow<List<BasicStationInfo>> = recentSearchDao.getAll().map { list ->
        list.map { BasicStationInfo(it.stationName, it.lineNumber) }
    }

    /** 검색어 저장 (동일 항목 있으면 먼저 삭제 후 재삽입 → 최신화) */
    suspend fun saveRecentSearch(stationName: String, lineNumber: String) {
        recentSearchDao.deleteOne(stationName, lineNumber)
        recentSearchDao.insert(RecentSearchEntity(stationName = stationName, lineNumber = lineNumber))
    }

    suspend fun deleteRecentSearch(stationName: String, lineNumber: String) {
        recentSearchDao.deleteOne(stationName, lineNumber)
    }
}