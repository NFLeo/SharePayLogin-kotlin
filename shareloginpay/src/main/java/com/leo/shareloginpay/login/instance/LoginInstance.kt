package com.leo.shareloginpay.login.instance

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.leo.shareloginpay.login.LoginListener
import com.leo.shareloginpay.login.result.BaseToken

abstract class LoginInstance(activity: Activity, listener: LoginListener?, fetchUserInfo: Boolean) {

    abstract fun doLogin(activity: Activity, listener: LoginListener, fetchUserInfo: Boolean)

    abstract fun fetchUserInfo(token: BaseToken)

    abstract fun handleResult(requestCode: Int, resultCode: Int, data: Intent)

    abstract fun isInstall(context: Context): Boolean

    abstract fun recycle()
}
