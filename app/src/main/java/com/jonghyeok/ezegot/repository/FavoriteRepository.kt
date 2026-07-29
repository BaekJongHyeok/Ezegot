package com.jonghyeok.ezegot.repository

import android.content.Context
import com.jonghyeok.ezegot.db.FavoriteStationDao
import com.jonghyeok.ezegot.db.FavoriteStationEntity
import com.jonghyeok.ezegot.dto.FavoriteStation
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
    /** 즐겨찾기 목록 (Room Flow – DB 변경 시 자동 emit). 역이 저장 단위다. */
    val favorites: Flow<List<FavoriteStation>> = favoriteDao.getAll().map { list ->
        list.map { FavoriteStation(it.stationName, it.lineNumber) }
    }

    /** 이 역·노선을 담아뒀는지 */
    fun isFavorite(stationName: String, lineNumber: String): Flow<Boolean> =
        favoriteDao.isFavorite(stationName, lineNumber)

    suspend fun addFavorite(favorite: FavoriteStation) {
        favoriteDao.insert(
            FavoriteStationEntity(
                stationName = favorite.stationName,
                lineNumber = favorite.lineNumber
            )
        )
        notifyWidget()
    }

    suspend fun removeFavorite(favorite: FavoriteStation) {
        favoriteDao.delete(favorite.stationName, favorite.lineNumber)
        notifyWidget()
    }

    suspend fun exists(favorite: FavoriteStation): Boolean =
        favoriteDao.exists(favorite.stationName, favorite.lineNumber)

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
