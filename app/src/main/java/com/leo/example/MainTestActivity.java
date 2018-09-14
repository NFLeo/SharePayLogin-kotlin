package com.leo.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.leo.shareloginpay.ShareLoginConfig;
import com.leo.shareloginpay.ShareLoginManager;
import com.leo.shareloginpay.login.LoginListener;
import com.leo.shareloginpay.login.LoginPlatform;
import com.leo.shareloginpay.login.LoginResult;
import com.leo.shareloginpay.login.LoginUtil;
import com.leo.shareloginpay.pay.PayListener;
import com.leo.shareloginpay.share.ShareListener;
import com.leo.simple.R;

import org.jetbrains.annotations.NotNull;

/**
 * Describe :
 * Created by Leo on 2018/9/13 on 18:50.
 */
public class MainTestActivity extends AppCompatActivity implements View.OnClickListener {

    private String APP_ID = "wx41592d9564208b37";
    private String APP_SECRET = "XXXXXXX";

    private LoginListener mLoginListener = new LoginListener() {
        @Override
        public void loginSuccess(@NotNull LoginResult result) {
            Toast.makeText(MainTestActivity.this, "登陆成功 " + result.getUserInfo().getNickname(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void loginFailure(@NotNull Exception e) {
            Toast.makeText(MainTestActivity.this, "登录失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void loginCancel() {
            Toast.makeText(MainTestActivity.this, "取消登录", Toast.LENGTH_SHORT).show();
        }
    };

    private PayListener mPayListener = new PayListener() {
        @Override
        public void paySuccess() {
            Toast.makeText(MainTestActivity.this, "支付成功 ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void payFailed(@NotNull Exception e) {
            Toast.makeText(MainTestActivity.this, "支付失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void payCancel() {
            Toast.makeText(MainTestActivity.this, "取消支付", Toast.LENGTH_SHORT).show();
        }
    };

    private ShareListener mShareListener = new ShareListener() {
        @Override
        public void shareSuccess() {
            Toast.makeText(MainTestActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void shareFailure(@NotNull Exception e) {
            Toast.makeText(MainTestActivity.this, "分享失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void shareCancel() {
            Toast.makeText(MainTestActivity.this, "取消分享", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initConfig();
        initListener();
    }

    private void initConfig() {
//        ShareLoginManager.Companion.getInstance().initManager(new ShareLoginConfig()
//                .qqId("1106618327")
//                .weiboId("1712559958")
//                .wxId("").wxSecret(""));
    }

    private void initListener() {
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_pay).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                final BaseBottomDialog loginDialog = BaseBottomDialog.Companion.newInstance(R.layout.layout_bottom_login);
                loginDialog.show(getSupportFragmentManager(), "loginDialog");

                loginDialog.getDialog().findViewById(R.id.login_wx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loginDialog.dismissParent();
//                        LoginUtil.Companion.getInstance().login(MainTestActivity.this, LoginPlatform.WX, mLoginListener, true);
                    }
                });

                loginDialog.getDialog().findViewById(R.id.login_weibo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loginDialog.dismissParent();
//                        LoginUtil.Companion.getInstance().login(MainTestActivity.this, LoginPlatform.WeiBO, mLoginListener, true);
                    }
                });

                loginDialog.getDialog().findViewById(R.id.login_qq).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loginDialog.dismissParent();
//                        LoginUtil.Companion.getInstance().login(MainTestActivity.this, LoginPlatform.QQ, mLoginListener, true);
                    }
                });
                break;
        }
    }
}
