package com.leo.shareloginpay.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.text.TextUtils

import com.leo.shareloginpay.ShareLogger

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSink
import okio.Okio

object ImageDecoder {

    private const val FILE_NAME = "share_image.jpg"

    @Throws(Exception::class)
    fun decode(context: Context, imageObject: ShareImageObject): String {
        val resultFile = cacheFile(context)

        if (!TextUtils.isEmpty(imageObject.pathOrUrl)) {
            return decode(context, imageObject.pathOrUrl)
        } else if (imageObject.bitmap != null) {
            // save bitmap to file
            val outputStream = FileOutputStream(resultFile)
            imageObject.bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            return resultFile.absolutePath
        } else if (imageObject.imageRes != 0) {
            // save bitmap to file
            val bitmap = BitmapFactory.decodeResource(context.resources, imageObject.imageRes)
            val outputStream = FileOutputStream(resultFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            return resultFile.absolutePath
        } else {
            throw IllegalArgumentException()
        }
    }

    @Throws(Exception::class)
    private fun decode(context: Context, pathOrUrl: String?): String {
        val resultFile = cacheFile(context)

        return if (File(pathOrUrl!!).exists()) {
            // copy file
            decodeFile(File(pathOrUrl), resultFile)
        } else if (HttpUrl.parse(pathOrUrl) != null) {
            // download image
            downloadImageToUri(pathOrUrl, resultFile)
        } else {
            throw IllegalArgumentException("Please input a file path or http url")
        }
    }

    @Throws(IOException::class)
    private fun downloadImageToUri(url: String, resultFile: File): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val sink = Okio.buffer(Okio.sink(resultFile))
        sink.writeAll(response.body()!!.source())

        sink.close()
        response.close()

        return resultFile.absolutePath
    }

    @Throws(Exception::class)
    private fun cacheFile(context: Context): File {
        val state = Environment.getExternalStorageState()
        return if (state != null && state == Environment.MEDIA_MOUNTED) {
            File(context.getExternalFilesDir(""), FILE_NAME)
        } else {
            throw Exception(ShareLogger.INFO.SD_CARD_NOT_AVAILABLE)
        }
    }

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(4096)
        while (-1 != inputStream.read(buffer)) {
            outputStream.write(buffer)
        }

        outputStream.flush()
        inputStream.close()
        outputStream.close()
    }

    @Throws(IOException::class)
    private fun decodeFile(origin: File, result: File): String {
        copyFile(FileInputStream(origin), FileOutputStream(result, false))
        return result.absolutePath
    }

    fun compress2Byte(imagePath: String, size: Int, length: Int): ByteArray {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)

        val outH = options.outHeight
        val outW = options.outWidth
        var inSampleSize = 1

        while (outH / inSampleSize > size || outW / inSampleSize > size) {
            inSampleSize *= 2
        }

        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(imagePath, options)

        val result = ByteArrayOutputStream()
        var quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)
        if (result.size() > length) {
            result.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)
        }

        bitmap.recycle()
        return result.toByteArray()
    }
}
