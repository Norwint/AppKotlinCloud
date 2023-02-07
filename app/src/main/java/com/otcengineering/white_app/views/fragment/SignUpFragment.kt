package com.otcengineering.white_app.views.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Welcome
import com.otcengineering.white_app.R
import com.otcengineering.white_app.databinding.FragmentSignupBinding
import com.otcengineering.white_app.utils.Common
import com.otcengineering.white_app.utils.MyProgressDialog
import com.otcengineering.white_app.utils.Preferences
import com.otcengineering.white_app.viewModel.Network
import com.otcengineering.white_app.viewModel.OtcCallback
import com.otcengineering.white_app.viewModel.getIMEI
import com.otcengineering.white_app.views.activity.VerificationActivity

class SignUpFragment: Fragment() {

    private val binding: FragmentSignupBinding by lazy {
        FragmentSignupBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.fragment = this

        binding.registerButton.setOnClickListener {

            val phoneNumber = "+" + binding.ccp.selectedCountryCode + binding.mobile.text.toString()
            Common.sharedPreferences.putString(Preferences.phoneNumber, phoneNumber)

            MyProgressDialog.create(requireContext())
            MyProgressDialog.show()

            val body = Welcome.UserRegistrationV2.newBuilder()
                .setPhoneNumber(Common.sharedPreferences.getString(Preferences.phoneNumber))
                .setCountryId(3)
                .setMobileIMEI(getIMEI())
                .build()
            Network.welcome.register(body).enqueue(object: OtcCallback<Shared.OTCResponse>(Shared.OTCResponse::class.java) {
                override fun response(response: Shared.OTCResponse) {
                    Log.d("Register", "Response!")
                    MyProgressDialog.hide()
                    VerificationActivity.newInstance(requireContext())
                }

                override fun error(status: Shared.OTCStatus) {
                    MyProgressDialog.hide()
                    Log.e("Register", "Error!")
                    binding.txtError1.visibility = View.VISIBLE
                    binding.txtError1.text = getString(R.string.error_try_again)
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