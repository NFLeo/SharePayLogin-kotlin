package com.leo.shareloginpay.share

import android.graphics.Bitmap
import android.util.Pair

/**
 * Describe : 微博支持资源图片分享
 * Created by Leo on 2018/6/22.
 */
class ShareImageObject(private val mObject: Any) {
    var bitmap: Bitmap? = null
    var pathOrUrl: String? = null
    var isShareImmediate: Boolean = false
    var imageRes: Int = 0
    var bytes: ByteArray? = null
    var pair: Pair<String, ByteArray>? = null

    init {
        when (mObject) {
            is Bitmap -> bitmap = mObject
            is String -> pathOrUrl = mObject
            is ByteArray -> bytes = mObject
            is Pair<*, *> -> pair = mObject as Pair<String, ByteArray>
            is Int -> imageRes = mObject
        }
    }

    fun returnImageType(): Int {
        return when (mObject) {
            is Bitmap -> IMAGE_TYPE_BITMAP
            is String -> IMAGE_TYPE_PATH
            is ByteArray -> IMAGE_TYPE_BYTE
            is Pair<*, *> -> IMAGE_TYPE_PAIR
            is Int -> IMAGE_TYPE_RES
            else -> -1
        }
    }

    companion object {

        const val IMAGE_TYPE_BITMAP = 0X186221
        const val IMAGE_TYPE_PATH = 0X186222
        const val IMAGE_TYPE_RES = 0X186223
        const val IMAGE_TYPE_BYTE = 0X186224
        const val IMAGE_TYPE_PAIR = 0X186225
    }
}
