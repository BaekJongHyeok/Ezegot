package com.jonghyeok.ezegot

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // WorkManager를 앱 시작 시 한 번 깨운다.
        //
        // 매니페스트에서 WorkManagerInitializer를 제거했다(Hilt WorkerFactory를 쓰려면
        // 필수다. 기본 초기화 프로바이더는 Configuration.Provider를 보지 않고
        // 빈 Configuration으로 초기화해 @HiltWorker를 만들지 못한다).
        //
        // 그래서 WorkManager는 처음 쓰이는 순간에야 초기화되는데, 예약을 되살리는
        // ForceStopRunnable도 그때 함께 돈다. 앱을 강제 종료하면 OS가 등록된 job을
        // 전부 지우므로, 앱을 다시 켜도 역 상세나 알림 탭에 들어가기 전까지는
        // 예약해 둔 도착 알림이 복구되지 않았다.
        //
        // getInstance는 Configuration.Provider 경로를 타므로 WorkerFactory는 그대로다.
        WorkManager.getInstance(this)
    }
}
