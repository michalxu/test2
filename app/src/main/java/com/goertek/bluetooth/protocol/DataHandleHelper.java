package com.goertek.bluetooth.protocol;

import com.goertek.bluetooth.link.function.IChannel;
import com.goertek.bluetooth.link.function.IReceiveDataCallback;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.tws.TWSCommand;
import com.goertek.bluetooth.protocol.tws.TWSProtocol;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.common.utils.Utils;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 文件 DataHandleHelper.java
 * 描述 数据拆包、拼包处理
 * 发送时 根据MTU大小进行数据拆包发送
 * 接收时 将设备发送来的原始数据处理为协议数据
 * 时间 2018/6/5 11:33
 * @author jochen.zhang
 */
public class DataHandleHelper {
    private static final String TAG = "DataHandleHelper";

    interface OnProtocolListener {
        /**
         * 满足协议格式的数据通过此接口回调至API层
         *
         * @param data 完整一包协议数据
         */
        void onData(byte[] data);
    }
    /** 最大传输单元 */
    private int mtu = 200;//Integer.MAX_VALUE
    /** 数据通道 */
    private IChannel mChannel;
    /** 从设备接收原始数据 */
    private IReceiveDataCallback mReceiveDataCallback = new IReceiveDataCallback() {
        @Override
        public void onReceive(byte[] data) {
            // 处理原始数据
            dealWithData(data);
        }
    };
    /** 发送单线程池 */
    private ExecutorService mSendThreadExecutor;
    /** 接收单线程池 */
    private ExecutorService mReceiveThreadExecutor;
    /** 协议回调 -> API 层 */
    private OnProtocolListener mOnProtocolListener;


    DataHandleHelper(OnProtocolListener onProtocolListener) {
        this.mOnProtocolListener = onProtocolListener;
    }

    /**
     * 注册信道
     *
     * @param channel 数据通道
     */
    public synchronized void registerChannel(IChannel channel) {
        if (channel == mChannel) {
            return;
        }
        // 清空原有信道
        releaseChannel();
        // 设备读写Channel(SPP/BLE)
        mChannel = channel;
        // 注册接收数据回调
        mChannel.registerReceiveDataCallback(mReceiveDataCallback);
        // 发送线程池
        mSendThreadExecutor = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        // 接收线程池
        mReceiveThreadExecutor = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 注销信道
     */
    public synchronized void releaseChannel() {
        // 收发线程池关闭
        if (mSendThreadExecutor != null && !mSendThreadExecutor.isShutdown()) {
            mSendThreadExecutor.shutdown();
            mSendThreadExecutor = null;
        }
        if (mReceiveThreadExecutor != null && !mReceiveThreadExecutor.isShutdown()) {
            mReceiveThreadExecutor.shutdown();
            mReceiveThreadExecutor = null;
        }
        // channel release
        if (mChannel != null) {
            mChannel.unregisterReceiveDataCallback(mReceiveDataCallback);
            mChannel = null;
        }
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    /***********************************************************************************************
     * 发送数据相关
     **********************************************************************************************/

    public synchronized boolean writeData(final List<byte[]> datas) {
        if (null == mChannel) {
            LogUtils_goertek.e(TAG, "writeData null == mChannel");
            return false;
        }

        if (null == datas || datas.isEmpty()) {
            LogUtils_goertek.e(TAG, "writeData null == datas or datas.size == 0");
            return false;
        }
        if (mChannel.isOpen()) {
            for (int i = 0; i < datas.size(); i++) {
                writeData(datas.get(i));
                // TODO: 2018/5/18 是否需要睡眠
            }
            return true;
        } else {
            LogUtils_goertek.e("writeData error! No device connected!");
            return false;
        }
    }

    public synchronized boolean writeData(final byte[] data) {
        if (null == mChannel || null == mSendThreadExecutor || mSendThreadExecutor.isShutdown()) {
            LogUtils_goertek.e(TAG, "writeData null == mChannel");
            return false;
        }

        if (null == data) {
            LogUtils_goertek.e(TAG, "writeData null == data");
            return false;
        }
        if (mChannel.isOpen()) {
            mSendThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mtu >= data.length) {
                        // 发送数据小于MTU，直接发送
                        LogUtils.d("data.length=="+data.length);
                        mChannel.write(data);
                    } else {
                        // 发送数据大于MTU，需要分包发送
                        int count = data.length;
                        int currentCount = 0;
                        while (count - currentCount > mtu) {
                            try {
                                Thread.sleep(10);
                                byte[] subData = ProtocolUtils.subByte(data, currentCount, mtu);
                                LogUtils.d("subData.length = "+subData.length);
                                mChannel.write(subData);
                                currentCount += mtu;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        byte[] subData = ProtocolUtils.subByte(data, currentCount, count - currentCount);
                        mChannel.write(subData);
                        LogUtils.d("subData.length = "+subData.length);
                    }
                }
            });
            return true;
        } else {
            LogUtils_goertek.e("writeData error! No device connected!");
            return false;
        }
    }

    /***********************************************************************************************
     * 接收数据相关
     **********************************************************************************************/
    public synchronized void dealWithData(final byte[] data) {
        if (null == mChannel || null == mReceiveThreadExecutor || mReceiveThreadExecutor.isShutdown()) {
            LogUtils_goertek.e(TAG, "dealWithData mChannel == " + mChannel + " mReceiveThreadExecutor == " + mReceiveThreadExecutor);
            return;
        }
        mReceiveThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                deal(data, true);
            }
        });
    }

    /**
     * 处理数据
     */
    private synchronized void deal(byte[] data, boolean fromDevice) {
        LogUtils_goertek.i(TAG, "fromDevice:" + fromDevice + " 处理数据:" + ProtocolUtils.bytesToHexStr(data));
        if (data == null || data.length == 0) {
            return;
        }
        String dataStr = ProtocolUtils.bytesToHexStr(data, true);
        String[] splitArr = dataStr.split(TWSCommand.SOF);
        if (splitArr.length < 2) {
            LogUtils_goertek.e(TAG, "未检测到TWSCommand.SOF");
            return;
        }
        for (int i = 0; i < splitArr.length; i++) {
            String other = splitArr[i];
            if (other.endsWith(TWSCommand.END)) {
                // 起始为TWSCommand.SOF 结尾为TWSCommand.END
                byte[] singleData = ProtocolUtils.hexStrToBytes(TWSCommand.SOF + other);
                if (TWSProtocol.checkPayload(singleData)) {
                    // 是协议数据(Payload校验通过)
                    LogUtils.d("Payload校验通过");
                    dataCallback(singleData);
                }
            }
        }
    }

    /**
     * 将协议数据回调至API层
     *
     * @param data 一条协议数据
     */
    private void dataCallback(byte[] data) {
        if (mOnProtocolListener != null) {
            mOnProtocolListener.onData(data);
        }
    }
}
