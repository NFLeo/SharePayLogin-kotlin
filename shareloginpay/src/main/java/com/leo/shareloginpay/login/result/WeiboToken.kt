package com.leo.shareloginpay.login.result

import com.sina.weibo.sdk.auth.Oauth2AccessToken

class WeiboToken : BaseToken() {

    var refreshToken: String? = null

    var phoneNum: String? = null

    companion object {

        fun parse(token: Oauth2AccessToken): WeiboToken {
            val target = WeiboToken()
            target.openid = token.uid
            target.accessToken = token.token
            target.refreshToken = token.refreshToken
            target.phoneNum = token.phoneNum
            return target
        }
    }
}
