package com.leo.shareloginpay.share

import com.leo.shareloginpay.ShareLogger
import com.tencent.tauth.IUiListener
import com.tencent.tauth.UiError

abstract class ShareListener : IUiListener {
    override fun onComplete(o: Any) {
        shareSuccess()
    }

    override fun onError(uiError: UiError?) {
        shareFailure(Exception(if (uiError == null) ShareLogger.INFO.DEFAULT_QQ_SHARE_ERROR else uiError.errorDetail))
    }

    override fun onCancel() {
        shareCancel()
    }

    abstract fun shareSuccess()

    abstract fun shareFailure(e: Exception)

    abstract fun shareCancel()

    open fun shareRequest() {}
}
