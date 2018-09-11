package com.leo.shareloginpay.pay.instance

import android.app.Activity
import android.content.Intent
import android.text.TextUtils

import com.leo.shareloginpay.ShareLoginManager
import com.leo.shareloginpay.pay.PayListener
import com.leo.shareloginpay.pay.WXPayParamsBean
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * Describe : 微信支付
 * Created by Leo on 2018/5/7 on 16:04.
 */
class WXPayInstance(activity: Activity) : PayInstance<WXPayParamsBean>, IWXAPIEventHandler {

    private val mIWXAPI: IWXAPI? = WXAPIFactory.createWXAPI(activity, ShareLoginManager.CONFIG.wxId)
    private var payParams: WXPayParamsBean? = null
    private var payCallback: PayListener? = null

    init {
        mIWXAPI!!.handleIntent(activity.intent, this)
    }

    override fun doPay(activity: Activity, payParams: WXPayParamsBean, payListener: PayListener?) {
        this.payParams = payParams
        payCallback = payListener
        if (!check()) {
            if (payCallback != null) {
                payCallback!!.payFailed(Exception("please install client first"))
                activity.finish()
            }
            return
        }
        if (this.payParams == null || TextUtils.isEmpty(this.payParams!!.appid) || TextUtils.isEmpty(this.payParams!!.partnerid)
                || TextUtils.isEmpty(this.payParams!!.prepayId) || TextUtils.isEmpty(this.payParams!!.packageValue) ||
                TextUtils.isEmpty(this.payParams!!.nonceStr) || TextUtils.isEmpty(this.payParams!!.timestamp) ||
                TextUtils.isEmpty(this.payParams!!.sign)) {
            if (payCallback != null) {
                payCallback!!.payFailed(Exception("pay params can`t be null"))
                activity.finish()
            }
            return
        }

        val req = PayReq()
        req.appId = this.payParams!!.appid
        req.partnerId = this.payParams!!.partnerid
        req.prepayId = this.payParams!!.prepayId
        req.packageValue = this.payParams!!.packageValue
        req.nonceStr = this.payParams!!.nonceStr
        req.timeStamp = this.payParams!!.timestamp
        req.sign = this.payParams!!.sign
        mIWXAPI!!.sendReq(req)
    }

    override fun handleResult(data: Intent) {
        mIWXAPI!!.handleIntent(data, this)
    }

    override fun recycle() {
        mIWXAPI?.detach()
    }

    //检测是否支持微信支付
    private fun check(): Boolean {
        return mIWXAPI!!.isWXAppInstalled && mIWXAPI.wxAppSupportAPI >= Build.PAY_SUPPORTED_SDK_INT
    }

    override fun onReq(baseReq: BaseReq) {

    }

    override fun onResp(baseResp: BaseResp?) {
        if (payCallback == null) {
            return
        }

        if (baseResp != null && baseResp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            when (baseResp.errCode) {
                0 -> payCallback!!.paySuccess()
                -1 -> {
                    val errorStr = if (TextUtils.isEmpty(baseResp.errStr)) "pay failed" else baseResp.errStr
                    payCallback!!.payFailed(Exception(errorStr))
                }
                -2 -> payCallback!!.payCancel()
            }
        }
    }
}
