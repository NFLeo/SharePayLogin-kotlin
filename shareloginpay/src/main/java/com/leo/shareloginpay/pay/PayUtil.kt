package com.leo.shareloginpay.pay

import android.app.Activity
import android.content.Context
import android.content.Intent

import com.leo.shareloginpay.ShareActivity
import com.leo.shareloginpay.ShareLogger
import com.leo.shareloginpay.pay.instance.AliPayInstance
import com.leo.shareloginpay.pay.instance.PayInstance
import com.leo.shareloginpay.pay.instance.WXPayInstance

/**
 * Describe : 支付
 * Created by Leo on 2018/5/7.
 */
object PayUtil {

    private var mPayInstance: PayInstance<*>? = null
    private var mPayListener: PayListener? = null
    private var mPlatform: PayPlatform = PayPlatform.DEFAULE
    private lateinit var mPayParamsBean: IPayParamsBean

    const val TYPE = 800

    fun pay(context: Context, platform: PayPlatform, paramsBean: IPayParamsBean, listener: PayListener) {
        mPlatform = platform
        mPayParamsBean = paramsBean
        mPayListener = PayListenerProxy(listener)
        if (platform == PayPlatform.ALIPAY) {
            mPayInstance = AliPayInstance()
            (mPayInstance as AliPayInstance).doPay(context as Activity, mPayParamsBean as AliPayParamsBean, mPayListener)
        } else {
            ShareActivity.newInstance(context, TYPE)
        }
    }

    internal fun action(activity: Activity) {
        when (mPlatform) {
            PayPlatform.ALIPAY -> {
                mPayInstance = AliPayInstance()
                (mPayInstance as AliPayInstance).doPay(activity, mPayParamsBean as AliPayParamsBean, mPayListener)
            }
            PayPlatform.WXPAY -> {
                mPayInstance = WXPayInstance(activity)
                (mPayInstance as WXPayInstance).doPay(activity, mPayParamsBean as WXPayParamsBean, mPayListener)
            }
            else -> {
                mPayListener!!.payFailed(Exception(ShareLogger.INFO.UNKNOW_PLATFORM))
                activity.finish()
            }
        }
    }

    internal fun handleResult(data: Intent) {
        if (mPayInstance != null) {
            mPayInstance!!.handleResult(data)
        }
    }

    fun recycle() {
        if (mPayInstance != null) {
            mPayInstance!!.recycle()
        }
        mPayInstance = null
        mPayListener = null
        mPlatform = PayPlatform.DEFAULE
    }

    private class PayListenerProxy(private val mListener: PayListener) : PayListener() {

        override fun paySuccess() {
            ShareLogger.i(ShareLogger.INFO.PAY_SUCCESS)
            mListener.paySuccess()
            recycle()
        }

        override fun payFailed(e: Exception) {
            ShareLogger.i(ShareLogger.INFO.PAY_FAIL)
            mListener.payFailed(e)
            recycle()
        }

        override fun payCancel() {
            ShareLogger.i(ShareLogger.INFO.PAY_CANCEL)
            mListener.payCancel()
            recycle()
        }
    }
}
