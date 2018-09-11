package com.leo.shareloginpay.share.instance

import android.app.Activity
import android.content.Context
import android.content.Intent

import com.leo.shareloginpay.share.ShareImageObject
import com.leo.shareloginpay.share.ShareListener
import com.leo.shareloginpay.share.SharePlatform

interface ShareInstance {

    fun shareText(platform: SharePlatform, text: String, activity: Activity, listener: ShareListener)

    fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String, miniId: String?, miniPath: String?,
                   shareImageObject: ShareImageObject, activity: Activity, listener: ShareListener)

    /**
     * 分享图文
     * @param platform           分享类型
     * @param title              分享标题
     * @param targetUrl          分享后跳转链接
     * @param summary            分享说明
     * @param miniId             小程序id
     * @param miniPath           小程序path
     * @param shareImageObject   分享图标对象
     * @param shareImmediate     是否直接分享（不对图片二次处理）
     * @param activity           activity
     * @param listener           分享结果
     */
    fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String, miniId: String?, miniPath: String?,
                   shareImageObject: ShareImageObject, shareImmediate: Boolean, activity: Activity, listener: ShareListener)

    fun shareImage(platform: SharePlatform, shareImageObject: ShareImageObject, activity: Activity, listener: ShareListener)

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent)

    fun isInstall(context: Context): Boolean

    fun recycle()
}
