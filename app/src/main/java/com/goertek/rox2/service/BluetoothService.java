package com.goertek.rox2.service;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.link.function.UUIDConfig;
import com.goertek.bluetooth.listener.BluetoothListener;
import com.goertek.bluetooth.protocol.ProtocolAPI;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.function.NotifyListener;
import com.goertek.bluetooth.protocol.model.ParseResultEvent;
import com.goertek.bluetooth.protocol.tws.entity.TWSEQParams;
import com.goertek.common.base.BaseService;
import com.goertek.common.event.Event;
import com.goertek.common.utils.EQUtils;
import com.goertek.common.utils.LocalBroadcastUtils;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.common.utils.NotificationUtils;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.common.utils.ToastUtils;
import com.goertek.common.utils.Utils;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.utils.timeUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * 文件名：BluetoothService
 * 描述：蓝牙监听服务
 * 监听系统蓝牙A2DP连接状态，根据系统蓝牙状态控制蓝牙通信链路状态
 * 创建时间：2020/7/31
 * @author jochen.zhang
 */
public class BluetoothService extends BaseService implements NotifyListener {
    private static final String TAG = "BluetoothService";

    /**
     * 开启服务
     *
     * @param context 上下文
     */
    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, BluetoothService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    /**
     * 绑定服务
     *
     * @param context 上下文
     * @param serviceConnection ServiceConnection
     */
    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent serviceIntent = new Intent(context, BluetoothService.class);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationUtils.createDefaultNotificationChannel(this);
        startForeground(NotificationUtils.NOTIFICATION_ID, NotificationUtils.getForegroundServiceNotification(this));
        ProtocolAPI.getDefault().registerChannel(Utils.getDevice(), 500);//Integer.MAX_VALUE
        checkConnectState();
        LogUtils.i("BluetoothService onCreate");
        ProtocolAPI.getDefault().registerNotifyListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ProtocolAPI.getDefault().unregisterNotifyListener(this);
        ProtocolAPI.getDefault().releaseChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 根据系统当前A2DP连接状态，更新设备通信链路状态
     */
    private void checkConnectState() {
        BluetoothDevice device = BluetoothListener.getInstance().getFocusDevice();
        if (device == null) {
            Utils.getDevice().disconnect();
        } else {
            Utils.getDevice().connect(device.getAddress(), UUIDConfig.getSppConfig());
        }
    }

    @Override
    protected boolean isRegisterLocalBroadcast() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        switch (event.getCode()) {
            case Const.EventCode.BluetoothDisconnectedCode:
                LogUtils.d("BluetoothDisconnectedCode");
                break;
            case Const.EventCode.BluetoothConnectedCode:
                // 系统蓝牙有设备连接
                checkConnectState();
                break;
            case Const.EventCode.ConnectStateCode:
                switch ((int) event.getData()) {
                    case ConnectState.STATE_DISCONNECTED:
                        // 设备断连
                        // 1、移除耳机EQParams
                        EQUtils.getInstance().removeParams();
                        // 2、EQ状态刷新
                        LocalBroadcastUtils.post(new Event<>(Const.EventCode.EQRefreshStateCode, false));
                        // 3、2s后再次检测耳机连接状态
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkConnectState();
                            }
                        }, 2000);
                        break;
                    case ConnectState.STATE_DATA_READY:
//                        // 1、查询耳机当前EQ
//                        LogUtils.d("ConnectState.STATE_DATA_READY");
//                        Utils.getEq(new IRspListener<TWSEQParams>() {
//                            @Override
//                            public void onSuccess(TWSEQParams object) {
//                                LogUtils_goertek.i(TAG, "getEq success " + object);
//                                LocalBroadcastUtils.post(new Event<>(Const.EventCode.EQRefreshStateCode, true));
//                            }
//
//                            @Override
//                            public void onFailed(int errorCode) {
//                                LogUtils.d("get EQ fail");
//                            }
//                        });
//
//                        // 2、查询电量
//                        Utils.refreshBattery();
//
//                        //3、查询ANC状态
//                        Utils.getNoiceControlStatus(new IRspListener<byte[]>() {
//                            @Override
//                            public void onSuccess(byte[] payolad) {
//                                LocalBroadcastUtils.post(new Event<>(Const.EventCode.EQRefreshStateCode,payolad));
//                            }
//
//                            @Override
//                            public void onFailed(int errorCode) {
//                                LogUtils.d("get ANC fail");
//                            }
//                        });
//                        //4、查询setting的状态
//                        Utils.getSettings(new IRspListener<byte[]>() {
//                            @Override
//                            public void onSuccess(byte[] object) {
//                                LocalBroadcastUtils.post(new Event<>(Const.EventCode.SettingsStateCode,object));
//                            }
//
//                            @Override
//                            public void onFailed(int errorCode) {
//
//                            }
//                        });
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;

        }
    }

    @Override
    public void onNotify(ParseResultEvent result) {

        //主动上报数据，是心率数据，更新最新的心率数据
        byte[] payload = result.getPayload();
        LogUtils.d("onNotify payload = ="+ProtocolUtils.bytesToHexStr(payload));
        int heartRate = Utils.byteToInt(payload[5]);
        RoxLitePal litePal = RoxLitePal.getInstance();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+1;
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        //获取整点的时间戳
        long timeStamp = timeUtils.getTimeStampForHour(year,month,day,hour);
        //添加心率数据到数据库
        if (litePal ==null){
            litePal = RoxLitePal.getInstance();
        }
        litePal.roxLitePalAdd.addHeartRate(litePal,heartRate,timeStamp);
    }
}
