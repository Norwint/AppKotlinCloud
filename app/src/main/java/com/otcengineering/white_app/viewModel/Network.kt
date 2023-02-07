package com.otcengineering.white_app.viewModel

import com.otcengineering.white_app.viewModel.interfaces.Vehicle
import com.otcengineering.white_app.viewModel.interfaces.Welcome
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.concurrent.TimeUnit

object Network {
    var BASE_URL = "http://connectech.otc010.com/"

    private val client : OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(AuthInterceptor())
            .build()
    }

    private val retrofit : Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(ProtoConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
    }

    val welcome : Welcome by lazy {
        retrofit.create(Welcome::class.java)
    }
    val vehicle : Vehicle by lazy {
        retrofit.create(Vehicle::class.java)
    }
}


fun getIMEI(): String {
    return "000000000000006"
}