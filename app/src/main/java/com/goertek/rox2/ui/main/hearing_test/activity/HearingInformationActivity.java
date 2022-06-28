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
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.MainActivity;
import com.goertek.rox2.ui.main.UnConnectedActivity;
import com.goertek.rox2.ui.main.utils.CustomHorizontalNumber;

public class HearingInformationActivity extends BaseActivity {

    private CustomHorizontalNumber numberPicker;
    private TextView tvMale;
    private TextView tvFemale;
    private Button btnNext;
    private boolean sex = true;  //true则为男
    private ImageView hearingInfoBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_information);
        initView();
        setClickListener();
    }

    private void initView() {
        hearingInfoBack = findViewById(R.id.tv_hearing_info_back);
        numberPicker = findViewById(R.id.number_picker);
        tvMale = findViewById(R.id.tv_male);
        tvFemale = findViewById(R.id.tv_female);
        btnNext = findViewById(R.id.btn_next_info);
    }

    private void setClickListener() {
        hearingInfoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tvMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvMale.setBackground(ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.gender_bg_blue,null));
                tvMale.setTextColor(Color.WHITE);
                Drawable drawable = ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.icon_male_white,null);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                tvMale.setCompoundDrawables(null,drawable,null,null);
                tvFemale.setBackground(ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.gender_bg_white,null));
                tvFemale.setTextColor(Color.parseColor("#091931"));
                Drawable drawableFemale = ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.icon_female_white,null);
                drawableFemale.setBounds(0,0,drawableFemale.getMinimumWidth(),drawableFemale.getMinimumHeight());
                tvFemale.setCompoundDrawables(null,drawableFemale,null,null);
                sex = true;
            }
        });
        tvFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvFemale.setBackground(ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.gender_bg_blue,null));
                tvFemale.setTextColor(Color.WHITE);
                Drawable drawableFemale = ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.icon_female_black,null);
                drawableFemale.setBounds(0,0,drawableFemale.getMinimumWidth(),drawableFemale.getMinimumHeight());
                tvFemale.setCompoundDrawables(null,drawableFemale,null,null);

                Drawable drawableMale = ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.icon_male_black,null);
                drawableMale.setBounds(0,0,drawableMale.getMinimumWidth(),drawableMale.getMinimumHeight());
                tvMale.setCompoundDrawables(null,drawableMale,null,null);
                tvMale.setBackground(ResourcesCompat.getDrawable(HearingInformationActivity.this.getResources(),R.drawable.gender_bg_white,null));
                tvMale.setTextColor(Color.parseColor("#091931"));
                sex = false;
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HearingInformationActivity.this,HearingTestActivity.class);
                intent.putExtra("userAge", numberPicker.getAge());
                intent.putExtra("userSex",sex);
                startActivity(intent);
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
                Intent intent = new Intent(HearingInformationActivity.this, UnConnectedActivity.class);
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