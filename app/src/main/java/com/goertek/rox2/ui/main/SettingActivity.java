package com.goertek.rox2.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.tws.entity.TWSBattery;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.common.utils.Utils;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingActivity extends BaseActivity {

    private ImageView settingBack;
    private TextView gameModeTextView;
    private TextView multiPointVConnectTextView;
    private TextView QHSTextView;
    private TextView HDAudioTextView;
    private TextView lipSyncTextView;
    private TextView wearDetectTextView;
    private TextView updateTextView;

    private boolean gameModeSwitch = true;
    private boolean multiPointVConnectSwitch = false;
    private boolean QHSSwitch = false;
    private boolean HDAudioSwitch = true;
    private boolean lipSyncSwitch = true;
    private boolean wearDetectSwitch = true;
    private List<TextView> switchList;
    private List<Boolean> switchListBoolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        setListener();

        Utils.getSettings((byte) (0x3f), new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] object) {
                LogUtils.d("setting 回复="+ ProtocolUtils.bytesToHexStr(object)+"object.length="+object.length);
                for (int i=0; i<(object.length-6)/3; i++){
                    int x = 3 * i+5;
                    LogUtils.d("3 * i+5="+x+"object[3*i+7]"+object[3*i+7]);
                    switchChanged(switchList.get((int) object[3 * i+5]), object[3*i+7]==0x01);
                    switchListBoolean.set((int) object[3 * i+5],object[3*i+7]==0x01);
                }
            }

            @Override
            public void onFailed(int errorCode) {

            }
        });
    }

    private void initView() {
        settingBack = findViewById(R.id.tv_setting_back);
        gameModeTextView = findViewById(R.id.tv_game_mode);
        multiPointVConnectTextView = findViewById(R.id.tv_multi_point_connect);
        QHSTextView = findViewById(R.id.tv_qhs);
        HDAudioTextView = findViewById(R.id.tv_hd_aduio);
        lipSyncTextView = findViewById(R.id.tv_lip_sync);
        wearDetectTextView = findViewById(R.id.tv_wear_detect);
        updateTextView = findViewById(R.id.tv_update);
        switchList = new ArrayList<>();
        switchList.add(gameModeTextView);
        switchList.add(multiPointVConnectTextView);
        switchList.add(QHSTextView);
        switchList.add(HDAudioTextView);
        switchList.add(lipSyncTextView);
        switchList.add(wearDetectTextView);
        switchListBoolean = new ArrayList<>();
        switchListBoolean.add(gameModeSwitch);
        switchListBoolean.add(multiPointVConnectSwitch);
        switchListBoolean.add(QHSSwitch);
        switchListBoolean.add(HDAudioSwitch);
        switchListBoolean.add(lipSyncSwitch);
        switchListBoolean.add(wearDetectSwitch);
    }

    private void setListener() {
        settingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        gameModeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameModeSwitch = !switchListBoolean.get(0);
                switchListBoolean.set(0,gameModeSwitch);
                byte[] data;
                if (gameModeSwitch){
                    data = new byte[]{0x00,0x01};
                }else {
                    data = new byte[]{0x00,0x00};
                }
                Utils.setSettings(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        if (object[4]==0x00){
                            switchChanged(gameModeTextView,gameModeSwitch);
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });

        multiPointVConnectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multiPointVConnectSwitch = !switchListBoolean.get(1);
                switchListBoolean.set(1,multiPointVConnectSwitch);
                byte[] data;
                if (multiPointVConnectSwitch){
                    data = new byte[]{0x01,0x01};
                }else {
                    data = new byte[]{0x01,0x00};
                }
                Utils.setSettings(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        if (object[4]==0x00){
                            switchChanged(multiPointVConnectTextView,multiPointVConnectSwitch);
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });

        QHSTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QHSSwitch = !switchListBoolean.get(2);
                switchListBoolean.set(2,QHSSwitch);
                byte[] data;
                if (QHSSwitch){
                    data = new byte[]{0x02,0x01};
                }else {
                    data = new byte[]{0x02,0x00};
                }
                Utils.setSettings(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        if (object[4]==0x00){
                            switchChanged(QHSTextView,QHSSwitch);
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });

        HDAudioTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HDAudioSwitch = !switchListBoolean.get(3);
                switchListBoolean.set(3,HDAudioSwitch);
                byte[] data;
                if (HDAudioSwitch){
                    data = new byte[]{0x03,0x01};
                }else {
                    data = new byte[]{0x03,0x00};
                }
                Utils.setSettings(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        if (object[4]==0x00){
                            switchChanged(HDAudioTextView,HDAudioSwitch);
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });

        lipSyncTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lipSyncSwitch = !switchListBoolean.get(4);
                switchListBoolean.set(4,lipSyncSwitch);
                byte[] data;
                if (lipSyncSwitch){
                    data = new byte[]{0x04,0x01};
                }else {
                    data = new byte[]{0x04,0x00};
                }
                Utils.setSettings(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        if (object[4]==0x00){
                            switchChanged(lipSyncTextView,lipSyncSwitch);
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });

        wearDetectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wearDetectSwitch = !switchListBoolean.get(5);
                switchListBoolean.set(5,wearDetectSwitch);
                byte[] data;
                if (wearDetectSwitch){
                    data = new byte[]{0x05,0x01};
                }else {
                    data = new byte[]{0x05,0x00};
                }
                Utils.setSettings(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        if (object[4]==0x00){
                            switchChanged(wearDetectTextView,wearDetectSwitch);
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {
                    }
                });
            }
        });

        updateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingActivity.this,"turn on updating-app to update the firmWare, if you don't finish it, you can't connect to the earPhone",Toast.LENGTH_LONG).show();
                Utils.updateFirmWare(new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {

                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });
    }

    private void switchChanged(TextView textView,boolean isOn){
        Drawable drawable;
        if (isOn){
            drawable = getResources().getDrawable(R.drawable.switch_on);
        }else {
            drawable = getResources().getDrawable(R.drawable.switch_off);
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(null,
                null, drawable, null);
    }
    @Override
    public void onEvent(Event event) {
        switch (event.getCode()) {
            case Const.EventCode.ConnectStateCode:
                onConnectState((int) event.getData());
                break;

            default:
                break;
        }
    }

    @Override
    protected boolean isRegisterLocalBroadcast() {
        return true;
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
                Intent intent = new Intent(SettingActivity.this,UnConnectedActivity.class);
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