package com.leo.shareloginpay.login.result

import org.json.JSONException
import org.json.JSONObject

class QQToken : BaseToken() {
    companion object {

        @Throws(JSONException::class)
        fun parse(jsonObject: JSONObject): QQToken {
            val token = QQToken()
            token.accessToken = jsonObject.getString("access_token")
            token.openid = jsonObject.getString("openid")
            return token
        }
    }

}
