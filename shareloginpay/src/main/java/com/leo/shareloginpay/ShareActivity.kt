package com.leo.shareloginpay

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.leo.shareloginpay.login.LoginUtil
import com.leo.shareloginpay.pay.PayUtil
import com.leo.shareloginpay.share.ShareUtil

class ShareActivity : Activity() {
    private var mType = 0
    private var isNew = false

    companion object {
        const val TYPE = "share_activity_type"

        fun newInstance(context: Context?, type: Int) {
            if (context == null) {
                return
            }

            val intent = Intent(context, ShareActivity::class.java)
            if (context is Application) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.putExtra(TYPE, type)
            context.startActivity(intent)
            (context as Activity).overridePendingTransition(0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShareLogger.i(ShareLogger.INFO.ACTIVITY_CREATE)
        isNew = true

        mType = intent.getIntExtra(TYPE, 0)
        when (mType) {
            ShareUtil.TYPE -> // 分享
                ShareUtil.action(this)
            LoginUtil.TYPE -> // 登录
                LoginUtil.action(this)
            PayUtil.TYPE -> PayUtil.action(this)
            else -> {
                // handle 微信回调
                LoginUtil.handleResult(-1, -1, intent)
                ShareUtil.handleResult(-1, -1, intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ShareLogger.i(ShareLogger.INFO.ACTIVITY_RESUME)
        if (isNew) {
            isNew = false
        } else {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ShareLogger.i(ShareLogger.INFO.ACTIVITY_NEW_INTENT)
        handleCallBack(0, 0, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        ShareLogger.i(ShareLogger.INFO.ACTIVITY_RESULT)
        handleCallBack(requestCode, resultCode, data)
    }

    // 处理回调
    private fun handleCallBack(requestCode: Int, resultCode: Int, data: Intent) {
        when (mType) {
            LoginUtil.TYPE -> LoginUtil.handleResult(requestCode, resultCode, data)
            ShareUtil.TYPE -> ShareUtil.handleResult(requestCode, resultCode, data)
            PayUtil.TYPE -> PayUtil.handleResult(data)
        }
        finish()
    }
}