package com.jonghyeok.ezegot.repository

import android.content.Context
import com.jonghyeok.ezegot.db.FavoriteStationDao
import com.jonghyeok.ezegot.db.FavoriteStationEntity
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.widget.ArrivalWidget
import com.jonghyeok.ezegot.widget.ArrivalWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoriteDao: FavoriteStationDao,
    @Named("realtimeArrivalApi") private val realtimeApi: SubwayApiService
) {
    /** 즐겨찾기 목록 (Room Flow – DB 변경 시 자동 emit) */
    val favorites: Flow<List<BasicStationInfo>> = favoriteDao.getAll().map { list ->
        list.map { BasicStationInfo(it.stationName, it.lineNumber) }
    }

    suspend fun addFavorite(station: BasicStationInfo) {
        favoriteDao.insert(FavoriteStationEntity(stationName = station.stationName, lineNumber = station.lineNumber))
        // Phase 1: 즉시 위젯에 역 이름 반영 (thread-safe, non-suspend)
        val updatedList = favoriteDao.getAllSync()
        ArrivalWidget.writeSnapshot(
            context,
            updatedList.take(ArrivalWidget.MAX_FAVORITES).map { it.stationName to it.lineNumber }
        )
        // Phase 2: API 호출 후 도착 정보 반영 (WorkManager)
        ArrivalWidgetReceiver.triggerImmediateUpdate(context)
    }

    suspend fun removeFavorite(station: BasicStationInfo) {
        favoriteDao.delete(station.stationName, station.lineNumber)
        // Phase 1: 즉시 위젯에 역 이름 반영 (thread-safe, non-suspend)
        val updatedList = favoriteDao.getAllSync()
        ArrivalWidget.writeSnapshot(
            context,
            updatedList.take(ArrivalWidget.MAX_FAVORITES).map { it.stationName to it.lineNumber }
        )
        // Phase 2: API 호출 후 도착 정보 반영 (WorkManager)
        ArrivalWidgetReceiver.triggerImmediateUpdate(context)
    }

    suspend fun isFavorite(station: BasicStationInfo): Boolean =
        favoriteDao.exists(station.stationName, station.lineNumber)

    suspend fun getRealtimeArrival(stationName: String): List<RealtimeArrival> =
        runCatching {
            withContext(Dispatchers.IO) { realtimeApi.getStationArrivalInfo(stationName).arrivals }
        }.getOrDefault(emptyList())
}
