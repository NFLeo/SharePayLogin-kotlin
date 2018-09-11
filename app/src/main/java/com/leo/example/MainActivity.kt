package com.leo.example

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.leo.simple.R
import com.leo.shareloginpay.ShareLoginConfig
import com.leo.shareloginpay.ShareLoginManager
import com.leo.shareloginpay.login.LoginListener
import com.leo.shareloginpay.login.LoginPlatform
import com.leo.shareloginpay.login.LoginResult
import com.leo.shareloginpay.login.LoginUtil
import com.leo.shareloginpay.pay.*
import com.leo.shareloginpay.share.ShareListener
import com.leo.shareloginpay.share.SharePlatform
import com.leo.shareloginpay.share.ShareUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.security.AccessController.getContext

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var APP_ID = "wx41592d9564208b37"
    private var APP_SECRET = "XXXXXXX"

    var mLoginListener = object : LoginListener() {
        override fun loginSuccess(result: LoginResult) {
            Toast.makeText(this@MainActivity, "登陆成功 " + result.userInfo?.nickname, Toast.LENGTH_SHORT).show()
        }

        override fun loginFailure(e: Exception) {
            Toast.makeText(this@MainActivity, "登录失败 " + e.message, Toast.LENGTH_SHORT).show()
        }

        override fun loginCancel() {
            Toast.makeText(this@MainActivity, "登录取消", Toast.LENGTH_SHORT).show()
        }
    }

    var mPayListener = object : PayListener() {
        override fun paySuccess() {
            Toast.makeText(this@MainActivity, "支付成功", Toast.LENGTH_SHORT).show()
        }

        override fun payFailed(e: Exception) {
            Toast.makeText(this@MainActivity, "支付失败 " + e.message, Toast.LENGTH_SHORT).show()
        }

        override fun payCancel() {
            Toast.makeText(this@MainActivity, "支付取消", Toast.LENGTH_SHORT).show()
        }
    }

    var mShareListener = object : ShareListener() {
        override fun shareSuccess() {
            Toast.makeText(this@MainActivity, "分享成功", Toast.LENGTH_SHORT).show()
        }

        override fun shareFailure(e: Exception) {
            Toast.makeText(this@MainActivity, "分享失败", Toast.LENGTH_SHORT).show()
        }

        override fun shareCancel() {
            Toast.makeText(this@MainActivity, "取消分享", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initConfig()
        initListener()
    }

    private fun initConfig() {
        val config = ShareLoginConfig.instance().qqId("1106618327")
                .weiboId("1712559958")
                .wxId(APP_ID).wxSecret(APP_SECRET)
        ShareLoginManager.initManager(config)
    }

    private fun initListener() {
        btn_login.setOnClickListener(this)
        btn_pay.setOnClickListener(this)
        btn_share.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btn_login -> {
                val loginDialog = BaseBottomDialog.newInstance(R.layout.layout_bottom_login)
                loginDialog.show(supportFragmentManager, "loginDialog")
                loginDialog.dialog.findViewById<View>(R.id.login_wx).setOnClickListener {
                    loginDialog.dismissParent()
                    LoginUtil.login(this@MainActivity, LoginPlatform.WX, mLoginListener)
                }

                loginDialog.dialog.findViewById<View>(R.id.login_weibo).setOnClickListener {
                    loginDialog.dismissParent()
                    LoginUtil.login(this@MainActivity, LoginPlatform.WeiBO, mLoginListener)
                }

                loginDialog.dialog.findViewById<View>(R.id.login_qq).setOnClickListener {
                    loginDialog.dismissParent()
                    LoginUtil.login(this@MainActivity, LoginPlatform.QQ, mLoginListener)
                }
            }

            btn_share -> {
                val shareDialog = BaseBottomDialog.newInstance(R.layout.layout_bottom_share)
                shareDialog.show(supportFragmentManager, "shareDialog")

                shareDialog.dialog.findViewById<View>(R.id.ll_wechat).setOnClickListener {
                    shareDialog.dismissParent()
                    ShareUtil.shareMedia(this@MainActivity, SharePlatform.WX, "标题", "内容", "http://www.baidu.com", R.mipmap.ic_launcher, mShareListener)
                }

                shareDialog.dialog.findViewById<View>(R.id.ll_wechat_mini).setOnClickListener {
                    shareDialog.dismissParent()
                    ShareUtil.shareMedia(this@MainActivity, SharePlatform.WX, "标题", "内容", "http://www.baidu.com", BitmapFactory.decodeResource(resources,
                            R.mipmap.ic_launcher), "gh_41bb43658d5e", "share/card/card", mShareListener)
                }

                shareDialog.dialog.findViewById<View>(R.id.ll_wechat_moment).setOnClickListener {
                    shareDialog.dismissParent()
                    ShareUtil.shareImage(this@MainActivity, SharePlatform.WX_TIMELINE,
                            "http://android-screenimgs.25pp.com/fs08/2018/05/11/4/110_f77a9c519c81005292e24f6eb324ea3b_234x360.jpg", mShareListener)
                }

                shareDialog.dialog.findViewById<View>(R.id.ll_weibo).setOnClickListener {
                    shareDialog.dismissParent()
                    ShareUtil.shareText(this@MainActivity, SharePlatform.WeiBO, "测试微博分享文字", mShareListener)
                }

                shareDialog.dialog.findViewById<View>(R.id.ll_qq).setOnClickListener {
                    shareDialog.dismissParent()
                    ShareUtil.shareMedia(this@MainActivity, SharePlatform.QQ, "Title", "summary",
                            "https://www.baidu.com", "http://android-screenimgs.25pp.com/fs08/2018/05/11/4/110_f77a9c519c81005292e24f6eb324ea3b_234x360.jpg",
                            mShareListener)
                }

                shareDialog.dialog.findViewById<View>(R.id.ll_qzone).setOnClickListener {
                    shareDialog.dismissParent()
                    ShareUtil.shareMedia(this@MainActivity, SharePlatform.QZone, "Title", "summary",
                            "https://www.baidu.com", "http://android-screenimgs.25pp.com/fs08/2018/05/11/4/110_f77a9c519c81005292e24f6eb324ea3b_234x360.jpg",
                            mShareListener)
                }

                shareDialog.dialog.findViewById<View>(R.id.ll_system).setOnClickListener {
                    shareDialog.dismissParent()
                    ShareUtil.shareMedia(this@MainActivity, SharePlatform.DEFAULT, "标题", "内容", "http://www.baidu.com", R.mipmap.ic_launcher, mShareListener)
                }
            }

            btn_pay -> {
                val payDialog = BaseBottomDialog.newInstance(R.layout.layout_bottom_pay)
                payDialog.show(supportFragmentManager, "payDialog")

                payDialog.dialog.findViewById<View>(R.id.pay_alipay).setOnClickListener {
                    payDialog.dismissParent()
                    val payParamsBean = AliPayParamsBean()
                    payParamsBean.orderInfo = "xxxx"
                    PayUtil.pay(this@MainActivity, PayPlatform.ALIPAY, payParamsBean, mPayListener)
                }

                payDialog.dialog.findViewById<View>(R.id.pay_wechat).setOnClickListener {
                    payDialog.dismissParent()
                    val wxPayParamsBean = WXPayParamsBean()
                    wxPayParamsBean.appid = "xxxx"
                    wxPayParamsBean.nonceStr = "xxxx"
                    wxPayParamsBean.partnerid = "xxxx"
                    wxPayParamsBean.packageValue = "xxxx"
                    wxPayParamsBean.prepayId = "xxxx"
                    wxPayParamsBean.sign = "xxxx"
                    wxPayParamsBean.timestamp = "xxxx"

                    PayUtil.pay(this@MainActivity, PayPlatform.WXPAY, wxPayParamsBean, mPayListener)
                }
            }
        }
    }
}
