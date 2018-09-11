package com.leo.shareloginpay.login

import com.leo.shareloginpay.login.result.BaseToken
import com.leo.shareloginpay.login.result.BaseUser

class LoginResult {

    var token: BaseToken? = null
    var userInfo: BaseUser? = null
    var platform: LoginPlatform

    constructor(platform: LoginPlatform, token: BaseToken) {
        this.platform = platform
        this.token = token
    }

    constructor(platform: LoginPlatform, token: BaseToken, userInfo: BaseUser) {
        this.platform = platform
        this.token = token
        this.userInfo = userInfo
    }
}
