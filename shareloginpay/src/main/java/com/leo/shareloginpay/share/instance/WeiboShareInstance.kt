package com.leo.shareloginpay.share.instance

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Pair
import com.leo.shareloginpay.share.*

import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.api.ImageObject
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.share.WbShareCallback
import com.sina.weibo.sdk.share.WbShareHandler

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class WeiboShareInstance(context: Context, appId: String, redirectUrl: String, scope: String) : ShareInstance {
    private var shareHandler: WbShareHandler? = null

    init {
        val authInfo = AuthInfo(context, appId, redirectUrl, scope)
        WbSdk.install(context, authInfo)
        shareHandler = WbShareHandler(context as Activity)
        shareHandler!!.registerApp()
    }

    override fun shareText(platform: SharePlatform, text: String, activity: Activity, listener: ShareListener) {
        val message = WeiboMultiMessage()
        message.textObject = getTextObj("", "", text)
        shareHandler!!.shareMessage(message, false)
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                            activity: Activity, listener: ShareListener) {
        shareTextOrImage(shareImageObject, title, targetUrl, summary, activity, listener)
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                            shareImmediate: Boolean, activity: Activity, listener: ShareListener) {
        if (shareImmediate) {
            if (shareImageObject.pair != null) {
                shareTextOrImage(shareImageObject.pair!!, title, targetUrl, summary, activity, listener)
            }
        } else {
            shareTextOrImage(shareImageObject, title, targetUrl, summary, activity, listener)
        }
    }

    override fun shareImage(platform: SharePlatform, shareImageObject: ShareImageObject, activity: Activity,
                            listener: ShareListener) {
        shareTextOrImage(shareImageObject, "", "", "", activity, listener)
    }

    override fun handleResult(requestCode: Int, resultCode: Int, mIntent: Intent) {
        shareHandler!!.doResultIntent(mIntent, object : WbShareCallback {
            override fun onWbShareSuccess() {
                ShareUtil.mShareListener?.shareSuccess()
            }

            override fun onWbShareCancel() {
                ShareUtil.mShareListener?.shareCancel()
            }

            override fun onWbShareFail() {
                ShareUtil.mShareListener?.shareFailure(Exception("分享失败"))
            }
        })
    }

    override fun isInstall(context: Context): Boolean {
        return shareHandler!!.isWbAppInstalled
    }

    override fun recycle() {
        shareHandler = null
    }

    private fun shareTextOrImage(shareImageObject: ShareImageObject, title: String, targetUrl: String, summary: String,
                                 activity: Activity, listener: ShareListener) {

        Flowable.create(FlowableOnSubscribe<Pair<String, ByteArray>> { emitter ->
            try {
                val path = ImageDecoder.decode(activity, shareImageObject)
                emitter.onNext(Pair.create(path, ImageDecoder.compress2Byte(path, TARGET_SIZE, TARGET_LENGTH)))
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest { listener.shareRequest() }
                .subscribe({ stringPair -> handleShare(stringPair, title, targetUrl, summary) }, { throwable ->
                    activity.finish()
                    listener.shareFailure(Exception(throwable.message))
                })
    }

    private fun shareTextOrImage(shareImageObject: Pair<String, ByteArray>, title: String, targetUrl: String, summary: String,
                                 activity: Activity, listener: ShareListener) {

        Flowable.create(FlowableOnSubscribe<Pair<String, ByteArray>> { emitter ->
            try {
                emitter.onNext(shareImageObject)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest { listener.shareRequest() }
                .subscribe({ stringPair -> handleShare(stringPair, title, targetUrl, summary) }, { throwable ->
                    activity.finish()
                    listener.shareFailure(Exception(throwable))
                })
    }

    private fun handleShare(stringPair: Pair<String, ByteArray>?, title: String, targetUrl: String, summary: String) {

        val message = WeiboMultiMessage()
        if (!TextUtils.isEmpty(summary)) {
            message.textObject = getTextObj(title, targetUrl, summary)
        }

        if (stringPair != null) {
            message.imageObject = getImageObj(stringPair)
        }

        shareHandler!!.shareMessage(message, false)
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private fun getTextObj(title: String, targetUrl: String, summary: String): TextObject {
        val textObject = TextObject()
        textObject.text = summary
        textObject.title = title
        textObject.actionUrl = targetUrl
        return textObject
    }

    /**
     * 创建图片消息对象。
     * String、Bitmap、Resource type image change to pair to share
     *
     * @return 图片消息对象。
     */
    private fun getImageObj(`object`: Pair<String, ByteArray>): ImageObject {
        val imageObject = ImageObject()
        imageObject.imagePath = `object`.first
        imageObject.imageData = `object`.second
        return imageObject
    }

    companion object {

        private const val TARGET_SIZE = 1024
        private const val TARGET_LENGTH = 2097152
    }
}
