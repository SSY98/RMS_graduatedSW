package com.project.rms.Image_recognition

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

//api
//The gson builder
var gson : Gson = GsonBuilder()
    .setLenient()
    .create()


//creating retrofit object
var retrofit = Retrofit.Builder()
    .baseUrl("http://e293-35-237-200-195.ngrok.io")
    .addConverterFactory(GsonConverterFactory.create(gson))
    .build()
//api


// api 인터페이스
interface retrofit_interface {

    // 이미지 보내기
    @Multipart
    @POST("/")
    fun sendFile(
        @Part file: MultipartBody.Part	// 우리가 넣을 데이터
    ): Call<ResponseData>


}