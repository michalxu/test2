package com.goertek.rox2.ui.main.hearing_test.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.protocol.tws.entity.TWSBattery;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.MainActivity;
import com.goertek.rox2.ui.main.UnConnectedActivity;
import com.lyratone.hearingaid.audio.EHPP;
import com.lyratone.hearingaid.audio.LyratoneTestFitting;

import java.util.HashMap;
import java.util.Map;

public class HearingEnvironmentTestActivity extends BaseActivity {

    private Button btnNext;
    private ImageView tvHearingTestBack;
    private TextView hearingEnvironmentRecommend;
    private TextView hearingEnvironmentLevel;
    // 噪音检测的数值获取方式 Handler
    Handler mHandler;
    // 聆通SDK调用对象（测听验配模块）
    private LyratoneTestFitting lyratoneTestFitting;
    // 默认的 环境音检测调整值，可自行修改
    double offset = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_environment_test);
        requestPermission();
        initView();
        setClickListener();
        lyratoneInit();

    }
    private void requestPermission(){
        // 获取权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int REQUEST_CODE_PERMISSION_STORAGE = 1;
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            for (String str : permissions) {
                if (ActivityCompat.checkSelfPermission(this, str) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_STORAGE);
                }
            }
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void initView() {
        //进入测听
        btnNext = findViewById(R.id.btn_next_envrionment_test);
        //返回键，处于标题textView中
        tvHearingTestBack = findViewById(R.id.tv_environment_test_back);
        hearingEnvironmentLevel = findViewById(R.id.hearing_environment_level);
        hearingEnvironmentRecommend = findViewById(R.id.hearing_environment_recommend);
    }

    private void setClickListener() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lyratoneTestFitting.pauseNoiseMeter();
                Intent intent = new Intent(HearingEnvironmentTestActivity.this,HearingTestPreparedActivity.class);
                startActivity(intent);
            }
        });

        tvHearingTestBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void lyratoneInit() {
        // Lyratone SDK init
        lyratoneTestFitting = new LyratoneTestFitting(HearingEnvironmentTestActivity.this);
        // 噪音检测的数值获取方式 Handler Msg
        // obj是数值；what判是否为1,1时表示采集音量大于60dB
        mHandler = new Handler() {

            // 处理环境音线程发送的消息。
            // sdk 200毫秒发一次
            @Override
            public void handleMessage(android.os.Message msg) {
                int dbVal = Integer.parseInt(msg.obj.toString());
                if (dbVal<30){
                    hearingEnvironmentLevel.setText("Quiet");
                    Drawable drawable = getResources().getDrawable(R.drawable.img_noise_quiet);
                    hearingEnvironmentLevel.setBackground(drawable);
                    btnNext.setBackground(getResources().getDrawable(R.drawable.michal_bg_btn_next_blue));
                    btnNext.setEnabled(true);
                    hearingEnvironmentRecommend.setText("Suitable for testing");
                    hearingEnvironmentRecommend.setTextColor(Color.parseColor("#2378F1"));
                }else if (dbVal>=30 && dbVal<60){
                    hearingEnvironmentLevel.setText("Medium");
                    Drawable drawable = getResources().getDrawable(R.drawable.img_noise_medium);
                    hearingEnvironmentLevel.setBackground(drawable);
                    btnNext.setBackground(getResources().getDrawable(R.drawable.michal_bg_btn_next_blue));
                    btnNext.setEnabled(true);
                    hearingEnvironmentRecommend.setText("Suitable for testing");
                    hearingEnvironmentRecommend.setTextColor(Color.parseColor("#2378F1"));
                }else {
                    hearingEnvironmentLevel.setText("Noisy");
                    Drawable drawable = getResources().getDrawable(R.drawable.img_noise_noisy);
                    hearingEnvironmentLevel.setBackground(drawable);
                    btnNext.setBackground(getResources().getDrawable(R.drawable.michal_bg_btn_next_gray));
                    btnNext.setEnabled(false);
                    hearingEnvironmentRecommend.setText("Not suitable to test");
                    hearingEnvironmentRecommend.setTextColor(Color.parseColor("#F1A332"));
                }
            }

        };
        // 噪音检测的数值获取方式 Handler Msg
        lyratoneTestFitting.setNoiseMeterHandler(mHandler);
        lyratoneTestFitting.setNoiseThreshold(60); // if over this threshold(dB spl) nmHandler's msg.what is 1
        lyratoneTestFitting.setMeterInterval(3); //interval unit sec
        lyratoneTestFitting.setNoiseMeterOffset(offset);
        lyratoneTestFitting.startNoiseMeter();
    }

    @Override
    protected void onResume() {
        if (lyratoneTestFitting != null) {
            lyratoneTestFitting.stopTestAudio();
            lyratoneTestFitting.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lyratoneTestFitting!=null){
            LogUtils.d("停止环境音检测");
            lyratoneTestFitting.pauseNoiseMeter();
        }
    }
    @Override
    protected boolean isRegisterLocalBroadcast() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getCode() == Const.EventCode.ConnectStateCode) {
            onConnectState((int) event.getData());
        }
    }
    /**
     * 连接状态变化
     *
     * @param state 连接状态
     */
    private void onConnectState(int state) {
        switch (state) {
            case ConnectState.STATE_DISCONNECTED:
                finish();
                Intent intent = new Intent(HearingEnvironmentTestActivity.this, UnConnectedActivity.class);
                startActivity(intent);
                break;
            case ConnectState.STATE_CONNECTING:
            case ConnectState.STATE_CONNECTED:
//                mBluetoothTextView.setText(R.string.drawer_bluetooth_connecting);
                break;
            case ConnectState.STATE_DATA_READY:
//                mBluetoothTextView.setText(R.string.drawer_bluetooth_connected);
                break;
            default:
                break;
        }
    }
}