package com.leo.shareloginpay.login.instance

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.leo.shareloginpay.ShareLogger
import com.leo.shareloginpay.login.LoginListener
import com.leo.shareloginpay.login.LoginPlatform
import com.leo.shareloginpay.login.LoginResult
import com.leo.shareloginpay.login.result.BaseToken
import com.leo.shareloginpay.login.result.WeiboToken
import com.leo.shareloginpay.login.result.WeiboUser
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.auth.*
import com.sina.weibo.sdk.auth.sso.SsoHandler
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class WeiboLoginInstance(activity: Activity, private var mLoginListener: LoginListener?, appId: String, redirectUrl: String, scope: String, fetchUserInfo: Boolean) : LoginInstance(activity, mLoginListener!!, fetchUserInfo) {

    private var mSsoHandler: SsoHandler? = null
    private val context: Context

    init {
        this.context = activity
        val authInfo = AuthInfo(activity, appId, redirectUrl, scope)
        WbSdk.install(activity, authInfo)
        mSsoHandler = SsoHandler(activity)
    }

    private inner class SelfWbAuthListener internal constructor(private val listener: LoginListener, private val fetchUserInfo: Boolean) : WbAuthListener {

        override fun onSuccess(token: Oauth2AccessToken?) {

            if (token == null) {
                listener.loginFailure(Exception("授权失败"))
                return
            }

            val weiboToken = WeiboToken.parse(token)
            AccessTokenKeeper.writeAccessToken(context, token)
            if (fetchUserInfo) {
                mLoginListener!!.beforeFetchUserInfo(weiboToken)
                fetchUserInfo(weiboToken)
            } else {
                mLoginListener!!.loginSuccess(LoginResult(LoginPlatform.WeiBO, weiboToken))
            }
        }

        override fun cancel() {
            if (mLoginListener != null) {
                mLoginListener!!.loginCancel()
            }
        }

        override fun onFailure(errorMessage: WbConnectErrorMessage) {
            if (mLoginListener != null) {
                mLoginListener!!.loginFailure(Exception(errorMessage.errorMessage))
            }
        }
    }

    override fun doLogin(activity: Activity, listener: LoginListener, fetchUserInfo: Boolean) {
        mSsoHandler!!.authorize(SelfWbAuthListener(listener, fetchUserInfo))
    }

    @SuppressLint("CheckResult")
    override fun fetchUserInfo(token: BaseToken) {
        Flowable.create<WeiboUser>({ weiboUserEmitter ->
            val client = OkHttpClient()
            val request = Request.Builder().url(buildUserInfoUrl(token, USER_INFO)).build()
            try {
                val response = client.newCall(request).execute()
                val jsonObject = JSONObject(response.body()!!.string())
                val user = WeiboUser.parse(jsonObject)
                weiboUserEmitter.onNext(user)
            } catch (e: IOException) {
                ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR)
                weiboUserEmitter.onError(e)
            } catch (e: JSONException) {
                ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR)
                weiboUserEmitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ weiboUser ->
                    mLoginListener!!.loginSuccess(
                            LoginResult(LoginPlatform.WeiBO, token, weiboUser))
                }, { throwable -> mLoginListener!!.loginFailure(Exception(throwable)) })
    }

    private fun buildUserInfoUrl(token: BaseToken, baseUrl: String): String {
        return baseUrl + "?access_token=" + token.accessToken + "&uid=" + token.openid
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (mSsoHandler != null) {
            mSsoHandler!!.authorizeCallBack(requestCode, resultCode, data)
        }
    }

    override fun isInstall(context: Context): Boolean {
        return mSsoHandler!!.isWbAppInstalled
    }

    override fun recycle() {
        mSsoHandler = null
        mLoginListener = null
    }

    companion object {
        private val USER_INFO = "https://api.weibo.com/2/users/show.json"
    }
}
