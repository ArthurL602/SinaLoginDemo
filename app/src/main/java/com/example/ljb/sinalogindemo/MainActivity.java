package com.example.ljb.sinalogindemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class MainActivity extends AppCompatActivity {

    private Platform mWeibo;
    EventHandler eventHandler = new EventHandler() {
        public void afterEvent(int event, int result, Object data) {
            if (data instanceof Throwable) {
                Throwable throwable = (Throwable) data;
                String msg = throwable.getMessage();
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            } else {
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    // 处理你自己的逻辑
                    Log.e("tag","验证成功");
                }
            }
        }
    };
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCachePermission();

        setContentView(R.layout.activity_main);
        // 如果希望在读取通信录的时候提示用户，可以添加下面的代码，并且必须在其他代码调用之前，否则不起作用；如果没这个需求，可以不加这行代码
        SMSSDK.initSDK(this, "21e0969751bc8","48d67c362ff0b4744858c82ff0a90a7e");

        // 注册监听器
        SMSSDK.registerEventHandler(eventHandler);
//        mWeibo = ShareSDK.getPlatform(SinaWeibo.NAME);

//        Log.e("tag", "登陆认证是否有效: " + mWeibo.isAuthValid());
        // 创建EventHandler对象
        //发送验证码到"<phone number>"
        SMSSDK.getVerificationCode("86",  "15874231850");
//服务器匹配验证码是否正确
        SMSSDK.submitVerificationCode("86","<phone number>", "验证码");

        initButton();
    }

    private void initButton() {
        findViewById(R.id.btn_sina).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mWeibo.showUser(null);//授权并获取用户信息
//回调信息，可以在这里获取基本的授权返回的信息，但是注意如果做提示和UI操作要传到主线程handler里去执行
                mWeibo.setPlatformActionListener(new PlatformActionListener() {

                    @Override
                    public void onError(Platform arg0, int arg1, Throwable arg2) {
                        arg2.printStackTrace();
                        Log.e("tag", "onError: " + arg2.getMessage());
                    }

                    @Override
                    public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
                        Log.e("tag", "onComplete: ");
                        //输出所有授权信息
                        String msg = arg0.getDb().exportData();
                        Log.e("tag", "msg: " + msg);
                    }

                    @Override
                    public void onCancel(Platform arg0, int arg1) {
                        Log.e("tag", "onCancel: ");
                    }
                });
//authorize与showUser单独调用一个即可
//                weibo.authorize();//单独授权,OnComplete返回的hashmap是空的


            }
        });
        findViewById(R.id.btn_sina_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //移除授权
//                mWeibo.removeAccount(true);
                SMSSDK.submitVerificationCode("86","15874231850", mEditText.getText().toString());
            }
        });
        mEditText = (EditText) findViewById(R.id.edit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    //外部存储器读写权限
    public void addCachePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            String permission[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest
                    .permission.READ_CONTACTS};
            int write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            if (write != PackageManager.PERMISSION_GRANTED || read != PackageManager
                    .PERMISSION_GRANTED) {
                requestPermissions(permission, 300);
            }
        }
    }
}
