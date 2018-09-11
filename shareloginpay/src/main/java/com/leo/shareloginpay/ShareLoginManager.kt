package com.leo.shareloginpay

object ShareLoginManager {
    var isInit = false

    var CONFIG = ShareLoginConfig()

    fun initManager(config: ShareLoginConfig) {
        isInit = true
        CONFIG = config
    }
}