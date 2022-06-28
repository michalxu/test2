package com.goertek.rox2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.StrictMode;

import androidx.core.app.ActivityCompat;

import com.goertek.bluetooth.link.device.SPPDevice;
import com.goertek.bluetooth.listener.BluetoothListener;
import com.goertek.common.utils.CrashHandler;
import com.goertek.common.utils.SharedPreferenceUtils;
import com.goertek.common.utils.ToastUtils;
import com.goertek.common.utils.Utils;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.service.BluetoothService;
import com.goertek.rox2.ui.main.LogUtils;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

public class MyApplication extends LitePalApplication {

    public static final  int[] STANDAR_EQ = {40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40,40};

    @Override
    public void onCreate() {
        super.onCreate();
        // 全局获取Context
        Utils.init(this);
        // 全局使用的BaseDevice
        Utils.setDevice(SPPDevice.getDefault());
        LogUtils.d("My application");
        CrashHandler.getInstance().init(this, true, false);

        // 初始化SP
        SharedPreferenceUtils.useDefault();
        // 初始化Toast
        ToastUtils.init(true);
        // 启动系统蓝牙监听
        BluetoothListener.getInstance().init(this);
        // 蓝牙服务
        BluetoothService.startService(this);


    }
}
