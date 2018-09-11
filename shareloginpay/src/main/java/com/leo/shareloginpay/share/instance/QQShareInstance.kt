package com.leo.shareloginpay.share.instance

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.leo.shareloginpay.ShareLogger
import com.leo.shareloginpay.share.*
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzonePublish
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.Tencent
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class QQShareInstance(context: Context, app_id: String) : ShareInstance {

    private var mTencent: Tencent? = null

    init {
        mTencent = Tencent.createInstance(app_id, context.applicationContext)
    }

    override fun shareText(platform: SharePlatform, text: String, activity: Activity, listener: ShareListener) {
        if (platform == SharePlatform.QZone) {
            shareToQZoneForText(text, activity, listener)
        } else {
            listener.shareFailure(Exception(ShareLogger.INFO.QQ_NOT_SUPPORT_SHARE_TXT))
            activity.finish()
        }
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                            activity: Activity, listener: ShareListener) {
        shareFunc(platform, title, targetUrl, summary, shareImageObject, false, activity, listener)
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                            shareImmediate: Boolean, activity: Activity, listener: ShareListener) {
        // 直接分享，外部处理好分享图片
        shareFunc(platform, title, targetUrl, summary, shareImageObject, shareImmediate, activity, listener)
    }

    private fun shareFunc(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                          shareImageObject: ShareImageObject, immediate: Boolean, activity: Activity, listener: ShareListener) {
        Flowable.create(FlowableOnSubscribe<String> { emitter ->
            try {
                if (immediate) {
                    emitter.onNext(shareImageObject.pathOrUrl!!)
                } else {
                    emitter.onNext(ImageDecoder.decode(activity, shareImageObject))
                }
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest { listener.shareRequest() }
                .subscribe({ s ->
                    if (platform == SharePlatform.QZone) {
                        shareToQZoneForMedia(title, targetUrl, summary, s, activity, listener)
                    } else {
                        shareToQQForMedia(title, summary, targetUrl, s, activity, listener)
                    }
                }, { throwable ->
                    activity.finish()
                    listener.shareFailure(Exception(throwable))
                })
    }

    override fun shareImage(platform: SharePlatform, shareImageObject: ShareImageObject,
                            activity: Activity, listener: ShareListener) {
        Flowable.create(FlowableOnSubscribe<String> { emitter ->
            try {
                emitter.onNext(ImageDecoder.decode(activity, shareImageObject))
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest { listener.shareRequest() }
                .subscribe({ localPath ->
                    if (platform == SharePlatform.QZone) {
                        shareToQZoneForImage(localPath, activity, listener)
                    } else {
                        shareToQQForImage(localPath, activity, listener)
                    }
                }, { throwable ->
                    activity.finish()
                    listener.shareFailure(Exception(throwable))
                })
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        Tencent.onActivityResultData(requestCode, resultCode, data, ShareUtil.mShareListener)
    }

    override fun isInstall(context: Context): Boolean {
        val pm = context.packageManager ?: return false

        val packageInfos = pm.getInstalledPackages(0)
        for (info in packageInfos) {
            if (TextUtils.equals(info.packageName.toLowerCase(), "com.tencent.mobileqq")) {
                return true
            }
        }
        return false
    }

    override fun recycle() {
        if (mTencent != null) {
            mTencent!!.releaseResource()
            mTencent = null
        }
    }

    private fun shareToQQForMedia(title: String, summary: String, targetUrl: String, thumbUrl: String,
                                  activity: Activity, listener: ShareListener) {
        val params = Bundle()
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title)
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary)
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl)
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, thumbUrl)
        mTencent!!.shareToQQ(activity, params, listener)
    }

    private fun shareToQQForImage(localUrl: String, activity: Activity, listener: ShareListener) {
        val params = Bundle()
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, localUrl)
        mTencent!!.shareToQQ(activity, params, listener)
    }

    private fun shareToQZoneForText(text: String, activity: Activity, listener: ShareListener) {
        val params = Bundle()
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, text)
        mTencent!!.publishToQzone(activity, params, listener)
    }

    private fun shareToQZoneForMedia(title: String, targetUrl: String, summary: String,
                                     imageUrl: String, activity: Activity, listener: ShareListener) {
        val params = Bundle()
        val image = ArrayList<String>()
        image.add(imageUrl)
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title)
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary)
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl)
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, image)
        mTencent!!.shareToQzone(activity, params, listener)
    }

    private fun shareToQZoneForImage(imagePath: String, activity: Activity, listener: ShareListener) {
        val params = Bundle()
        val image = ArrayList<String>()
        image.add(imagePath)
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, image)
        mTencent!!.publishToQzone(activity, params, listener)
    }
}
