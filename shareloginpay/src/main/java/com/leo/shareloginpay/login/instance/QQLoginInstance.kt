package com.leo.shareloginpay.login.instance

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.leo.shareloginpay.ShareLogger
import com.leo.shareloginpay.ShareLoginManager
import com.leo.shareloginpay.login.LoginListener
import com.leo.shareloginpay.login.LoginPlatform
import com.leo.shareloginpay.login.LoginResult
import com.leo.shareloginpay.login.result.BaseToken
import com.leo.shareloginpay.login.result.QQToken
import com.leo.shareloginpay.login.result.QQUser
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class QQLoginInstance(activity: Activity, var mLoginListener: LoginListener?,
                      fetchUserInfo: Boolean) : LoginInstance(activity, mLoginListener, fetchUserInfo) {

    private var mTencent: Tencent? = null

    private var mIUiListener: IUiListener? = null

    init {
        mTencent = Tencent.createInstance(ShareLoginManager.CONFIG.qqId, activity.applicationContext)
        mIUiListener = object : IUiListener {
            override fun onComplete(o: Any) {
                ShareLogger.i(ShareLogger.INFO.QQ_AUTH_SUCCESS)
                try {
                    val token = QQToken.parse(o as JSONObject)
                    if (fetchUserInfo) {
                        mLoginListener?.beforeFetchUserInfo(token)
                        fetchUserInfo(token)
                    } else {
                        mLoginListener?.loginSuccess(LoginResult(LoginPlatform.QQ, token))
                    }
                } catch (e: JSONException) {
                    ShareLogger.i(ShareLogger.INFO.ILLEGAL_TOKEN)
                    mLoginListener!!.loginFailure(e)
                }
            }

            override fun onError(uiError: UiError) {
                ShareLogger.i(ShareLogger.INFO.QQ_LOGIN_ERROR)
                mLoginListener?.loginFailure(
                        Exception("QQError: " + uiError.errorCode + uiError.errorDetail))
            }

            override fun onCancel() {
                ShareLogger.i(ShareLogger.INFO.AUTH_CANCEL)
                mLoginListener?.loginCancel()
            }
        }
    }

    override fun doLogin(activity: Activity, listener: LoginListener, fetchUserInfo: Boolean) {
        mTencent!!.login(activity, SCOPE, mIUiListener)
    }

    override fun fetchUserInfo(token: BaseToken) {
        Flowable.create<QQUser>({ qqUserEmitter ->
            val client = OkHttpClient()
            val request = Request.Builder().url(buildUserInfoUrl(token, URL)).build()

            try {
                val response = client.newCall(request).execute()
                val jsonObject = JSONObject(response.body()!!.string())
                val user = QQUser.parse(token.openid!!, jsonObject)
                qqUserEmitter.onNext(user)
            } catch (e: IOException) {
                ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR)
                qqUserEmitter.onError(e)
            } catch (e: JSONException) {
                ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR)
                qqUserEmitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ qqUser ->
                    mLoginListener!!.loginSuccess(
                            LoginResult(LoginPlatform.QQ, token, qqUser))
                }, { throwable -> mLoginListener!!.loginFailure(Exception(throwable)) })
    }

    private fun buildUserInfoUrl(token: BaseToken, base: String): String {
        return base + "?access_token=" + token.accessToken +
                "&oauth_consumer_key=" + ShareLoginManager.CONFIG.qqId +
                "&openid=" + token.openid
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        Tencent.handleResultData(data, mIUiListener)
    }

    override fun isInstall(context: Context): Boolean {
        val pm = context.packageManager ?: return false

        val packageInfos = pm.getInstalledPackages(0)
        for (info in packageInfos) {
            if (TextUtils.equals(info.packageName.toLowerCase(), "com.tencent.mobileqq")) {
                return true
            }
        }
        return false
    }

    override fun recycle() {
        mTencent!!.releaseResource()
        mIUiListener = null
        mLoginListener = null
        mTencent = null
    }

    companion object {

        private val SCOPE = "get_simple_userinfo"
        private val URL = "https://graph.qq.com/user/get_user_info"
    }
}
