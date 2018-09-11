package com.leo.shareloginpay.pay.instance

import android.app.Activity
import android.content.Intent

import com.leo.shareloginpay.pay.IPayParamsBean
import com.leo.shareloginpay.pay.PayListener

/**
 * Describe : 支付instance
 * Created by Leo on 2018/5/7 on 15:20.
 */
interface PayInstance<T : IPayParamsBean> {
    fun doPay(activity: Activity, payParams: T, payListener: PayListener?)

    fun handleResult(data: Intent)

    fun recycle()
}
