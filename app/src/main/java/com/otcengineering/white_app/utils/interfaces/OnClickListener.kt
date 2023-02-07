package com.otcengineering.white_app.utils.interfaces

import android.view.View

interface OnClickListener<T> {
    fun onItemClick(view: View, t: T)
}