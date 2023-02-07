package com.otcengineering.white_app.views.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Welcome
import com.otcengineering.white_app.R
import com.otcengineering.white_app.databinding.FragmentLoginBinding
import com.otcengineering.white_app.utils.Common
import com.otcengineering.white_app.utils.MyProgressDialog
import com.otcengineering.white_app.utils.Preferences
import com.otcengineering.white_app.viewModel.Network
import com.otcengineering.white_app.viewModel.OtcCallback
import com.otcengineering.white_app.viewModel.Token
import com.otcengineering.white_app.viewModel.getIMEI
import com.otcengineering.white_app.views.activity.VerificationActivity

class LoginFragment : Fragment() {

    private val binding: FragmentLoginBinding by lazy {
        FragmentLoginBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.fragment = this

        binding.loginButton.setOnClickListener {

            MyProgressDialog.create(requireContext())
            MyProgressDialog.show()

            val phoneNumber = "+" + binding.ccp.selectedCountryCode + binding.mobile.text.toString()
            Common.sharedPreferences.putString(Preferences.phoneNumber, phoneNumber)

            val body = Welcome.LoginV2.newBuilder()
                .setPhoneNumber(Common.sharedPreferences.getString(Preferences.phoneNumber))
                .setCountryId(3)
                .setMobileIMEI(getIMEI())
                .build()

            Network.welcome.login(body).enqueue(object: OtcCallback<Welcome.LoginResponseV2>(Welcome.LoginResponseV2::class.java) {
                override fun response(response: Welcome.LoginResponseV2) {
                    MyProgressDialog.hide()
                    Log.d("Login", "Response!")
                    Token.putToken(response.apiToken)
                    VerificationActivity.newInstance(requireContext())
                }

                override fun error(status: Shared.OTCStatus) {
                    MyProgressDialog.hide()
                    Log.e("Login", "Error!")
                    binding.txtError1.visibility = View.VISIBLE
                    binding.txtError1.text = getString(R.string.error_phone)
                    changeColor(binding.mobile, resources.getColor(R.color.error))
                    binding.mobile.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_red_24dp, 0)
                    binding.invalidateAll()
                }
            })
        }
        return binding.root
    }

    fun changeColor(editText: EditText, color: Int) {
        editText.backgroundTintList = ColorStateList.valueOf(color)
    }

}