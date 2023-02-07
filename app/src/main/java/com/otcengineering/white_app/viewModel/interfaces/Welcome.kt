package com.otcengineering.white_app.viewModel.interfaces

import com.otc.alice.api.model.Shared.OTCResponse
import com.otc.alice.api.model.Welcome
import com.otc.alice.api.model.Vehicle
import com.otcengineering.white_app.viewModel.HEADER_PUBLIC_API
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.GET

interface Welcome {
    @Headers(HEADER_PUBLIC_API)
    @POST("v2/welcome/user/login")
    fun login(@Body body: Welcome.LoginV2): retrofit2.Call<OTCResponse>

    @Headers(HEADER_PUBLIC_API)
    @POST("v2/welcome/user/register")
    fun register(@Body body: Welcome.UserRegistrationV2): retrofit2.Call<OTCResponse>

    @Headers(HEADER_PUBLIC_API)
    @PUT("v2/welcome/user/otp-check")
    fun otpCheck(@Body body: Welcome.UserOTPCheck): retrofit2.Call<OTCResponse>
}

interface Vehicle {

    @PUT("v1/vehicle/link")
    fun linkVehicle(@Body body: Vehicle.VehicleLink): retrofit2.Call<OTCResponse>

    @PUT("v1/vehicle/unlink")
    fun unlinkVehicle(@Body body: Vehicle.VehicleLink): retrofit2.Call<OTCResponse>

    @GET("v1/vehicle")
    fun vehicles(): retrofit2.Call<OTCResponse>
}