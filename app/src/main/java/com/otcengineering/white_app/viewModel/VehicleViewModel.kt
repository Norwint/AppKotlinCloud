package com.otcengineering.white_app.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import androidx.databinding.ObservableArrayList
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Welcome
import com.otcengineering.white_app.R
import com.otcengineering.white_app.data.Vehicle
import com.otcengineering.white_app.utils.Common
import com.otcengineering.white_app.utils.MyProgressDialog
import com.otcengineering.white_app.utils.Preferences
import com.otcengineering.white_app.views.activity.VerificationActivity
import kotlin.random.Random

class VehicleViewModel (private val ctx: Context) {

    var listVehicles = ObservableArrayList<Vehicle>()

    init {
        getVehicles()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun createDummy() {

        for (i in 0L until 1L) {

            val listVehicle = Vehicle(0L, "Vehicle 1", ctx.getDrawable(R.drawable.car_otc))

            listVehicles.add(listVehicle)
        }

    }

    fun getVehicles() {
        Network.vehicle.vehicles().enqueue(object: OtcCallback<Shared.OTCResponse>(Shared.OTCResponse::class.java) {
            override fun response(response: Shared.OTCResponse) {
                Log.d("Vehicle", "Response!")

                val rsp = response.data.unpack(com.otc.alice.api.model.Vehicle.VehicleResponse::class.java)

                Common.sharedPreferences.putString(Preferences.vehicleName, rsp.getVehicles(0).model)

                for (dtc in rsp.vehiclesList) {

                    val listVehicle = Vehicle(dtc.id, dtc.model, ctx.getDrawable(R.drawable.car_otc))

                    listVehicles.add(listVehicle)
                }


            }

            override fun error(status: Shared.OTCStatus) {
                Log.e("Vehicle", "Error!")

            }
        })
    }


}