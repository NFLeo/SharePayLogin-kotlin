package com.leo.shareloginpay

class ShareLoginConfig {

    var wxId: String? = null
    var wxSecret: String? = null
    var qqId: String? = null
    var weiboId: String? = null
    var weiboRedirectUrl = "https://api.weibo.com/oauth2/default.html"
    var weiboScope = "email"
    var isDebug: Boolean = false

    fun wxId(id: String): ShareLoginConfig {
        wxId = id
        return this
    }

    fun wxSecret(id: String): ShareLoginConfig {
        wxSecret = id
        return this
    }

    fun qqId(id: String): ShareLoginConfig {
        qqId = id
        return this
    }

    fun weiboId(id: String): ShareLoginConfig {
        weiboId = id
        return this
    }

    fun weiboRedirectUrl(url: String): ShareLoginConfig {
        weiboRedirectUrl = url
        return this
    }

    fun weiboScope(scope: String): ShareLoginConfig {
        weiboScope = scope
        return this
    }

    fun debug(isDebug: Boolean): ShareLoginConfig {
        this.isDebug = isDebug
        return this
    }

    companion object {

        fun instance(): ShareLoginConfig {
            return ShareLoginConfig()
        }
    }
}
