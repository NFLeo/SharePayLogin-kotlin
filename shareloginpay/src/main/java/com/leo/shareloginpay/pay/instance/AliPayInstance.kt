package com.leo.shareloginpay.pay.instance

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import com.alipay.sdk.app.PayTask
import com.leo.shareloginpay.pay.AliPayParamsBean
import com.leo.shareloginpay.pay.AliPayResultBean
import com.leo.shareloginpay.pay.PayListener

/**
 * Describe : 支付宝支付
 * Created by Leo on 2018/5/7 on 15:26.
 */
class AliPayInstance : PayInstance<AliPayParamsBean> {

    override fun doPay(activity: Activity, payParams: AliPayParamsBean, payListener: PayListener?) {
        AliPayInstance.payListener = payListener

        if (payListener == null) {
            return
        }

        if (TextUtils.isEmpty(payParams.orderInfo)) {
            payListener.payFailed(Exception("pay params can`t be null"))
            return
        }

        val payRunnable = Runnable {
            val payTask = PayTask(activity)
            val payResult = payTask.pay(payParams.orderInfo, true)
            val msg = Message()
            msg.what = PAY_RESULT
            msg.obj = payResult
            mHandler.sendMessage(msg)
        }

        val payThread = Thread(payRunnable)
        payThread.start()
    }

    override fun handleResult(data: Intent) {}

    override fun recycle() {}

    companion object {

        private const val PAY_RESULT = 0x9527
        private var payListener: PayListener? = null

        val mHandler = InnerHandler()
    }

    class InnerHandler :  Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == PAY_RESULT) {
                val resultBean = AliPayResultBean(msg.obj as String)
                val resultStatus = resultBean.resultStatus
                val resultMsg = resultBean.getMemo()
                when (resultStatus) {
                // 判断resultStatus 为“9000”则代表支付成功
                    "9000" -> payListener!!.paySuccess()
                // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认
                // 最终交易是否成功以服务端异步通知为准（小概率状态）
                    "8000" -> {
                    }
                // 支付取消
                    "6001" -> payListener!!.payCancel()
                // 支付失败
                    else -> payListener!!.payFailed(Exception(resultMsg))
                }
            }
        }
    }
}
