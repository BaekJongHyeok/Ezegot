package com.jonghyeok.ezegot.di

import android.content.Context
import androidx.room.Room
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.db.AppDatabase
import com.jonghyeok.ezegot.db.FavoriteStationDao
import com.jonghyeok.ezegot.db.RecentSearchDao
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── OkHttp ───────────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // ─── Retrofit instances ───────────────────────────────────────────────────
    /** 역 기본 정보 (XML) – http://openapi.seoul.go.kr:8088 */
    @Provides
    @Singleton
    @Named("stationInfo")
    fun provideStationInfoRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://openapi.seoul.go.kr:8088/")
            .client(okHttpClient)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

    /** 실시간 도착 정보 (XML) – http://swopenapi.seoul.go.kr */
    @Provides
    @Singleton
    @Named("realtimeArrival")
    fun provideRealtimeArrivalRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://swopenapi.seoul.go.kr/")
            .client(okHttpClient)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

    /** 역 위경도 정보 (JSON) – https://t-data.seoul.go.kr */
    @Provides
    @Singleton
    @Named("stationLocation")
    fun provideStationLocationRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://t-data.seoul.go.kr/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
    // =========================================================================
    // 새로 추가될 기능 연동을 위한 공공데이터포털 Base Retrofit
    // =========================================================================
    /** 빠른 환승, 역 편의시설 정보 (JSON) - http://apis.data.go.kr */
    @Provides
    @Singleton
    @Named("dataGoKr")
    fun provideDataGoKrRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ─── API Services ─────────────────────────────────────────────────────────
    @Provides
    @Singleton
    @Named("stationInfoApi")
    fun provideStationInfoApi(@Named("stationInfo") retrofit: Retrofit): SubwayApiService =
        retrofit.create(SubwayApiService::class.java)

    @Provides
    @Singleton
    @Named("realtimeArrivalApi")
    fun provideRealtimeArrivalApi(@Named("realtimeArrival") retrofit: Retrofit): SubwayApiService =
        retrofit.create(SubwayApiService::class.java)

    @Provides
    @Singleton
    @Named("stationLocationApi")
    fun provideStationLocationApi(@Named("stationLocation") retrofit: Retrofit): SubwayApiService =
        retrofit.create(SubwayApiService::class.java)

    @Provides
    @Singleton
    @Named("extendedApi") // 빠른환승 및 편의시설 제공
    fun provideExtendedApi(@Named("dataGoKr") retrofit: Retrofit): SubwayApiService =
        retrofit.create(SubwayApiService::class.java)

    // ─── Room Database ────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ezegot_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoriteStationDao(db: AppDatabase): FavoriteStationDao = db.favoriteStationDao()

    @Provides
    @Singleton
    fun provideRecentSearchDao(db: AppDatabase): RecentSearchDao = db.recentSearchDao()

    @Provides
    @Singleton
    fun provideSubwayAlarmDao(db: AppDatabase): SubwayAlarmDao = db.subwayAlarmDao()
}
