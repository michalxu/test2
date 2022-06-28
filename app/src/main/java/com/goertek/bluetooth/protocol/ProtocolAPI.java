package com.goertek.bluetooth.protocol;

import android.os.Handler;
import android.os.Looper;

import com.goertek.bluetooth.link.function.IChannel;
import com.goertek.bluetooth.protocol.function.IResultListener;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.function.NotifyListener;
import com.goertek.bluetooth.protocol.model.ParseResultEvent;
import com.goertek.bluetooth.protocol.model.SendDataEvent;
import com.goertek.bluetooth.protocol.tws.TWSCommand;
import com.goertek.bluetooth.protocol.tws.TWSProtocol;
import com.goertek.bluetooth.protocol.tws.entity.TWSBattery;
import com.goertek.bluetooth.protocol.tws.entity.TWSEQParams;
import com.goertek.bluetooth.protocol.tws.entity.TWSResult;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.goertek.bluetooth.protocol.tws.TWSProtocol.getLength;

public class ProtocolAPI {
    private static final String TAG = "ProtocolAPI";

    private DataHandleHelper mDataHandleHelper;
    private NotifyListener mNotifyListener;
    private List<NotifyListener> notifyListenerList = new ArrayList<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private DataHandleHelper.OnProtocolListener mOnProtocolListener = new DataHandleHelper.OnProtocolListener() {
        @Override
        public void onData(byte[] data) {
            // 设备上报数据
            LogUtils.d("设备上报数据");
            receiveResponse(new ParseResultEvent(TWSCommand.SOF, TWSProtocol.getPayload(data)));
        }
    };

    public synchronized void registerChannel(IChannel channel, int mtu) {
        mDataHandleHelper.registerChannel(channel);
        mDataHandleHelper.setMtu(mtu);
    }

    public synchronized void releaseChannel() {
        mDataHandleHelper.releaseChannel();
    }

    /**
     * 注册设备主动上报数据监听者
     * @param listener 设备主动上报数据监听者
     */
    public void registerNotifyListener(NotifyListener listener) {
        notifyListenerList.add(listener);
//        mNotifyListener = listener;
    }

    /**
     * 解绑设备主动上报数据监听者
     */
    public void unregisterNotifyListener(NotifyListener listener) {
        if (notifyListenerList.contains(listener)){
            notifyListenerList.remove(listener);
        }
//        mNotifyListener = null;
    }

    /**
     * 重置EQ
     *
     * @param listener IRspListener<TWSResult>
     */
    public void resetEq(IRspListener<TWSResult> listener) {
        LogUtils.d( "resetEq");
        sendData(TWSProtocol.resetEq(), NORMAL_TIME_OUT, new IResultListener() {
            @Override
            public void onSuccess(byte[] payload) {
                TWSResult result = TWSProtocol.parseResetEq(payload);
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

    /**
     * 设置EQ
     *
     * @param params EQ参数
     * @param listener IRspListener<TWSResult>
     */
    public void setEq(TWSEQParams params, IRspListener<TWSResult> listener) {
        LogUtils.d( "setEq " + params);
        sendData(TWSProtocol.setEq(params), NORMAL_TIME_OUT, new IResultListener() {
            @Override
            public void onSuccess(byte[] payload) {
                TWSResult result = TWSProtocol.parseSetEq(payload);
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

    /**
     * 设置EQ
     *
     * @param params EQ参数
     * @param listener IRspListener<TWSResult>
     */
    public void setEq(int[] params, IRspListener<byte[]> listener) {
        sendData(TWSProtocol.setEq(params), NORMAL_TIME_OUT, new IResultListener() {
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

    /**
     * 获取EQ
     *
     * @param listener IRspListener<TWSEQParams>
     */
    public void getEq(IRspListener<TWSEQParams> listener) {
        LogUtils.d("getEq");
        sendData(TWSProtocol.getEq(), NORMAL_TIME_OUT, new IResultListener() {
            @Override
            public void onSuccess(byte[] payload) {
                TWSEQParams params = TWSProtocol.parseGetEq(payload);
                if (listener != null) {
                    listener.onSuccess(params);
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

    /**
     * 设置ANC
     * 0x00：正常模式 0x01：降噪模式 0x02：通透模式
     *
     * @param isOn true ANC ON
     * @param listener IRspListener<TWSResult>
     */
    public void setAnc(boolean isOn, IRspListener<TWSResult> listener) {
        LogUtils.d("setAnc " + isOn);
        sendData(TWSProtocol.setAnc(isOn ? 0x01 : 0x00), NORMAL_TIME_OUT, new IResultListener() {
            @Override
            public void onSuccess(byte[] payload) {
                TWSResult result = TWSProtocol.parseSetAnc(payload);
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

//    public void setNoiceControl(int mode,IRspListener<TWSResult> listener){
//        sendData(TWSProtocol.setNoiceControl(mode), NORMAL_TIME_OUT, new IResultListener() {
//            @Override
//            public void onSuccess(byte[] payload) {
//                TWSResult result = TWSProtocol.parseSetAnc(payload);
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

//    public void setWdrcData(byte[] wdrcDta,IRspListener<byte[]> listener){
//        sendData(TWSProtocol.setWdrcData(wdrcDta), NORMAL_TIME_OUT, new IResultListener() {
//            @Override
//            public void onSuccess(byte[] result) {
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
    /**
     * 获取电量
     *
     * @param listener IRspListener<TWSBattery>
     */
    public void getBattery(IRspListener<TWSBattery> listener) {
        LogUtils.d("getBattery");
        sendData(TWSProtocol.getBattery(), NORMAL_TIME_OUT, new IResultListener() {
            @Override
            public void onSuccess(byte[] payload) {
                TWSBattery battery = TWSProtocol.parseGetBattery(payload);
                if (listener != null) {
                    listener.onSuccess(battery);
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


    public void getNoiceControlStatus(IRspListener<byte[]> listener){
        sendData(TWSProtocol.getNoiceControl(), NORMAL_TIME_OUT, new IResultListener() {
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

    public void setNoiceControl(byte[] data,IRspListener<byte[]> listener){
        sendData(TWSProtocol.setNoiceControl(data), NORMAL_TIME_OUT, new IResultListener() {
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

    public void setHeartRateOnMeasure(IRspListener<byte[]> listener){
        sendData(TWSProtocol.setHeartRateOnMeasure(), NORMAL_TIME_OUT, new IResultListener() {
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

    public void setSettings(byte[] data,IRspListener<byte[]> listener){
        sendData(TWSProtocol.setSettings(data), NORMAL_TIME_OUT, new IResultListener() {
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

    public void getSettings(byte data,IRspListener<byte[]> listener){
        sendData(TWSProtocol.getSettings(data), NORMAL_TIME_OUT, new IResultListener() {
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

    public void setHeartRateDetectAuto(boolean isOn,IRspListener<byte[] > listener){
        sendData(TWSProtocol.setHeartRateDetectAuto(isOn), NORMAL_TIME_OUT, new IResultListener() {
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

    public void setHeartRateDetectAccurately(boolean isOn,IRspListener<byte[] > listener){
        sendData(TWSProtocol.setHeartRateDetectAccurately(isOn), NORMAL_TIME_OUT, new IResultListener() {
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

    public void getHeartRateDetectAutoStatus(IRspListener<byte[] > listener) {
        sendData(TWSProtocol.getHeartRateDetectAutoStatus(), NORMAL_TIME_OUT, new IResultListener() {
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

    public void getHeartRateDetectAccurately(IRspListener<byte[] > listener) {
        sendData(TWSProtocol.getHeartRateDetectAccurately(), NORMAL_TIME_OUT, new IResultListener() {
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
    public void getWearState(IRspListener<byte[]> listener){
        sendData(TWSProtocol.getWearState(), NORMAL_TIME_OUT, new IResultListener() {
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

    public void setAudition(byte[] params, IRspListener<byte[]> listener) {
        LogUtils.d("set eq="+ProtocolUtils.bytesToHexStr(params));
        sendData(TWSProtocol.setAudition(params), NORMAL_TIME_OUT, new IResultListener() {
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

    public void updateFirmWare(IRspListener<byte[]> listener) {
        sendData(TWSProtocol.updateFirmWare(), NORMAL_TIME_OUT, new IResultListener() {
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
    /**
     * 发送数据（有响应、自定超时）
     */
    public void sendData(byte[] data, int timeOut, IResultListener listener) {
        LogUtils.d("sendData="+ProtocolUtils.bytesToHexStr(data));
        SendDataEvent sendDataEvent = new SendDataEvent();
        sendDataEvent.addData(data);
        sendDataEvent.setSOF(TWSCommand.SOF);
        LogUtils.d("ProtocolUtils.byteToHexStr(data[4])="+ProtocolUtils.byteToHexStr(data[4]));
        sendDataEvent.setTag(ProtocolUtils.byteToHexStr(data[4]));  //opCode
        sendDataEvent.setListener(listener);
        sendDataEvent.setTimeOut(timeOut);
        LogUtils.d(sendDataEvent.toString());
        addQueue(sendDataEvent);
    }

    /**
     * 发送数据
     */
    public void sendData(byte[] data) {
        mDataHandleHelper.writeData(data);
    }

    /***********************************************************************************************
     * Bluetooth 发送队列
     **********************************************************************************************/
    private static final int NORMAL_TIME_OUT = 2000;
    private static final int MAX_RESEND = 2;
    private List<SendDataEvent> mSendQueue = new ArrayList<>();
    private Timer mResendTimer = new Timer();
    private TimerTask mResendTimerTask;
    private boolean isSending = false;
    private int resendCount = 0;

    /**
     * 向队列中添加待发送命令
     * @param sendDataEvent 新增命令
     */
    private synchronized void addQueue(SendDataEvent sendDataEvent) {
        mSendQueue.add(sendDataEvent);
        LogUtils.d( "SendQueue add command current size:  " + mSendQueue.size());
        send();
    }

    /**
     * 发送队列中的指令
     */
    private void send() {
        if (!mSendQueue.isEmpty() && !isSending) {
            isSending = true;
            resendCount = -1;
            sendOnceCommand(mSendQueue.get(0));
        }
    }

    /**
     * 发送单条指令，
     * 若重发次数小于MAX_RESEND，则设置超时重发
     * 否则，清空队列
     *
     * @param sendDataEvent 本次发送数据
     */
    private void sendOnceCommand(final SendDataEvent sendDataEvent) {
        resendCount++;
        if (resendCount > MAX_RESEND) {
            //重发次数超限
            LogUtils.e( "Resend time over " + MAX_RESEND + " times, command fail.");
            SendDataEvent sendDataEvent1 = mSendQueue.remove(0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendDataEvent1.getListener().onFailed(ErrorCode.ERROR_REACH_MAX_RESEND);
                }
            });
            isSending = false;
            send();
            return;
        }
        if (mDataHandleHelper.writeData(sendDataEvent.getDatas())) {
            mResendTimerTask = new TimerTask() {
                @Override
                public void run() {
                    sendOnceCommand(sendDataEvent);
                }
            };
            mResendTimer.schedule(mResendTimerTask, sendDataEvent.getTimeOut());
        } else {
            sendOnceCommand(sendDataEvent);
        }
    }

    /**
     * 应用层解析完数据后调用此方法
     *
     * @param result 应用层返回的Response结果
     */
    public void receiveResponse(final ParseResultEvent result) {
        if (result.isSuccess() && !mSendQueue.isEmpty() && mSendQueue.get(0).isResult(result)) {
            com.goertek.rox2.ui.main.LogUtils.i("receiveResponse: " + result.isSuccess());
            //接收到返回数据
            //1、取消超时重发，将状态至为未发送
            mResendTimerTask.cancel();
            mResendTimerTask = null;
            isSending = false;
            //2、处理返回结果，调用Callback
            SendDataEvent sendDataEvent = mSendQueue.remove(0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d("michal,receiveResponse:"+ ProtocolUtils.bytesToHexStr(result.getPayload()));
                    sendDataEvent.getListener().onSuccess(result.getPayload());
                }
            });

            //若队列中有未发送数据，继续发送
            send();
        } else {
            //非返回数据
            if (notifyListenerList!=null&&notifyListenerList.size()>0){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i=0; i<notifyListenerList.size(); i++){
                            notifyListenerList.get(i).onNotify(result);
                        }
//                        mNotifyListener.onNotify(result);
                    }
                });
            }
            if (null != mNotifyListener) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mNotifyListener.onNotify(result);
                    }
                });
            }
        }
    }

    /**
     * 清空发送队列
     */
    public synchronized void clearSendQueue() {
        LogUtils.i("清空发送队列");
        //1、取消超时重发，将状态至为未发送
        if (null != mResendTimerTask) {
            mResendTimerTask.cancel();
            mResendTimerTask = null;
        }
        isSending = false;

        while (!mSendQueue.isEmpty()) {
            SendDataEvent sendDataEvent = mSendQueue.remove(0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendDataEvent.getListener().onFailed(ErrorCode.ERROR_CLEAR_SEND_QUEUE);
                }
            });
        }
    }

    /***********************************************************************************************
     * 单例化
     **********************************************************************************************/
    public ProtocolAPI() {
        mDataHandleHelper = new DataHandleHelper(mOnProtocolListener);
    }

    public static ProtocolAPI getDefault() {
        return Singleton.SINGLETON.getSingleTon();
    }

    public enum Singleton {
        /** 枚举本身序列化之后返回的实例 */
        SINGLETON;
        private ProtocolAPI singleton;

        /** JVM保证只实例一次 */
        Singleton() {
            singleton = new ProtocolAPI();
        }

        /** 公布对外方法 */
        public ProtocolAPI getSingleTon() {
            return singleton;
        }
    }
}
