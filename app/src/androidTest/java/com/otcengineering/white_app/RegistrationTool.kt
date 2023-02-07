package com.otcengineering.white_app

import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Welcome
import com.otcengineering.white_app.network.Endpoints
import com.otcengineering.white_app.network.utils.ApiCaller
import org.junit.Test
import org.junit.internal.runners.JUnit4ClassRunner
import org.junit.runner.RunWith
import java.util.*

@RunWith(JUnit4ClassRunner::class)
class RegistrationTool {
    var secret = ""
    private var token = ""

    private val scanner = Scanner(System.`in`)

    @Test
    fun userSuperRegistrator3000() {
        when (ask("Which step: 1- Register, 2- OTP Validate, 3- Second Registration?")) {
            "1" -> {
                val username = ask("Username:")
                val password = ask("Password:")
                val phoneNo = ask("Phone Number:")
                val email = ask("Email:")
                val registerResponse = register(username, password, phoneNo, email)
                apply(registerResponse) {
                    token = it.apiToken
                    val otp = ask("OTP:")
                    enablePhone(username, otp)
                    val sn = ask("Serial Number:")
                    apply(secondRegister(username, sn)) {
                        println("Success!")
                    }
                }
            }
            "2" -> {
                val username = ask("Username:")
                val password = ask("Password:")
                val otp = ask("OTP:")
                enablePhone(username, otp)
                apply(login(username, password)) {
                    val sn = ask("Serial Number:")
                    apply(secondRegister(username, sn)) {
                        println("Success!")
                    }
                }
            }
            "3" -> {
                val username = ask("Username:")
                val password = ask("Password:")
                apply(login(username, password)) {
                    val sn = ask("Serial Number:")
                    apply(secondRegister(username, sn)) {
                        println("Success!")
                    }
                }
            }
        }
    }

    private fun register(username: String, password: String, phoneNo: String, email: String) : APIMaybe<Welcome.LoginResponse> {
        val request = Welcome.UserRegistration.newBuilder()
        request.username = username
        request.password = password
        request.mobileIMEI = TestConstants.imei
        request.mobilePhoneNumber = phoneNo
        request.email = email

        val rsp = ApiCaller.doCall(Endpoints.REGISTER, request.build(), Welcome.LoginResponse::class.java)
        return APIMaybe.pack(rsp)
    }

    private fun secondRegister(username: String, sn: String) : APIMaybe<Shared.OTCResponse> {
        val request = TestFunctions.getUserProfile(username, sn)

        val resp = ApiCaller.doCall(Endpoints.PROFILE, token.toByteArray(), request.build(), Shared.OTCResponse::class.java)
        println(resp.status)

        return APIMaybe.pack(resp)
    }

    private fun login(username: String, password: String) : APIMaybe<Welcome.LoginResponse> {
        val request = Welcome.Login.newBuilder()
        request.username = username
        request.password = password
        request.mobileIMEI = TestConstants.imei

        val resp = ApiCaller.doCall(Endpoints.LOGIN, request.build(), Welcome.LoginResponse::class.java)
        return APIMaybe.pack(resp)
    }

    private fun enablePhone(username: String, secret: String): Shared.OTCResponse {
        val request = Welcome.UserActivation.newBuilder()
        request.username = username
        request.secret = secret

        return ApiCaller.doCall(Endpoints.ACTIVATE, request.build(), Shared.OTCResponse::class.java)
    }

    private fun ask(request: String) : String {
        println("\t$request")
        return scanner.nextLine()
    }
}