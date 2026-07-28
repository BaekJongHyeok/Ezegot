package com.jonghyeok.ezegot.di

/**
 * 외부 공공 API 키 묶음.
 *
 * 실제 값은 `local.properties` → `BuildConfig` 경로로 들어오며,
 * [AppModule.provideApiKeys]에서 한 번만 조립된다.
 * 따라서 `BuildConfig`를 참조하는 곳은 `AppModule` 하나로 유지된다.
 *
 * Repository는 키 문자열을 하나씩 주입받는 대신 이 객체 하나만 주입받는다.
 * 테스트에서는 임의의 값으로 채운 인스턴스를 넘기면 된다.
 */
data class ApiKeys(
    /** 서울 열린데이터광장 – 전체 역 목록, 실시간 도착 정보 */
    val seoulOpen: String,
    /** 서울 열린데이터광장 – 역별 시간표 (첫차·막차) */
    val seoulTimetable: String,
    /** 서울 교통 데이터(t-data) – 역 위경도 */
    val taims: String,
    /** 공공데이터포털 TAGO – 시간표 폴백. 이미 URL 인코딩된 값이다. */
    val tago: String
)
