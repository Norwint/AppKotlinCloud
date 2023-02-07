package com.otcengineering.white_app.views.activity


import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.otcengineering.white_app.R
import com.otcengineering.white_app.databinding.ActivityWelcomeBinding
import com.otcengineering.white_app.views.fragment.LoginFragment
import com.otcengineering.white_app.views.fragment.SignUpFragment

class WelcomeActivity : AppCompatActivity() {

    private val binding: ActivityWelcomeBinding by lazy { ActivityWelcomeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setFragment(LoginFragment())

        binding.signUp.setOnClickListener {
            binding.imageSignup.visibility = View.VISIBLE
            binding.imageLogin.visibility = View.INVISIBLE
            binding.textSignup.typeface = Typeface.DEFAULT_BOLD
            binding.textLogin.typeface = Typeface.DEFAULT
            setFragment(SignUpFragment())
        }

        binding.login.setOnClickListener {
            binding.imageSignup.visibility = View.INVISIBLE
            binding.imageLogin.visibility = View.VISIBLE
            binding.textSignup.typeface = Typeface.DEFAULT
            binding.textLogin.typeface = Typeface.DEFAULT_BOLD
            setFragment(LoginFragment())
        }
    }

    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}