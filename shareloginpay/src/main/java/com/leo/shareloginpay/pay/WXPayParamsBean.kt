/*
******************************* Copyright (c)*********************************\
**
**                 (c) Copyright 2017, King, china
**                          All Rights Reserved
**                                
**                              By(King)
**                         
**------------------------------------------------------------------------------
*/
package com.leo.shareloginpay.pay

/**
 * Describe : 微信支付参数
 * Created by Leo on 2018/5/7.
 */
class WXPayParamsBean : IPayParamsBean {
    /**
     * sign : ECE311C3DF76E009E6F37F05C350625F
     * timestamp : 1474886901
     * partnerid : 1391669502
     * package : Sign=WXPay
     * appid : wx46a24ab145becbde
     * nonceStr : 0531a4a42fa846fe8a7563847cd24c2a
     * prepayId : wx20160926184820acbd9357100240402425
     */
    var sign: String? = null
    var timestamp: String? = null
    var partnerid: String? = null
    var packageValue: String? = null
    var appid: String? = null
    var nonceStr: String? = null
    var prepayId: String? = null
}
