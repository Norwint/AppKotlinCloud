package com.otcengineering.white_app.utils.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by OTC_100 on 6/4/2018.
 */

public interface NetworkAPI {
    @GET("firmware/")
    Call<ResponseBody> downloadFirmwareFromServer();
}
