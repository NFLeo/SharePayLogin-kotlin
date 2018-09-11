package com.leo.shareloginpay.login.result

import org.json.JSONException
import org.json.JSONObject

class WxToken : BaseToken() {

    var refreshToken: String? = null

    companion object {

        @Throws(JSONException::class)
        fun parse(jsonObject: JSONObject): WxToken {
            val wxToken = WxToken()
            wxToken.openid = jsonObject.getString("openid")
            wxToken.accessToken = jsonObject.getString("access_token")
            wxToken.refreshToken = jsonObject.getString("refresh_token")
            return wxToken
        }
    }
}
