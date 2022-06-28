package com.goertek.bluetooth.listener;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.goertek.common.utils.LocalBroadcastUtils;
import com.goertek.common.event.Event;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.rox2.common.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：BluetoothListener
 * 描述：蓝牙A2DP状态监听
 * 创建时间：2020/7/30
 * @author jochen.zhang
 */
public class BluetoothListener {
    private static final String TAG = "BluetoothListener";
    private static final String DEVICE_NAME = "";

    private ArrayList<BluetoothDevice> connectedDevices = new ArrayList<>();

    public void init(Context context) {
        mReceiver.register(context.getApplicationContext(), true);
        getBluetoothAdapter().getProfileProxy(context, mListener, BluetoothProfile.A2DP);
    }

    public synchronized ArrayList<BluetoothDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public synchronized BluetoothDevice getFocusDevice() {
        if (connectedDevices.isEmpty()) {
            return null;
        }
        return connectedDevices.get(0);
    }

    private synchronized void addDevice(BluetoothDevice device) {
        if (device.getName() != null && device.getName().contains(DEVICE_NAME)) {
            connectedDevices.add(device);
            LogUtils_goertek.i(TAG, "[" + device.getName() + "] 已连接 " + device.getAddress());
            LocalBroadcastUtils.post(new Event<>(Const.EventCode.BluetoothConnectedCode, device));
        }
    }

    private synchronized void removeDevice(BluetoothDevice device) {
        if (connectedDevices.contains(device)) {
            connectedDevices.remove(device);
            LogUtils_goertek.i(TAG, "[" + device.getName() + "] 已断开 " + device.getAddress());
            LocalBroadcastUtils.post(new Event<>(Const.EventCode.BluetoothDisconnectedCode, device));
        }
    }

    private synchronized void clearDevice() {
        while (connectedDevices.size() > 0) {
            BluetoothDevice device = connectedDevices.remove(0);
            LogUtils_goertek.i(TAG, "[" + device.getName() + "] 已断开 " + device.getAddress());
            LocalBroadcastUtils.post(new Event<>(Const.EventCode.BluetoothDisconnectedCode, device));
        }
    }

    /***********************************************************************************************
     * A2DP已连接设备获取
     **********************************************************************************************/
    private BluetoothAdapter mBtAdapter;
    private BluetoothA2dp mBluetoothA2dp;
    private BluetoothAdapter getBluetoothAdapter() {
        if (mBtAdapter == null) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBtAdapter;
    }

    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                mBluetoothA2dp = null;
            }
        }
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if(profile == BluetoothProfile.A2DP){
                mBluetoothA2dp = (BluetoothA2dp) proxy;
                List<BluetoothDevice> devices = mBluetoothA2dp.getConnectedDevices();
                for (BluetoothDevice device : devices) {
                    addDevice(device);
                }
            }
        }
    };

    /***********************************************************************************************
     * A2DP广播监听
     **********************************************************************************************/
    private static Receiver mReceiver = new Receiver();
    private static class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        public void register(Context context, boolean register) {
            if (mRegistered == register) {
                return;
            }
            if (register) {
                final IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
                context.getApplicationContext().registerReceiver(this, filter);
            } else {
                context.getApplicationContext().unregisterReceiver(this);
            }
            mRegistered = register;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (null == action) {
                return;
            }
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    BluetoothListener.getInstance().handleBluetoothIntent(intent);
                    break;
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    BluetoothListener.getInstance().handleA2DPIntent(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleBluetoothIntent(Intent intent) {
        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
        switch (blueState) {
            case BluetoothAdapter.STATE_TURNING_ON:
                LogUtils_goertek.i(TAG, "[蓝牙状态]: 开启中");
                break;
            case BluetoothAdapter.STATE_ON:
                LogUtils_goertek.i(TAG, "[蓝牙状态]: 已打开");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                LogUtils_goertek.i(TAG, "[蓝牙状态]: 关闭中");
                break;
            case BluetoothAdapter.STATE_OFF:
                LogUtils_goertek.i(TAG, "[蓝牙状态]: 已关闭");
                clearDevice();
                break;
            default:
                break;
        }
    }

    private void handleA2DPIntent(Intent intent) {
        int status = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (status) {
            case BluetoothProfile.STATE_DISCONNECTED:
                removeDevice(device);
                break;
            case BluetoothProfile.STATE_CONNECTED:
                addDevice(device);
                break;
            default:
                break;
        }
    }

    /***********************************************************************************************
     * 单例化
     **********************************************************************************************/
    private BluetoothListener() {
        // 私有化构造函数
    }

    public static BluetoothListener getInstance() {
        return Singleton.SINGLETON.getSingleTon();
    }

    public enum Singleton {
        /** 枚举本身序列化之后返回的实例 */
        SINGLETON;
        private BluetoothListener singleton;

        /** JVM保证只实例一次 */
        Singleton() {
            singleton = new BluetoothListener();
        }

        /** 公布对外方法 */
        public BluetoothListener getSingleTon() {
            return singleton;
        }
    }
}
