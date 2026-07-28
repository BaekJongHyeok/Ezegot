package com.jonghyeok.ezegot.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.db.FavoriteStationDao
import com.jonghyeok.ezegot.di.ApiKeys
import com.jonghyeok.ezegot.dto.RealtimeArrival
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Named

/**
 * 위젯의 실시간 도착 정보를 갱신하는 Worker.
 *
 * 즐겨찾기 변경 시 즉시 1회, 그리고 15분 주기로 실행된다
 * ([ArrivalWidgetReceiver] 참고).
 */
@HiltWorker
class ArrivalWidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val favoriteDao: FavoriteStationDao,
    @Named("realtimeArrivalApi") private val realtimeApi: SubwayApiService,
    private val apiKeys: ApiKeys
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val favorites = favoriteDao.getAllSync()

            if (favorites.isEmpty()) {
                // 즐겨찾기 없으면 빈 상태로 위젯 갱신
                ArrivalWidget.updateWidgets(applicationContext, emptyList())
                return Result.success()
            }

            // 각 즐겨찾기 역의 실시간 도착 정보를 병렬 호출
            val results: List<FavoriteArrivalInfo> = withContext(Dispatchers.IO) {
                favorites.map { fav ->
                    async {
                        val arrivals = runCatching {
                            realtimeApi.getStationArrivalInfo(
                                apiKeys.seoulOpen,
                                fav.stationName
                            ).arrivals
                        }.getOrDefault(emptyList())
                        FavoriteArrivalInfo(fav.stationName, fav.lineNumber, arrivals)
                    }
                }.awaitAll()
            }

            ArrivalWidget.updateWidgets(applicationContext, results)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

data class FavoriteArrivalInfo(
    val stationName: String,
    val lineNumber: String,
    val arrivals: List<RealtimeArrival>
)
