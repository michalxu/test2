package com.goertek.common.base;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.goertek.common.event.Event;
import com.goertek.common.utils.LanguageUtils;
import com.goertek.common.utils.LocalBroadcastUtils;
import com.goertek.common.utils.LogUtils_goertek;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * 文件名：BaseActivity
 * 描述：Activity基类
 * 创建时间：2020/9/2
 * @author jochen.zhang
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static String TAG = BaseActivity.class.getSimpleName();
    private boolean isActive = true;
    private boolean isDestroy = false;
    private MyHandler mBaseHandler = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDestroy()) {
                return;
            }
            if (intent != null) {
                Event event = (Event) intent.getSerializableExtra(LocalBroadcastUtils.KEY_EVENT);
                if (event != null) {
                    onEvent(event);
                }
            }
        }
    };

    /*********************************************************************************************************************************
     * AppCompatActivity 重写函数
     ********************************************************************************************************************************/

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtils.attachBaseContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBaseHandler = new MyHandler(this);
        if (isRegisterLocalBroadcast()) {
            LocalBroadcastUtils.registerLocalBroadCast(mReceiver);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isDestroy = false;
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    protected void onDestroy() {
        if (isRegisterLocalBroadcast()) {
            LocalBroadcastUtils.unregisterLocalBroadCast(mReceiver);
        }
        super.onDestroy();
        //销毁handlers
        try {
            mBaseHandler.removeCallbacksAndMessages(null);
            mBaseHandler = null;
        } catch (Exception ignored) {
        }
        isDestroy = true;
        isActive = false;
    }

    /*********************************************************************************************************************************
     * 权限请求 相关函数
     * 权限组列表：
     * Android6.0只用申请权限组中一个权限及获得全部权限
     * Android8.0需要全部申请权限组权限，但是只会申请第一个权限时提示，后面不会提示
     *
     * // 读写日历
     * Manifest.permission.READ_CALENDAR,
     * Manifest.permission.WRITE_CALENDAR
     * // 相机
     * Manifest.permission.CAMERA
     * // 读写联系人
     * Manifest.permission.READ_CONTACTS,
     * Manifest.permission.WRITE_CONTACTS,
     * Manifest.permission.GET_ACCOUNTS
     * // 读位置信息
     * Manifest.permission.ACCESS_FINE_LOCATION,
     * Manifest.permission.ACCESS_COARSE_LOCATION
     * // 使用麦克风
     * Manifest.permission.RECORD_AUDIO
     * // 读电话状态、打电话、读写电话记录
     * Manifest.permission.READ_PHONE_STATE,
     * Manifest.permission.CALL_PHONE,
     * Manifest.permission.READ_CALL_LOG,
     * Manifest.permission.WRITE_CALL_LOG,
     * Manifest.permission.ADD_VOICEMAIL,
     * Manifest.permission.USE_SIP,
     * Manifest.permission.PROCESS_OUTGOING_CALLS
     * // 传感器
     * Manifest.permission.BODY_SENSORS
     * // 读写短信、收发短信
     * Manifest.permission.SEND_SMS,
     * Manifest.permission.RECEIVE_SMS,
     * Manifest.permission.READ_SMS,
     * Manifest.permission.RECEIVE_WAP_PUSH,
     * Manifest.permission.RECEIVE_MMS,
     * Manifest.permission.READ_CELL_BROADCASTS
     * // 读写存储卡
     * Manifest.permission.READ_EXTERNAL_STORAGE,
     * Manifest.permission.WRITE_EXTERNAL_STORAGE
     ********************************************************************************************************************************/
    private static final int REQUEST_PERMISSIONS_CODE = 0x1000;

    /**
     * 从Manifest中获取Permission数组
     *
     * @return Permission数组
     */
    protected String[] getManifestPermissions() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            return packageInfo.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 权限请求相关函数
     *
     * @param permissions String[] 所有请求
     */
    protected void checkPermissions(String[] permissions) {
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        } else {
                            onPermissionFailed(permissions[i]);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 权限允许
     *
     * @param permission permission String
     */
    protected void onPermissionGranted(String permission) {
        if (isActive) {
            LogUtils_goertek.d(TAG, String.format(Locale.getDefault(), "%s %s", permission, "Granted"));
        }
    }

    /**
     * 权限拒绝
     *
     * @param permission permission String
     */
    protected void onPermissionFailed(String permission) {
        if (isActive) {
            LogUtils_goertek.d(TAG, String.format(Locale.getDefault(), "%s %s", permission, "Failed"));
        }
    }

    protected static final int REQUEST_OPEN_BT_CODE = 0x1001;
    protected static final int REQUEST_OPEN_GPS_CODE = 0x1002;


    /**
     * 打开蓝牙
     */
    public void openBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_OPEN_BT_CODE);

    }


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    public boolean isGPSOpen() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    /**
     * 强制帮用户打开GPS
     */
    public void openGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, REQUEST_OPEN_GPS_CODE);
    }

    /*********************************************************************************************************************************
     * Handler 相关函数
     ********************************************************************************************************************************/
    /** 获取Handler */
    protected MyHandler getHandler() {
        return mBaseHandler;
    }

    /** 子类重写处理Handler方法 */
    protected void onMessage(Message msg) {}

    /** 自定义Handler */
    public static class MyHandler extends Handler {
        private final WeakReference<BaseActivity> mWeakReference;

        public MyHandler(BaseActivity reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity context = mWeakReference.get();
            if (context != null) {
                if (context.isDestroy()) {
                    return;
                }
                context.onMessage(msg);
            }
        }
    }

    /*********************************************************************************************************************************
     * 其他函数
     ********************************************************************************************************************************/

    /**
     * 子类重写该方法修改初始化时StatusBar的颜色
     */
    protected @ColorInt int initialStatusBarColor() {
        return Color.TRANSPARENT;
    }

    /**
     * 设置状态栏颜色
     *
     * @param color Res
     */
    protected void setStatusBarColor(@ColorInt int color, boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(color);
            View decor = window.getDecorView();
            if (isDark) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }

    protected void setNavigationColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(color);
        }
    }

    /**
     * 获取状态
     *
     * @return
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 获取是否销毁的状态
     *
     * @return
     */
    public boolean isDestroy() {
        return isDestroy;
    }

    /**
     * 是否注册事件分发
     *
     * @return true绑定LocalBroadcast事件分发，默认不绑定，子类需要绑定的话复写此方法返回true.
     */
    protected boolean isRegisterLocalBroadcast() {
        return false;
    }

    /**
     * 监听LocalBroadcast消息
     *
     * @param event 接收到的event
     */
    public void onEvent(Event event) {}
}
