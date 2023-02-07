package com.otcengineering.white_app.viewModel

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val AUTHORIZATION = "Authorization"
private const val PRIVATE_AUTHORIZATION_PREFIX = "A1 "
private const val PUBLIC_AUTHORIZATION = "A1 public_access:0"
const val HEADER_PUBLIC_API_KEY = "Public-Api"
const val HEADER_PUBLIC_API_VALUE = "True"
const val HEADER_NON_PROTO_BUFF_API_KEY = "NON_PROTO-Api"
const val HEADER_NON_PROTO_BUFF_API_VALUE = "True"
const val HEADER_PUBLIC_API = "$HEADER_PUBLIC_API_KEY: $HEADER_PUBLIC_API_VALUE"
const val HEADER_NON_PROTO_BUFF_API =
    "$HEADER_NON_PROTO_BUFF_API_KEY: $HEADER_NON_PROTO_BUFF_API_VALUE"

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authToken = Token.getToken()
        return chain.proceed(
            if (chain.request().isNotPublic() && authToken.isNotEmpty())
                chain.request()
                    .newBuilder()
                    .addPrivateAuthorization(authToken)
                    .build()
            else
                chain.request().newBuilder()
                    .addPublicAuthorization()
                    .build()
        )
    }
}

fun Request.isNotPublic() = (header(HEADER_PUBLIC_API_KEY)?.equals(HEADER_PUBLIC_API_VALUE) != true)

fun Request.hasNonProtoBuff() = (header(HEADER_NON_PROTO_BUFF_API_KEY)?.equals(
    HEADER_NON_PROTO_BUFF_API_VALUE
) == true)

fun Request.Builder.addPrivateAuthorization(authToken: String) = apply {
    val comToken = PRIVATE_AUTHORIZATION_PREFIX + authToken
    header(AUTHORIZATION, comToken)
}

fun Request.Builder.addPublicAuthorization() = apply {
    header(AUTHORIZATION, PUBLIC_AUTHORIZATION)
}
