package com.leo.shareloginpay.share.instance

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.leo.shareloginpay.R
import com.leo.shareloginpay.share.ImageDecoder
import com.leo.shareloginpay.share.ShareImageObject
import com.leo.shareloginpay.share.ShareListener
import com.leo.shareloginpay.share.SharePlatform
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class DefaultShareInstance : ShareInstance {

    override fun shareText(platform: SharePlatform, text: String, activity: Activity, listener: ShareListener) {
        handleShare(activity, SHARE_TYPE_TEXT, "", "", text, null)
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                            activity: Activity, listener: ShareListener) {
        createImageShare(title, targetUrl, summary, shareImageObject, activity, listener)
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                            shareImmediate: Boolean, activity: Activity, listener: ShareListener) {
        createImageShare(title, targetUrl, summary, shareImageObject, activity, listener)
    }

    private fun handleShare(activity: Activity, type: Int, title: String, targetUrl: String, summary: String, imageUri: Uri?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", summary, targetUrl))

        if (type == SHARE_TYPE_IMAGE && imageUri != null) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            sendIntent.type = "image/*"
        } else {
            sendIntent.type = "text/plain"
        }

        activity.startActivity(Intent.createChooser(sendIntent, activity.resources.getString(R.string.vista_share_title)))
    }

    override fun shareImage(platform: SharePlatform, shareImageObject: ShareImageObject,
                   activity: Activity, listener: ShareListener) {
        createImageShare("", "", "", shareImageObject, activity, listener)
    }

    private fun createImageShare(title: String, targetUrl: String, summary: String, shareImageObject: ShareImageObject,
                                 activity: Activity, listener: ShareListener) {
        Flowable.create(FlowableOnSubscribe<Uri> { emitter ->
            try {
                val uri = Uri.fromFile(File(ImageDecoder.decode(activity, shareImageObject)))
                emitter.onNext(uri)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest { listener.shareRequest() }
                .subscribe({ uri -> handleShare(activity, SHARE_TYPE_IMAGE, title, targetUrl, summary, uri) }, { throwable -> listener.shareFailure(Exception(throwable)) })
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Default share, do nothing
    }

    override fun isInstall(context: Context): Boolean {
        return true
    }

    override fun recycle() {}

    companion object {

        private const val SHARE_TYPE_TEXT = 0x11111
        private const val SHARE_TYPE_IMAGE = 0x11112
    }
}
