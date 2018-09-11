package com.leo.shareloginpay.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.leo.shareloginpay.ShareActivity

import com.leo.shareloginpay.ShareLogger
import com.leo.shareloginpay.ShareLoginManager
import com.leo.shareloginpay.login.instance.LoginInstance
import com.leo.shareloginpay.login.instance.QQLoginInstance
import com.leo.shareloginpay.login.instance.WeiboLoginInstance
import com.leo.shareloginpay.login.instance.WxLoginInstance
import com.leo.shareloginpay.login.result.BaseToken

object LoginUtil {

    private var mLoginInstance: LoginInstance? = null
    private var mLoginListener: LoginListener? = null
    private var mPlatform: LoginPlatform? = null
    private var isFetchUserInfo: Boolean = false

    val TYPE = 799

    @JvmOverloads
    fun login(context: Context, platform: LoginPlatform,
              listener: LoginListener, fetchUserInfo: Boolean = true) {
        mPlatform = platform
        mLoginListener = LoginListenerProxy(listener)
        isFetchUserInfo = fetchUserInfo
        ShareActivity.newInstance(context, TYPE)
    }

    internal fun action(activity: Activity) {
        when (mPlatform) {
            LoginPlatform.QQ -> mLoginInstance = QQLoginInstance(activity, mLoginListener, isFetchUserInfo)
            LoginPlatform.WeiBO -> mLoginInstance = WeiboLoginInstance(activity, mLoginListener, ShareLoginManager.CONFIG.weiboId!!,
                    ShareLoginManager.CONFIG.weiboRedirectUrl, ShareLoginManager.CONFIG.weiboScope, isFetchUserInfo)
            LoginPlatform.WX -> mLoginInstance = WxLoginInstance(activity, mLoginListener!!, isFetchUserInfo)
            else -> {
                mLoginListener!!.loginFailure(Exception(ShareLogger.INFO.UNKNOW_PLATFORM))
                activity.finish()
            }
        }
        mLoginInstance!!.doLogin(activity, mLoginListener!!, isFetchUserInfo)
    }

    internal fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (mLoginInstance != null) {
            mLoginInstance!!.handleResult(requestCode, resultCode, data)
        }
    }

    fun recycle() {
        if (mLoginInstance != null) {
            mLoginInstance!!.recycle()
        }
        mLoginInstance = null
        mLoginListener = null
        mPlatform = LoginPlatform.Default
        isFetchUserInfo = false
    }

    private class LoginListenerProxy internal constructor(private val mListener: LoginListener) : LoginListener() {

        override fun loginSuccess(result: LoginResult) {
            ShareLogger.i(ShareLogger.INFO.LOGIN_SUCCESS)
            mListener.loginSuccess(result)
            recycle()
        }

        override fun loginFailure(e: Exception) {
            ShareLogger.i(ShareLogger.INFO.LOGIN_FAIL)
            mListener.loginFailure(e)
            recycle()
        }

        override fun loginCancel() {
            ShareLogger.i(ShareLogger.INFO.LOGIN_CANCEL)
            mListener.loginCancel()
            recycle()
        }

        override fun beforeFetchUserInfo(token: BaseToken) {
            ShareLogger.i(ShareLogger.INFO.LOGIN_AUTH_SUCCESS)
            mListener.beforeFetchUserInfo(token)
        }
    }
}
