package com.jonghyeok.ezegot.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.db.AppDatabase
import com.jonghyeok.ezegot.dto.RealtimeArrival
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class ArrivalWidgetUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Room DB에서 즐겨찾기 목록 조회 (Hilt 없이 직접 인스턴스화)
            val db = AppDatabase.getDatabase(context)
            val favorites = db.favoriteStationDao().getAllSync()

            if (favorites.isEmpty()) {
                // 즐겨찾기 없으면 빈 상태로 위젯 갱신
                ArrivalWidget.updateWidgets(context, emptyList())
                return Result.success()
            }

            // 2. Retrofit 직접 생성
            val api = buildRealtimeApi()

            // 3. 각 즐겨찾기 역의 실시간 도착 정보를 병렬 호출
            val results: List<FavoriteArrivalInfo> = withContext(Dispatchers.IO) {
                favorites.map { fav ->
                    async {
                        val arrivals = runCatching {
                            api.getStationArrivalInfo(fav.stationName).arrivals
                        }.getOrDefault(emptyList())
                        FavoriteArrivalInfo(fav.stationName, fav.lineNumber, arrivals)
                    }
                }.awaitAll()
            }

            // 4. Glance 위젯 갱신
            ArrivalWidget.updateWidgets(context, results)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun buildRealtimeApi(): SubwayApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        val okHttp = OkHttpClient.Builder().addInterceptor(logging).build()
        return Retrofit.Builder()
            .baseUrl("http://swopenapi.seoul.go.kr/")
            .client(okHttp)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(SubwayApiService::class.java)
    }
}

data class FavoriteArrivalInfo(
    val stationName: String,
    val lineNumber: String,
    val arrivals: List<RealtimeArrival>
)
