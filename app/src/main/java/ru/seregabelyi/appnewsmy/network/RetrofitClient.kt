package ru.seregabelyi.appnewsmy.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GNewsRetrofitClient {
    private const val BASE_URL = "https://gnews.io/api/v4/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: GNewsApiService by lazy {
        instance.create(GNewsApiService::class.java)
    }
}