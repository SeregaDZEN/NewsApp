package ru.seregabelyi.appnewsmy.network


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.seregabelyi.appnewsmy.models.GNewsResponse

interface GNewsApiService {
    @GET("search")
    fun getTopHeadlines(
        @Query("q") query: String = "tech",
        @Query("token") token: String,
        @Query("lang") lang: String = "en",
        @Query("country") country: String = "us"
    ): Call<GNewsResponse>
}



