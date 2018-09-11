package com.leo.shareloginpay.login

import com.leo.shareloginpay.login.result.BaseToken

abstract class LoginListener {
    abstract fun loginSuccess(result: LoginResult)

    open fun beforeFetchUserInfo(token: BaseToken) {}

    abstract fun loginFailure(e: Exception)

    abstract fun loginCancel()
}