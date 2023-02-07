package com.otcengineering.white_app

import com.google.protobuf.Message

class APIMaybe<T : Message> {
    private var t : T? = null

    fun canUnpack() = t != null
    fun unpack() = t!!
    companion object {
        fun <T : Message> pack(value: T?) : APIMaybe<T> {
            val maybe = APIMaybe<T>()
            maybe.t = value
            return maybe
        }
    }
}

fun <T : Message> apply(response: APIMaybe<T>, callback: (T) -> Unit) {
    if (response.canUnpack()) {
        callback(response.unpack())
    }
}