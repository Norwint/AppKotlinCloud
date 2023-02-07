package com.otcengineering.white_app.viewModel

import android.content.Context
import com.otcengineering.white_app.utils.MySharedPreferences

object Token {
    private const val TOKEN = "user_token"

    fun putToken(token: String) {
        val msp = MySharedPreferences.create()
        msp.putString(TOKEN, token)
    }

    fun getToken(): String {
        val msp = MySharedPreferences.create()
        return msp.getString(TOKEN)
    }
}