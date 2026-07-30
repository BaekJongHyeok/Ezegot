package com.jonghyeok.ezegot.repository

import android.content.Context
import com.jonghyeok.ezegot.db.FavoriteStationDao
import com.jonghyeok.ezegot.db.FavoriteStationEntity
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.widget.ArrivalWidget
import com.jonghyeok.ezegot.widget.ArrivalWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoriteDao: FavoriteStationDao
) {
    /** 즐겨찾기 목록 (Room Flow – DB 변경 시 자동 emit) */
    val favorites: Flow<List<BasicStationInfo>> = favoriteDao.getAll().map { list ->
        list.map { BasicStationInfo(it.stationName, it.lineNumber) }
    }

    suspend fun addFavorite(station: BasicStationInfo) {
        favoriteDao.insert(FavoriteStationEntity(stationName = station.stationName, lineNumber = station.lineNumber))
        notifyWidget()
    }

    suspend fun removeFavorite(station: BasicStationInfo) {
        favoriteDao.delete(station.stationName, station.lineNumber)
        notifyWidget()
    }

    suspend fun isFavorite(station: BasicStationInfo): Boolean =
        favoriteDao.exists(station.stationName, station.lineNumber)

    /**
     * 즐겨찾기 변경을 위젯에 2단계로 반영한다.
     *
     * Phase 1: 역 이름만 즉시 기록 (thread-safe, non-suspend) → 위젯이 곧바로 갱신됨
     * Phase 2: WorkManager가 API를 호출해 도착 정보를 채움
     */
    private suspend fun notifyWidget() {
        val updatedList = favoriteDao.getAllSync()
        ArrivalWidget.writeSnapshot(
            context,
            updatedList.take(ArrivalWidget.MAX_FAVORITES).map { it.stationName to it.lineNumber }
        )
        ArrivalWidgetReceiver.triggerImmediateUpdate(context)
    }
}
