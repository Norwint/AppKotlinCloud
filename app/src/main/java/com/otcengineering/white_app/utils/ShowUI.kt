package com.otcengineering.white_app.utils

import android.app.Service
import android.os.Looper
import android.util.Log

fun Service.runOnMainThread(callback: () -> Unit) {
    android.os.Handler(Looper.getMainLooper()).post {
        try {
            callback()
        } catch (e: Exception) {
            Log.e("Service Main Thread",e.message,e)
        }
    }
}