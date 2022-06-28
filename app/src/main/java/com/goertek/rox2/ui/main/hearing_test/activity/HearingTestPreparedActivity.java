package com.goertek.rox2.ui.main.hearing_test.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.common.utils.MusicUtils;
import com.goertek.common.utils.Utils;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.UnConnectedActivity;

public class HearingTestPreparedActivity extends BaseActivity {
    private Button btnNext;
    private boolean isRecommend = true;
    private TextView tvWearedDetect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_test_prepared);
//        MusicUtils.checkPermission(HearingTestPreparedActivity.this);
        if (MusicUtils.isMusicActive()){
            MusicUtils.stop();
        }
        // 1、音量取半值
        MusicUtils.halfVolume();
        wearDetect();
        //进入测听
        btnNext = findViewById(R.id.btn_next_prepared);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecommend){
                    Intent intent = new Intent(HearingTestPreparedActivity.this, HearingInformationActivity.class);
                    startActivity(intent);
                }
            }
        });
        //返回键，处于标题textView中
        ImageView tvHearingTestBack = findViewById(R.id.tv_hearing_prepared_test_back);
        tvWearedDetect = findViewById(R.id.tv_weared_detect);
        tvHearingTestBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void wearDetect(){
        Utils.getWearState(new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] object) {
                int state = object[5];
                if (state == 0){
                    isRecommend = false;
                }else {
                    isRecommend = true;
                }
                Drawable drawable;
                Drawable wearDrawable;
                if (isRecommend){
                    tvWearedDetect.setTextColor(Color.parseColor("#2378F1"));
                    wearDrawable = ResourcesCompat.getDrawable(HearingTestPreparedActivity.this.getResources(),R.drawable.icon_done,null);
                    drawable = ResourcesCompat.getDrawable(HearingTestPreparedActivity.this.getResources(),R.drawable.michal_bg_btn_next_blue,null);
                }else {
                    tvWearedDetect.setTextColor(Color.parseColor("#FFF1A332"));
                    wearDrawable = ResourcesCompat.getDrawable(HearingTestPreparedActivity.this.getResources(),R.drawable.icon_undo,null);
                    drawable = ResourcesCompat.getDrawable(HearingTestPreparedActivity.this.getResources(),R.drawable.michal_bg_btn_next_gray,null);
                }
                tvWearedDetect.setCompoundDrawables(null,null,wearDrawable,null);
                btnNext.setBackground(drawable);
            }

            @Override
            public void onFailed(int errorCode) {

            }
        });

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
                Intent intent = new Intent(HearingTestPreparedActivity.this, UnConnectedActivity.class);
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