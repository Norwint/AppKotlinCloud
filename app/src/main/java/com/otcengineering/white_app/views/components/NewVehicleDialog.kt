package com.otcengineering.white_app.views.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import androidx.core.content.ContentProviderCompat.requireContext
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Vehicle
import com.otcengineering.white_app.R
import com.otcengineering.white_app.databinding.DialogNewVehicleBinding
import com.otcengineering.white_app.viewModel.Network
import com.otcengineering.white_app.viewModel.OtcCallback
import com.otcengineering.white_app.viewModel.Token
import com.otcengineering.white_app.views.activity.VerificationActivity

class NewVehicleDialog(context: Context) : Dialog(context, android.R.style.Theme) {

    private val binding: DialogNewVehicleBinding by lazy {
        DialogNewVehicleBinding.inflate(layoutInflater)
    }

    var callback : (String) -> Unit = {}

    fun showOnMainThread() {
        Handler(Looper.getMainLooper()).post {
            try {
                show()
            } catch (e: Exception) {
                Log.e("AlertDialog", "Exception", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(R.color.dialog_transparents)

        binding.acceptButton.setOnClickListener {
            Log.d("alert", "pressed")

            val vin = binding.vin.text.toString()
            callback(vin)

            dismiss()

        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

    }

}