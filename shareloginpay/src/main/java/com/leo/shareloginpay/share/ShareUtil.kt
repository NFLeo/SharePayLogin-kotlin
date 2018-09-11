package com.leo.shareloginpay.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.leo.shareloginpay.ShareActivity
import com.leo.shareloginpay.ShareLogger
import com.leo.shareloginpay.ShareLoginManager
import com.leo.shareloginpay.share.instance.*
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.util.*

object ShareUtil {

    const val TYPE = 798
    var mShareListener: ShareListener? = null

    private var mShareInstance: ShareInstance? = null

    private const val TYPE_IMAGE = 1
    private const val TYPE_TEXT = 2
    private const val TYPE_MEDIA = 3

    private var mType: Int = 0
    private var mShareImmediate: Boolean = false
    private var mPlatform: SharePlatform = SharePlatform.DEFAULT
    private var mText: String? = null
    private var mShareImageObject: ShareImageObject? = null
    private var mTitle: String? = null
    private var mSummary: String? = null
    private var mTargetUrl: String? = null
    private var mMiniId: String? = null                  // 小程序id
    private var mMiniPath: String? = null                // 小程序path

    internal fun action(activity: Activity) {
        mShareInstance = getShareInstance(mPlatform, activity)

        // 防止之后调用 NullPointException
        if (mShareListener == null) {
            activity.finish()
            return
        }

        if (!mShareInstance!!.isInstall(activity)) {
            mShareListener!!.shareFailure(Exception(ShareLogger.INFO.NOT_INSTALL))
            activity.finish()
            return
        }

        when (mType) {
            TYPE_TEXT -> mShareInstance!!.shareText(mPlatform, mText!!, activity, mShareListener!!)
            TYPE_IMAGE -> mShareInstance!!.shareImage(mPlatform, mShareImageObject!!, activity, mShareListener!!)
            TYPE_MEDIA -> mShareInstance!!.shareMedia(mPlatform, mTitle!!, mTargetUrl!!, mSummary!!, mMiniId, mMiniPath,
                    mShareImageObject!!, mShareImmediate, activity, mShareListener!!)
        }
    }

    fun shareText(context: Context, platform: SharePlatform, text: String,
                  listener: ShareListener) {
        initShareData(context, TYPE_TEXT, platform, listener, text, null, false, null, null, null, null, null)
    }

    fun shareImage(context: Context, platform: SharePlatform,
                   urlOrPath: Any, listener: ShareListener) {
        initShareData(context, TYPE_IMAGE, platform, listener, null, urlOrPath, false, null, null, null, null, null)
    }

    fun shareMedia(context: Context, platform: SharePlatform, title: String, summary: String,
                   targetUrl: String, thumb: Any, shareImmediate: Boolean, listener: ShareListener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumb, shareImmediate, summary, targetUrl, title, null, null)
    }

    fun shareMedia(context: Context, platform: SharePlatform, title: String,
                   summary: String, targetUrl: String, thumb: Any, listener: ShareListener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumb, false, summary, targetUrl, title, null, null)
    }

    fun shareMedia(context: Context, platform: SharePlatform, title: String, summary: String,
                   targetUrl: String, thumbUrlOrPath: Any, miniId: String, miniPath: String, listener: ShareListener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumbUrlOrPath, false, summary, targetUrl, title, miniId, miniPath)
    }

    fun shareMedia(context: Context, platform: SharePlatform, title: String, summary: String,
                   targetUrl: String, thumbUrlOrPath: Any, shareImmediate: Boolean, miniId: String, miniPath: String, listener: ShareListener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumbUrlOrPath, shareImmediate, summary, targetUrl, title, miniId, miniPath)
    }

    /**
     * deal with share data
     *
     * @param context     ctx
     * @param platform    share type
     * @param title       share title
     * @param summary     share content
     * @param targetUrl   targetUrl
     * @param imageObject logo   (image url path or bitmap)
     * @param miniId      miniprogram id
     * @param miniPath    miniprogram path
     * @param listener    result listener
     */
    private fun initShareData(context: Context, type: Int, platform: SharePlatform, listener: ShareListener,
                              text: String?, imageObject: Any?, immediate: Boolean,
                              summary: String?, targetUrl: String?, title: String?, miniId: String?, miniPath: String?) {
        when (type) {
            TYPE_TEXT -> mText = text
            TYPE_IMAGE -> {
                mShareImageObject = ShareImageObject(imageObject!!)
                mShareImageObject!!.isShareImmediate = immediate
            }
            TYPE_MEDIA -> {
                mShareImageObject = ShareImageObject(imageObject!!)
                mShareImageObject!!.isShareImmediate = immediate
                mShareImmediate = immediate
                mSummary = summary
                mTargetUrl = targetUrl
                mTitle = title
                mMiniId = miniId
                mMiniPath = miniPath
            }
        }

        mType = type
        mPlatform = platform
        mShareListener = buildProxyListener(listener)
        ShareActivity.newInstance(context, TYPE)
    }

    private fun buildProxyListener(listener: ShareListener): ShareListener {
        return ShareListenerProxy(listener)
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 微博分享会同时回调onActivityResult和onNewIntent， 而且前者返回的intent为null
        if (mShareInstance != null && data != null) {
            mShareInstance!!.handleResult(requestCode, resultCode, data)
        } else if (data == null) {
            if (mPlatform == SharePlatform.QQ || mPlatform == SharePlatform.QZone) {
                mShareInstance!!.handleResult(requestCode, resultCode, data!!)
            } else if (mPlatform != SharePlatform.WeiBO) {
                ShareLogger.e(ShareLogger.INFO.HANDLE_DATA_NULL)
            }
        } else {
            ShareLogger.e(ShareLogger.INFO.UNKNOWN_ERROR)
        }
    }

    private fun getShareInstance(platform: SharePlatform, context: Context): ShareInstance {
        return when (platform) {
            SharePlatform.WX, SharePlatform.WX_TIMELINE -> WxShareInstance(context, ShareLoginManager.CONFIG.wxId!!)
            SharePlatform.QQ, SharePlatform.QZone -> QQShareInstance(context, ShareLoginManager.CONFIG.qqId!!)
            SharePlatform.WeiBO -> WeiboShareInstance(context, ShareLoginManager.CONFIG.weiboId!!,
                    ShareLoginManager.CONFIG.weiboRedirectUrl, ShareLoginManager.CONFIG.weiboScope)
            SharePlatform.DEFAULT -> DefaultShareInstance()
        }
    }

    fun recycle() {
        mTitle = null
        mSummary = null
        mShareListener = null
        mMiniPath = null
        mMiniId = null

        // bitmap recycle
        if (mShareImageObject != null
                && mShareImageObject!!.bitmap != null
                && !mShareImageObject!!.bitmap!!.isRecycled) {
            mShareImageObject!!.bitmap!!.recycle()
        }
        mShareImageObject = null

        if (mShareInstance != null) {
            mShareInstance!!.recycle()
        }
        mShareInstance = null
    }

    @Deprecated("")
    fun isQQInstalled(context: Context): Boolean {
        val pm = context.packageManager ?: return false

        val packageInfos = pm.getInstalledPackages(0)
        for (info in packageInfos) {
            if (TextUtils.equals(info.packageName.toLowerCase(Locale.getDefault()),
                            "com.tencent.mobileqq")) {
                return true
            }
        }
        return false
    }

    @Deprecated("")
    fun isWeiBoInstalled(context: Context): Boolean {
        return mShareInstance!!.isInstall(context)
    }

    @Deprecated("")
    fun isWeiXinInstalled(context: Context): Boolean {
        val api = WXAPIFactory.createWXAPI(context, ShareLoginManager.CONFIG.wxId, true)
        return api.isWXAppInstalled
    }

    private class ShareListenerProxy internal constructor(private val mShareListener: ShareListener) : ShareListener() {

        override fun shareSuccess() {
            ShareLogger.i(ShareLogger.INFO.SHARE_SUCCESS)
            ShareUtil.recycle()
            mShareListener.shareSuccess()
        }

        override fun shareFailure(e: Exception) {
            ShareLogger.i(ShareLogger.INFO.SHARE_FAILURE)
            ShareUtil.recycle()
            mShareListener.shareFailure(e)
        }

        override fun shareCancel() {
            ShareLogger.i(ShareLogger.INFO.SHARE_CANCEL)
            ShareUtil.recycle()
            mShareListener.shareCancel()
        }

        override fun shareRequest() {
            ShareLogger.i(ShareLogger.INFO.SHARE_REQUEST)
            mShareListener.shareRequest()
        }
    }
}
