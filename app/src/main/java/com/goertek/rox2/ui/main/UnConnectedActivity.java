package com.goertek.rox2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.common.utils.SharedPreferenceUtils;
import com.goertek.db.bean.HeartRateTable;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.utils.timeUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import static com.goertek.rox2.ui.main.MainActivity.setViewHeight;

public class UnConnectedActivity extends BaseActivity {

    private TextView tvConnectState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_connected);
        isRegisterLocalBroadcast();
        TextView tvRetry = findViewById(R.id.retry_connect);
        View heartRateView = findViewById(R.id.launch_to_test_heart_rate);
        TextView  heartRrateValue = findViewById(R.id.tv_launch_heart_rate_value);
        TextView HeartRateTestTime = findViewById(R.id.tv_launch_heart_time);
        TextView heartRateUint = findViewById(R.id.launch_heart_rate_uint);
        TextView heartRateText = findViewById(R.id.launch_heart_rate_text);
        tvConnectState = findViewById(R.id.tv_connect_state);
        //重连
        tvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
        //在数据库中查询最新的心率数据
        SharedPreferenceUtils.useMichalSharedPreference("latestHeartRate");
        int latestHeartRate = SharedPreferenceUtils.get("heartRate",-1);
        Long latestTime = SharedPreferenceUtils.get("time",-1L);
        if (latestHeartRate!=-1){
            heartRateView.setBackground(getResources().getDrawable(R.drawable.bg_hr_1));
            setViewHeight(UnConnectedActivity.this,heartRateView,200);
            heartRrateValue.setVisibility(View.VISIBLE);
            HeartRateTestTime.setVisibility(View.VISIBLE);
            heartRateUint.setVisibility(View.VISIBLE);
            heartRrateValue.setText(latestHeartRate+"");
            String date = timeUtils.timeStampToDate(latestTime,"");
            String latestMonth = date.substring(5,7);
            String latestDay = date.substring(8,10);
            int latestHour = Integer.parseInt(date.substring(11,13));
            String latestMin = date.substring(14,16);
            if (latestHour>12){
                latestHour = latestHour-12;
                NumberFormat numberFormat = new DecimalFormat("00");
                HeartRateTestTime.setText("Latest: "+numberFormat.format(latestHour)+":"+latestMin+" PM "+latestMonth+"/"+latestDay);
            }else {
                HeartRateTestTime.setText("Latest: "+date.substring(11,13)+":"+latestMin+" AM "+latestMonth+"/"+latestDay);
            }
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            LogUtils.d("year="+year+",month="+month+",day="+day);
            RoxLitePal litePal = RoxLitePal.getInstance();
            List<HeartRateTable> heartRateTableList = litePal.roxLitePalCheck.getHeartRateTable(year,month,day);
            int minHeartRate = -1;
            int maxHeartRate = -1;
            LogUtils.d("heartRateTableList.size()="+heartRateTableList.size());
            if (heartRateTableList.size()>0){
                int tempMin;
                int tempMax;
                for (int i=0;i<heartRateTableList.size();i++){
                    tempMin = heartRateTableList.get(i).getLowHeartRate();
                    if (minHeartRate == -1){
                        minHeartRate = tempMin;
                    }
                    if (minHeartRate>tempMin){
                        minHeartRate = tempMin;
                    }
                    tempMax = heartRateTableList.get(i).getHighHeartRate();
                    if (maxHeartRate == -1){
                        maxHeartRate = tempMax;
                    }
                    if (maxHeartRate<tempMax){
                        maxHeartRate = tempMax;
                    }
                }
                heartRateText.setVisibility(View.VISIBLE);
                heartRateText.setText("Your heart rate ranged from "+minHeartRate+" to "+maxHeartRate+" today.");
            }else {
                heartRateText.setVisibility(View.INVISIBLE);
            }
        }else {
            heartRateText.setVisibility(View.GONE);
            heartRateView.setBackground(getResources().getDrawable(R.drawable.bg_hr_2));
            setViewHeight(UnConnectedActivity.this,heartRateView,108);
            heartRrateValue.setVisibility(View.GONE);
            HeartRateTestTime.setVisibility(View.GONE);
            heartRateUint.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isRegisterLocalBroadcast() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        LogUtils.d("onEvent");
        if (event.getCode()==Const.EventCode.ConnectStateCode){
                onConnectState((int) event.getData());
        }
    }
    /**
     * 连接状态变化
     *
     * @param state 连接状态
     */
    private void onConnectState(int state) {
        LogUtils.d("onConnectState");
        switch (state) {
            case ConnectState.STATE_DISCONNECTED:
                tvConnectState.setText(R.string.drawer_bluetooth_disconnect);
                LogUtils.d("STATE_DISCONNECTED");
                break;
            case ConnectState.STATE_CONNECTING:
            case ConnectState.STATE_CONNECTED:
                tvConnectState.setText(R.string.drawer_bluetooth_connecting);
                break;
            case ConnectState.STATE_DATA_READY:
                tvConnectState.setText(R.string.drawer_bluetooth_connected);
                Intent intent = new Intent(UnConnectedActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }
}