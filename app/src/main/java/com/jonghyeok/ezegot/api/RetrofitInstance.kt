package com.jonghyeok.ezegot.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://openapi.seoul.go.kr:8088/" // 지하철 역 정보 URL
    private const val BASE_URL2 = "http://swopenapi.seoul.go.kr/" // 실시간 지하철 도착 정보 URL
    private const val BASE_URL3 = "https://t-data.seoul.go.kr/" // 지하철역 위경도 정보 URL

    val api: SubwayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(SubwayApiService::class.java)
    }

    val api2: SubwayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(SubwayApiService::class.java)
    }

    val api3: SubwayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL3)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SubwayApiService::class.java)
    }
}