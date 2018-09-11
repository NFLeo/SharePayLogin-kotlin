package com.leo.shareloginpay.login.instance

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.leo.shareloginpay.ShareLogger
import com.leo.shareloginpay.ShareLoginManager
import com.leo.shareloginpay.login.LoginListener
import com.leo.shareloginpay.login.LoginPlatform
import com.leo.shareloginpay.login.LoginResult
import com.leo.shareloginpay.login.result.BaseToken
import com.leo.shareloginpay.login.result.WxToken
import com.leo.shareloginpay.login.result.WxUser
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class WxLoginInstance(activity: Activity, private val mLoginListener: LoginListener, private val fetchUserInfo: Boolean) : LoginInstance(activity, mLoginListener, fetchUserInfo) {

    private val mIWXAPI: IWXAPI? = WXAPIFactory.createWXAPI(activity, ShareLoginManager.CONFIG.wxId)
    private val mClient: OkHttpClient = OkHttpClient()

    override fun doLogin(activity: Activity, listener: LoginListener, fetchUserInfo: Boolean) {
        val req = SendAuth.Req()
        req.scope = SCOPE_USER_INFO
        req.state = System.currentTimeMillis().toString()
        mIWXAPI!!.sendReq(req)
    }

    private fun getToken(code: String) {
        Flowable.create<WxToken>({ wxTokenEmitter ->
            val request = Request.Builder().url(buildTokenUrl(code)).build()
            try {
                val response = mClient.newCall(request).execute()
                val jsonObject = JSONObject(response.body()!!.string())
                val token = WxToken.parse(jsonObject)
                wxTokenEmitter.onNext(token)
            } catch (e: IOException) {
                wxTokenEmitter.onError(e)
            } catch (e: JSONException) {
                wxTokenEmitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ wxToken ->
                    if (fetchUserInfo) {
                        mLoginListener.beforeFetchUserInfo(wxToken)
                        fetchUserInfo(wxToken)
                    } else {
                        mLoginListener.loginSuccess(LoginResult(LoginPlatform.WX, wxToken))
                    }
                }, { throwable -> mLoginListener.loginFailure(Exception(throwable.message)) })
    }

    override fun fetchUserInfo(token: BaseToken) {
        Flowable.create<WxUser>({ wxUserEmitter ->
            val request = Request.Builder().url(buildUserInfoUrl(token)).build()
            try {
                val response = mClient.newCall(request).execute()
                val jsonObject = JSONObject(response.body()!!.string())
                val user = WxUser.parse(jsonObject)
                wxUserEmitter.onNext(user)
            } catch (e: IOException) {
                wxUserEmitter.onError(e)
            } catch (e: JSONException) {
                wxUserEmitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ wxUser ->
                    mLoginListener.loginSuccess(
                            LoginResult(LoginPlatform.WX, token, wxUser))
                }, { throwable -> mLoginListener.loginFailure(Exception(throwable)) })
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        mIWXAPI!!.handleIntent(data, object : IWXAPIEventHandler {
            override fun onReq(baseReq: BaseReq) {}

            override fun onResp(baseResp: BaseResp) {
                if (baseResp is SendAuth.Resp && baseResp.getType() == 1) {
                    when (baseResp.errCode) {
                        BaseResp.ErrCode.ERR_OK -> getToken(baseResp.code)
                        BaseResp.ErrCode.ERR_USER_CANCEL -> mLoginListener.loginCancel()
                        BaseResp.ErrCode.ERR_SENT_FAILED -> mLoginListener.loginFailure(Exception(ShareLogger.INFO.WX_ERR_SENT_FAILED))
                        BaseResp.ErrCode.ERR_UNSUPPORT -> mLoginListener.loginFailure(Exception(ShareLogger.INFO.WX_ERR_UNSUPPORT))
                        BaseResp.ErrCode.ERR_AUTH_DENIED -> mLoginListener.loginFailure(Exception(ShareLogger.INFO.WX_ERR_AUTH_DENIED))
                        else -> mLoginListener.loginFailure(Exception(ShareLogger.INFO.WX_ERR_AUTH_ERROR))
                    }
                }
            }
        })
    }

    override fun isInstall(context: Context): Boolean {
        return mIWXAPI!!.isWXAppInstalled
    }

    override fun recycle() {
        mIWXAPI?.detach()
    }

    private fun buildTokenUrl(code: String): String {
        return (BASE_URL
                + "oauth2/access_token?appid="
                + ShareLoginManager.CONFIG.wxId
                + "&secret="
                + ShareLoginManager.CONFIG.wxSecret
                + "&code="
                + code
                + "&grant_type=authorization_code")
    }

    private fun buildUserInfoUrl(token: BaseToken): String {
        return (BASE_URL
                + "userinfo?access_token="
                + token.accessToken
                + "&openid="
                + token.openid)
    }

    companion object {

        val SCOPE_USER_INFO = "snsapi_userinfo"
        private val SCOPE_BASE = "snsapi_base"

        private val BASE_URL = "https://api.weixin.qq.com/sns/"
    }
}