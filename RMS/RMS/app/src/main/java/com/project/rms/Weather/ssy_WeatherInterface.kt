package com.project.rms.Weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ssy_WeatherInterface {
    @GET("getVilageFcst?serviceKey=%2FmgS9kKXxFGa56eydxQLF0PJ%2FTxesM%2B8FDVxlckG32aYw0OGSaaz61GOSNinvpLoc8DCSzDTo3QOTmR6q2oMZw%3D%3D")
    fun GetWeather(
        @Query("dataType") data_type : String,
        @Query("numOfRows") num_of_rows : Int,
        @Query("pageNo") page_no : Int,
        @Query("base_date") base_date : String,
        @Query("base_time") base_time : String,
        @Query("nx") nx : String,
        @Query("ny") ny : String
    ): Call<ssy_WHEATHER>
}