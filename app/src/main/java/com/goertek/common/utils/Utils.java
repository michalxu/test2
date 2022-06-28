package com.goertek.common.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.goertek.bluetooth.link.device.BaseDevice;
import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.protocol.ProtocolAPI;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.tws.entity.TWSBattery;
import com.goertek.bluetooth.protocol.tws.entity.TWSEQParams;
import com.goertek.bluetooth.protocol.tws.entity.TWSResult;
import com.goertek.common.event.Event;
import com.goertek.rox2.common.Const;

public class Utils {
    private static final String TAG = "Utils";
    /** 全局获取上下文 */
    private static Context sContext;
    /** 应用使用的蓝牙连接控制器 */
    private static BaseDevice sDevice;
    /** 全局主线程Handler */
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }

    public static int byteToInt(byte data){
        String mData = String.valueOf(data);
        return Integer.parseInt(mData);
    }
    public static byte[] intToBytes(int n){
        String s = String.valueOf(n);

        return s.getBytes();

    }
    public static void setDevice(BaseDevice device) {
        sDevice = device;
    }

    public static BaseDevice getDevice() {
        return sDevice;
    }

    public static Handler getHandler() {
        return sHandler;
    }

    /** 刷新电量 */
    public static void refreshBattery() {
        if (sDevice.connectionState != ConnectState.STATE_DATA_READY) {
            // 若未连接，直接退出
            return;
        }
        ProtocolAPI.getDefault().getBattery(new IRspListener<TWSBattery>() {
            @Override
            public void onSuccess(TWSBattery object) {
                LocalBroadcastUtils.post(new Event<>(Const.EventCode.BatteryStateCode, object));
            }

            @Override
            public void onFailed(int errorCode) {
                LogUtils_goertek.i(TAG, "电量获取失败!");
            }
        });
    }

    public static void getEq(IRspListener<TWSEQParams> listener) {
        ProtocolAPI.getDefault().getEq(new IRspListener<TWSEQParams>() {
            @Override
            public void onSuccess(TWSEQParams object) {
                // 耳机当前参数变更
                EQUtils.getInstance().setParams(object);
                if (listener != null) {
                    listener.onSuccess(object);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener != null) {
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void getNoiceControlStatus(IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().getNoiceControlStatus(new IRspListener<byte[]>() {

            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void setNoiceControl(byte[] data,IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().setNoiceControl(data, new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void setHeartRateOnMeasure(IRspListener<byte[] >listener){
        ProtocolAPI.getDefault().setHeartRateOnMeasure( new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void setSettings(byte[] data, IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().setSettings(data, new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void getSettings(byte data,IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().getSettings(data, new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void setHeartRateDetectAuto(boolean isOn,IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().setHeartRateDetectAuto(isOn, new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void setHeartRateDetectAccurately(boolean isOn,IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().setHeartRateDetectAccurately(isOn, new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void getHeartRateDetectAutoStatus(IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().getHeartRateDetectAutoStatus( new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void getHeartRateDetectAccurately(IRspListener<byte[]> listener){
        ProtocolAPI.getDefault().getHeartRateDetectAccurately( new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener!=null){
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener!=null){
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void getWearState(IRspListener<byte[]> listener) {
        ProtocolAPI.getDefault().getWearState(new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] payload) {
                if (listener != null) {
                    listener.onSuccess(payload);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener != null) {
                    listener.onFailed(errorCode);
                }
            }
        });
    }
    public static void setEq(TWSEQParams params, IRspListener<TWSResult> listener) {
        ProtocolAPI.getDefault().setEq(params, new IRspListener<TWSResult>() {
            @Override
            public void onSuccess(TWSResult object) {
                // 耳机当前参数变更
                EQUtils.getInstance().setParams(params);
                // 回调外界
                if (listener != null) {
                    listener.onSuccess(object);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener != null) {
                    listener.onFailed(errorCode);
                }
            }
        });
    }
    public static void setEq(int[] params, IRspListener<byte[]> listener) {
        ProtocolAPI.getDefault().setEq(params, new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] object) {
                // 回调外界
                if (listener != null) {
                    listener.onSuccess(object);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener != null) {
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void resetEq(IRspListener<TWSResult> listener) {
        ProtocolAPI.getDefault().resetEq(new IRspListener<TWSResult>() {
            @Override
            public void onSuccess(TWSResult object) {
                // 耳机当前参数变更
                EQUtils.getInstance().removeParams();
                // 回调外界
                if (listener != null) {
                    listener.onSuccess(object);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener != null) {
                    listener.onFailed(errorCode);
                }
            }
        });
    }
//    public static void setWdrcDta(byte[] data,IRspListener<byte[]> listener){
//        ProtocolAPI.getDefault().setWdrcData(data, new IRspListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] result) {
//                // 回调外界
//                if (listener != null) {
//                    listener.onSuccess(result);
//                }
//            }
//
//            @Override
//            public void onFailed(int errorCode) {
//                if (listener != null) {
//                    listener.onFailed(errorCode);
//                }
//            }
//        });
//    }

    public static void setAudition(byte[] params, IRspListener<byte[]> listener) {
        ProtocolAPI.getDefault().setAudition(params, new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                // 回调外界
                if (listener != null) {
                    listener.onSuccess(result);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener != null) {
                    listener.onFailed(errorCode);
                }
            }
        });
    }

    public static void updateFirmWare( IRspListener<byte[]> listener) {
        ProtocolAPI.getDefault().updateFirmWare(new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                // 回调外界
                if (listener != null) {
                    listener.onSuccess(result);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                if (listener != null) {
                    listener.onFailed(errorCode);
                }
            }
        });
    }
}
