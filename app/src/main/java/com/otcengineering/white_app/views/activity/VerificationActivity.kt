package com.otcengineering.white_app.views.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Welcome
import com.otcengineering.white_app.R
import com.otcengineering.white_app.activities.Home2Activity
import com.otcengineering.white_app.databinding.ActivityVerificationBinding
import com.otcengineering.white_app.utils.*
import com.otcengineering.white_app.viewModel.Network
import com.otcengineering.white_app.viewModel.OtcCallback

class VerificationActivity : AppCompatActivity() {

    private val binding: ActivityVerificationBinding by lazy { ActivityVerificationBinding.inflate(layoutInflater) }

    var OTP = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.textSms.text = getString(R.string.sms_verification, Common.sharedPreferences.getString(Preferences.phoneNumber))

        binding.otpOneEdtxt.addTextChangedListener(GenericTextWatcher(binding.otpOneEdtxt, binding.otpTwoEdtxt))
        binding.otpTwoEdtxt.addTextChangedListener(GenericTextWatcher(binding.otpTwoEdtxt, binding.otpThreeEdtxt))
        binding.otpThreeEdtxt.addTextChangedListener(GenericTextWatcher(binding.otpThreeEdtxt, binding.otpFourEdtxt))
        binding.otpFourEdtxt.addTextChangedListener(GenericTextWatcher(binding.otpFourEdtxt, binding.otp5Edtxt))
        binding.otp5Edtxt.addTextChangedListener(GenericTextWatcher(binding.otp5Edtxt, binding.otp6Edtxt))
        binding.otp6Edtxt.addTextChangedListener(GenericTextWatcher(binding.otp6Edtxt, binding.otp6Edtxt))

        binding.otpOneEdtxt.setOnKeyListener(GenericKeyEvent(binding.otpOneEdtxt, null))
        binding.otpTwoEdtxt.setOnKeyListener(GenericKeyEvent(binding.otpTwoEdtxt, binding.otpOneEdtxt))
        binding.otpThreeEdtxt.setOnKeyListener(GenericKeyEvent(binding.otpThreeEdtxt, binding.otpTwoEdtxt))
        binding.otpFourEdtxt.setOnKeyListener(GenericKeyEvent(binding.otpFourEdtxt, binding.otpThreeEdtxt))
        binding.otp5Edtxt.setOnKeyListener(GenericKeyEvent(binding.otp5Edtxt, binding.otpFourEdtxt))
        binding.otp6Edtxt.setOnKeyListener(GenericKeyEvent(binding.otp6Edtxt, binding.otp5Edtxt))

        binding.otpButton.setOnClickListener {

            MyProgressDialog.create(this)
            MyProgressDialog.show()

            OTP = binding.otpOneEdtxt.text.toString() + binding.otpTwoEdtxt.text.toString() + binding.otpThreeEdtxt.text.toString() +
                    binding.otpFourEdtxt.text.toString() + binding.otp5Edtxt.text.toString() + binding.otp6Edtxt.text.toString()

            Log.d("otp",OTP)
            val body = Welcome.UserOTPCheck.newBuilder().
                setPhoneNumber(Common.sharedPreferences.getString(Preferences.phoneNumber)).
                setOneTimePassword(OTP).
                setCountryId(3).
                build()

            Network.welcome.otpCheck(body).enqueue(object: OtcCallback<Shared.OTCResponse>(Shared.OTCResponse::class.java) {
                override fun response(response: Shared.OTCResponse) {
                    MyProgressDialog.hide()
                    Log.e("OTP", "Response!")
                    startActivity(Intent(this@VerificationActivity, HomeActivity::class.java))
                }

                override fun error(status: Shared.OTCStatus) {
                    MyProgressDialog.hide()
                    Log.e("OTP", "Error!")
                }

            })
        }
    }

    companion object {
        fun newInstance(ctx: Context) = ctx.startActivity(Intent(ctx, VerificationActivity::class.java))
    }

}