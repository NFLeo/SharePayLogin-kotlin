package com.leo.shareloginpay.login.result

import org.json.JSONException
import org.json.JSONObject

class WxUser : BaseUser() {

    var city: String? = null

    var country: String? = null

    var province: String? = null

    var unionid: String? = null

    companion object {

        @Throws(JSONException::class)
        fun parse(jsonObject: JSONObject): WxUser {
            val user = WxUser()
            user.openId = jsonObject.getString("openid")
            user.nickname = jsonObject.getString("nickname")
            user.sex = jsonObject.getInt("sex")
            user.headImageUrl = jsonObject.getString("headimgurl")
            user.headImageUrlLarge = jsonObject.getString("headimgurl")       // 重复
            user.province = jsonObject.getString("province")
            user.city = jsonObject.getString("city")
            user.country = jsonObject.getString("country")
            user.unionid = jsonObject.getString("unionid")

            return user
        }
    }
}
