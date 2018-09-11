package com.leo.shareloginpay.login.result

import android.text.TextUtils
import org.json.JSONException
import org.json.JSONObject

class QQUser : BaseUser() {

    private var qZoneHeadImage: String? = null
    private var qZoneHeadImageLarge: String? = null

    fun getqZoneHeadImage(): String? {
        return qZoneHeadImage
    }

    fun setqZoneHeadImage(qZoneHeadImage: String) {
        this.qZoneHeadImage = qZoneHeadImage
    }

    fun getQZoneHeadImageLarge(): String? {
        return qZoneHeadImageLarge
    }

    fun setQZoneHeadImageLarge(qZoneHeadImageLarge: String) {
        this.qZoneHeadImageLarge = qZoneHeadImageLarge
    }

    companion object {

        @Throws(JSONException::class)
        fun parse(openId: String, jsonObject: JSONObject): QQUser {
            val user = QQUser()
            user.nickname = jsonObject.getString("nickname")
            user.openId = openId
            user.sex = if (TextUtils.equals("男", jsonObject.getString("gender"))) 1 else 2
            user.headImageUrl = jsonObject.getString("figureurl_qq_1")
            user.headImageUrlLarge = jsonObject.getString("figureurl_qq_2")
            user.setqZoneHeadImage(jsonObject.getString("figureurl_1"))
            user.setQZoneHeadImageLarge(jsonObject.getString("figureurl_2"))

            return user
        }
    }
}
