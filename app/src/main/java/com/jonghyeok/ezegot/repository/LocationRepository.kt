package com.jonghyeok.ezegot.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GPS 위치 Repository.
 *
 * ## 지연 단축 전략
 *
 * 기존: requestLocationUpdates(BALANCED) → 첫 fix까지 수 초 대기
 *
 * 개선:
 * 1. [getLastKnownLocation] – 즉시 반환 (캐시된 최근 위치, 0~50ms)
 *    → 첫 화면 렌더링에 활용
 * 2. [requestLocationUpdates] – HIGH_ACCURACY로 지속 갱신
 *    → 더 정확한 위치 확보 후 UI 업데이트
 *
 * 이 Two-phase 패턴은 Google Maps, Kakao Map 등 실 앱에서 사용하는 표준 방식이다.
 */
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // HIGH_ACCURACY: 빠른 첫 fix 획득 (BALANCED 대비 2~3배 빠름)
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        3_000L   // 3초 간격 – 근처 역 표시용으로 충분
    )
        .setMinUpdateIntervalMillis(1_500L)   // 최소 1.5초 간격
        .setWaitForAccurateLocation(false)    // 부정확해도 즉시 내보냄
        .build()

    // ── Phase 1: 즉시 위치 반환 ─────────────────────────────────
    /**
     * FusedLocationProvider의 캐시된 마지막 위치를 반환한다.
     * GPS가 꺼져있거나 처음 사용이면 null을 반환할 수 있다.
     *
     * 반환 속도: 0~50ms (네트워크/GPS 불필요)
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? =
        runCatching {
            if (!hasPermission()) return@runCatching null
            fusedClient.lastLocation.await()
        }.getOrNull()

    // ── Phase 2: 지속적 위치 갱신 ───────────────────────────────
    /**
     * 위치 갱신 Flow.
     * HIGH_ACCURACY + setWaitForAccurateLocation(false) 조합으로
     * 첫 콜백이 BALANCED 대비 빠르게 도착한다.
     */
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    private fun hasPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}