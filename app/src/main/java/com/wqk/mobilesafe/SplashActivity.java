package com.wqk.mobilesafe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wqk.mobilesafe.utils.StreamTools;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import es.dmoral.toasty.Toasty;

public class SplashActivity extends AppCompatActivity {

    private final int ENTER_HOME = 0;
    private final int SHOW_UPDATE_DIALOG = 1;
    private final int URL_ERROR = 2;
    private final int NETWORK_ERROR = 3;
    private final int JSON_ERROR = 4;
    private TextView tv_splash_show_progress;
    private String apkUrl;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            switch (msg.what){
//                case ENTER_HOME:
//                    Toast.makeText(SplashActivity.this, "您已经是最新版本", Toast.LENGTH_LONG).show();
//                case SHOW_UPDATE_DIALOG:
//                    Toast.makeText(SplashActivity.this, "有新的版本需要更新", Toast.LENGTH_LONG).show();
//                case URL_ERROR:
//                    Toast.makeText(SplashActivity.this, "请检查你的网络", Toast.LENGTH_LONG).show();
//                case NETWORK_ERROR:
//                    Toast.makeText(SplashActivity.this, "请检查你的网络", Toast.LENGTH_LONG).show();
//                case JSON_ERROR:
//                    Toast.makeText(SplashActivity.this, "请检查你的网络", Toast.LENGTH_LONG).show();
//                default:
//                        break;
//            }
            Toasty.success(SplashActivity.this, "您已经是最新版本", Toast.LENGTH_SHORT, true).show();
            showUpdateDialog();
        }
    };
    private final static String TAG = "SplashActivity";
    private TextView tv_splash_version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tv_splash_show_progress = findViewById(R.id.tv_splash_show_progress);
        tv_splash_version = findViewById(R.id.tv_splash_version);
        tv_splash_version.setText(this.getVersionName());
        this.checkUpdate();

        AlphaAnimation anim = new AlphaAnimation(0.3f, 1.0f);
        anim.setDuration(500);
        findViewById(R.id.ly_root_splash).startAnimation(anim);
    }

    //网络检查版本,有的话就升级
    private void checkUpdate(){
        new Thread(){
            @Override
            public void run() {
                Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();
                try {
                    URL url = new URL(getString(R.string.serverurl));
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(4000);
                    int code = con.getResponseCode();
                    if (code == 200){
                        String ret = StreamTools.readFromStream(con.getInputStream());
                        JSONObject json = new JSONObject(ret);
                        String vc = json.getString("versioncode");
                        apkUrl = json.getString("apkurl");
                        Log.i(TAG, vc);
                        if (vc.equals(getVersionName())){
                            //版本一致
                            msg.what = ENTER_HOME;
                        }else{
                            //版本不一致，需要更新
                            msg.what = SHOW_UPDATE_DIALOG;
                        }
                    }
                }catch (MalformedURLException e){
                    msg.what = URL_ERROR;
                }catch (IOException e){
                    msg.what = NETWORK_ERROR;
                }catch (JSONException e){
                    msg.what = JSON_ERROR;
                }finally {
                    long endTime = System.currentTimeMillis();
                    long dTime = endTime - startTime;
                    if (dTime < 2000){
                        try {
                            Thread.sleep(2000-dTime);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    handler.sendMessage(msg);

                }

            }
        }.start();
    }

    //获取本地app版本
    private String getVersionName(){
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    //提醒升级
    private void showUpdateDialog(){
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("提示");
        d.setMessage("发现新版本 是否要更新");

        //升级更新
        d.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                FinalHttp fh = new FinalHttp();
                //调用download方法开始下载
                HttpHandler handler = fh.download(apkUrl,
                        "",
                        true,
                        new AjaxCallBack<File>() {
                            @Override
                            public void onStart() {
                                super.onStart();
                            }

                            @Override
                            public void onLoading(long count, long current) {
                                super.onLoading(count, current);
                            }

                            @Override
                            public void onSuccess(File file) {
                                super.onSuccess(file);
                            }

                            @Override
                            public void onFailure(Throwable t, int errorNo, String strMsg) {
                                super.onFailure(t, errorNo, strMsg);
                            }
                        });




            }
        });
        //取消更新，进入主页面
        d.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                enterHome();
            }
        });
        d.show();
    }

    //进入主页
    private void enterHome(){
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        this.startActivity(intent);
        this.finish();
    }
}
