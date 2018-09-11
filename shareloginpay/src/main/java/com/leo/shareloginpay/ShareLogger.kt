package com.leo.shareloginpay

import android.util.Log

object ShareLogger {

    private const val TAG = "share_login_logger"

    fun i(info: String) {
        if (ShareLoginManager.CONFIG.isDebug) {
            Log.i(TAG, info)
        }
    }

    fun e(error: String) {
        if (ShareLoginManager.CONFIG.isDebug) {
            Log.e(TAG, error)
        }
    }

    object INFO {

        const val SHARE_SUCCESS = "call share success"
        const val SHARE_FAILURE = "call share failure"
        const val SHARE_CANCEL = "call share cancel"
        const val SHARE_REQUEST = "call share request"

        // for share
        const val HANDLE_DATA_NULL = "Handle the result, but the data is null, please check you app id"
        const val UNKNOWN_ERROR = "Unknown error"
        const val NOT_INSTALL = "The application is not install"
        const val DEFAULT_QQ_SHARE_ERROR = "QQ share failed"
        const val QQ_NOT_SUPPORT_SHARE_TXT = "QQ not support share text"
        const val IMAGE_FETCH_ERROR = "Image fetch error"
        const val SD_CARD_NOT_AVAILABLE = "The sd card is not available"

        // for login
        const val LOGIN_SUCCESS = "call login success"
        const val LOGIN_FAIL = "call login failed"
        const val LOGIN_CANCEL = "call login cancel"
        const val LOGIN_AUTH_SUCCESS = "call before fetch user info"
        const val ILLEGAL_TOKEN = "Illegal token, please check your config"
        const val QQ_LOGIN_ERROR = "QQ login error"
        const val QQ_AUTH_SUCCESS = "QQ auth success"
        const val WEIBO_AUTH_ERROR = "weibo auth error"
        const val UNKNOW_PLATFORM = "unknown platform"

        // for pay
        const val PAY_SUCCESS = "call pay success"
        const val PAY_FAIL = "call pay failed"
        const val PAY_CANCEL = "call pay cancel"

        const val WX_ERR_SENT_FAILED = "Wx sent failed"
        const val WX_ERR_UNSUPPORT = "Wx UnSupport"
        const val WX_ERR_AUTH_DENIED = "Wx auth denied"
        const val WX_ERR_AUTH_ERROR = "Wx auth error"

        const val AUTH_CANCEL = "auth cancel"
        const val FETCH_USER_INOF_ERROR = "Fetch user info error"

        // for shareActivity
        const val ACTIVITY_CREATE = "ShareActivity onCreate"
        const val ACTIVITY_RESUME = "ShareActivity onResume"
        const val ACTIVITY_RESULT = "ShareActivity onActivityResult"
        const val ACTIVITY_NEW_INTENT = "ShareActivity onNewIntent"
    }
}