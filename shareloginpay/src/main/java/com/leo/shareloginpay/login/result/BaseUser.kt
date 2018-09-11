package com.leo.shareloginpay.login.result

open class BaseUser {

    /**
     * sex
     * 0. 未知
     * 1. 男
     * 2. 女
     */

    var openId: String? = null

    var nickname: String? = null

    var sex: Int = 0

    var headImageUrl: String? = null

    var headImageUrlLarge: String? = null
}
