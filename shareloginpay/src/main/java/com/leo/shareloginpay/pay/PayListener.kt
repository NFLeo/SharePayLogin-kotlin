package com.leo.shareloginpay.pay

/**
 * Describe : 支付结果回调
 * Created by Leo on 2018/5/7 on 15:21.
 */
abstract class PayListener {
    abstract fun paySuccess()

    abstract fun payFailed(e: Exception)

    abstract fun payCancel()
}
