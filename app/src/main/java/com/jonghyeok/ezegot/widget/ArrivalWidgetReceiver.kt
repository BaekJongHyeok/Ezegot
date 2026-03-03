package com.jonghyeok.ezegot.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.*
import java.util.concurrent.TimeUnit

class ArrivalWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: ArrivalWidget = ArrivalWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // 위젯이 처음 추가될 때: 주기 갱신 스케줄 + 최초 1회 즉시 데이터 로드
        schedulePeriodicUpdate(context)
        triggerImmediateUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork(WORKER_NAME)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 위젯 카파로드 브로드캐스트 수신 시 Glance가 SharedPrefs 데이터로
        // 다시 렌더링함 – WorkManager를 시작하지 않음!
        // (트리거: FavoriteRepository 또는 평글 Worker)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        private const val WORKER_NAME = "ArrivalWidgetUpdateWorker"

        /** 15분 주기 자동 갱신 WorkManager 스케줄 등록 */
        fun schedulePeriodicUpdate(context: Context) {
            val request = PeriodicWorkRequestBuilder<ArrivalWidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** 즉시 1회 갱신 – 네트워크 제약 없음 (Phase 1은 네트워크 불필요) */
        fun triggerImmediateUpdate(context: Context) {
            val request = OneTimeWorkRequestBuilder<ArrivalWidgetUpdateWorker>()
                .build()  // 제약 없음: 즉시 실행, Worker 내부에서 네트워크 상태 판단
            WorkManager.getInstance(context).enqueueUniqueWork(
                "ImmediateWidgetUpdate",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
