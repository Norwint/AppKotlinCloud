package com.otcengineering.white_app.viewModel

import com.google.protobuf.Message
import com.otc.alice.api.model.Shared.OTCResponse
import com.otc.alice.api.model.Shared.OTCStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class OtcCallback<T : Message>(private val clazz: Class<T>) : Callback<OTCResponse> {

    override fun onResponse(call: Call<OTCResponse>, response: Response<OTCResponse>) {
        val body = response.body()
        if (body == null) {
            error(OTCStatus.SERVER_ERROR)
            return
        }

        if (body.status != OTCStatus.SUCCESS) {
            error(body.status)
            return
        }

        if (clazz == OTCResponse::class.java) {
            response(body as T)
            return
        }
        val parsed = body.data.unpack(clazz)
        response(parsed)
    }

    override fun onFailure(call: Call<OTCResponse>, t: Throwable) {
        error(OTCStatus.SERVER_ERROR)
    }

    abstract fun response(response: T)
    abstract fun error(status: OTCStatus)
}