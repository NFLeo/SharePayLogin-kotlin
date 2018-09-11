package com.leo.shareloginpay.share.instance

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Pair
import com.leo.shareloginpay.share.*
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class WxShareInstance(context: Context, appId: String) : ShareInstance {

    /**
     * 微信分享限制thumb image必须小于32Kb，否则点击分享会没有反应
     */
    private val mIWXAPI: IWXAPI = WXAPIFactory.createWXAPI(context, appId, true)

    init {
        mIWXAPI.registerApp(appId)
    }

    override fun shareText(platform: SharePlatform, text: String, activity: Activity, listener: ShareListener) {
        val textObject = WXTextObject()
        textObject.text = text

        val message = WXMediaMessage()
        message.mediaObject = textObject
        message.description = text

        sendMessage(platform, message, buildTransaction("text"))
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                            activity: Activity, listener: ShareListener) {
        shareFunc(platform, title, targetUrl, summary, miniId, miniPath, shareImageObject, activity, listener)
    }

    override fun shareMedia(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                            miniId: String?, miniPath: String?, shareImageObject: ShareImageObject, shareImmediate: Boolean, activity: Activity, listener: ShareListener) {
        // 直接分享，外部处理好分享图片
        if (shareImmediate) {
            if (shareImageObject.bytes != null) {
                handleShareWx(platform, title, targetUrl, summary, shareImageObject.bytes!!, miniId, miniPath)
            }
        } else {
            shareFunc(platform, title, targetUrl, summary, miniId, miniPath, shareImageObject, activity, listener)
        }
    }

    private fun shareFunc(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                          miniId: String?, miniPath: String?, shareImageObject: ShareImageObject,
                          activity: Activity, listener: ShareListener) {

        Flowable.create(FlowableOnSubscribe<ByteArray> { emitter ->
            try {
                val imagePath = ImageDecoder.decode(activity, shareImageObject)
                emitter.onNext(ImageDecoder.compress2Byte(imagePath, TARGET_SIZE, THUMB_SIZE))
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest { listener.shareRequest() }
                .subscribe({ bytes -> handleShareWx(platform, title, targetUrl, summary, bytes, miniId, miniPath) }, { throwable ->
                    activity.finish()
                    listener.shareFailure(Exception(throwable))
                })
    }

    private fun handleShareWx(platform: SharePlatform, title: String, targetUrl: String, summary: String,
                              bytes: ByteArray, miniId: String?, miniPath: String?) {
        val message: WXMediaMessage
        var miniProgramObject: WXMiniProgramObject? = null
        var webpageObject: WXWebpageObject? = null

        if (miniId != null && "" != miniId && miniPath != null && "" != miniPath) {
            miniProgramObject = WXMiniProgramObject()
            // 低版本微信将打开网页分享
            miniProgramObject.webpageUrl = targetUrl
            // 目标小程序的原始ID
            miniProgramObject.userName = miniId
            // 小程序path
            miniProgramObject.path = miniPath
        } else {
            webpageObject = WXWebpageObject()
            webpageObject.webpageUrl = targetUrl
        }

        message = WXMediaMessage(miniProgramObject ?: webpageObject)
        message.title = title
        message.description = summary
        message.thumbData = bytes

        sendMessage(platform, message, buildTransaction("webPage"))
    }

    override fun shareImage(platform: SharePlatform, shareImageObject: ShareImageObject,
                            activity: Activity, listener: ShareListener) {

        Flowable.create(FlowableOnSubscribe<Pair<Bitmap, ByteArray>> { emitter ->
            try {
                val imagePath = ImageDecoder.decode(activity, shareImageObject)
                emitter.onNext(Pair.create(BitmapFactory.decodeFile(imagePath),
                        ImageDecoder.compress2Byte(imagePath, TARGET_SIZE, THUMB_SIZE)))
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest { listener.shareRequest() }
                .subscribe({ pair ->
                    val imageObject = WXImageObject(pair.first)

                    val message = WXMediaMessage()
                    message.mediaObject = imageObject
                    message.thumbData = pair.second

                    sendMessage(platform, message, buildTransaction("image"))
                }, { throwable ->
                    activity.finish()
                    listener.shareFailure(Exception(throwable))
                })
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        mIWXAPI.handleIntent(data, object : IWXAPIEventHandler {
            override fun onReq(baseReq: BaseReq) {}

            override fun onResp(baseResp: BaseResp) {
                when (baseResp.errCode) {
                    BaseResp.ErrCode.ERR_OK -> ShareUtil.mShareListener?.shareSuccess()
                    BaseResp.ErrCode.ERR_USER_CANCEL -> ShareUtil.mShareListener?.shareCancel()
                    else -> ShareUtil.mShareListener?.shareFailure(Exception(baseResp.errStr))
                }
            }
        })
    }

    override fun isInstall(context: Context): Boolean {
        return mIWXAPI.isWXAppInstalled
    }

    override fun recycle() {
        mIWXAPI.detach()
    }

    private fun sendMessage(platform: SharePlatform, message: WXMediaMessage, transaction: String) {
        val req = SendMessageToWX.Req()
        req.transaction = transaction
        req.message = message

        // 小程序类型分享到会话区域 与SharePlatform.WX一致
        req.scene = if (platform == SharePlatform.WX_TIMELINE)
            SendMessageToWX.Req.WXSceneTimeline
        else
            SendMessageToWX.Req.WXSceneSession
        mIWXAPI.sendReq(req)
    }

    private fun buildTransaction(type: String): String {
        return System.currentTimeMillis().toString() + type
    }

    companion object {

        private const val THUMB_SIZE = 32 * 1024 * 8
        private const val TARGET_SIZE = 200
    }
}
