package com.goertek.bluetooth.link.device;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.link.function.IChannel;
import com.goertek.bluetooth.link.function.IReceiveDataCallback;
import com.goertek.bluetooth.link.function.UUIDConfig;
import com.goertek.common.utils.LocalBroadcastUtils;
import com.goertek.common.event.Event;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.rox2.common.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：BaseDevice
 * 描述：设备Model基类
 * 创建人：jochen.zhang
 * 创建时间：2019/7/30
 */
public abstract class BaseDevice implements IChannel {
    /** 连接蓝牙设备地址 */
    protected String mac;
    /** 连接状态 */
    public int connectionState = ConnectState.STATE_DISCONNECTED;
    /** UUID配置 */
    protected UUIDConfig mUUIDConfig;
    /** 接收数据回调 */
    private List<IReceiveDataCallback> receiveDataCallbackList = new ArrayList<>();

    /**
     * 建立连接
     */
    public abstract boolean connect(String address, UUIDConfig uuidConfig);

    /**
     * 断开连接
     */
    public abstract void disconnect();


    /**
     * 设备发送的数据的数据
     * @param data 设备发送的原始数据
     */
    protected void receive(byte[] data) {
        for (IReceiveDataCallback receiveDataCallback: receiveDataCallbackList) {
            receiveDataCallback.onReceive(data);
        }
    }

    /**
     * 设置连接状态
     *
     * @param state ConnectState
     */
    protected synchronized void setConnectState(int state) {
        if (state != connectionState) {
            LogUtils_goertek.i("[" + mac + "] 连接状态 " + connectionState + " -> " + state);
            connectionState = state;
            LocalBroadcastUtils.post(new Event<>(Const.EventCode.ConnectStateCode, state));
        }
    }

    /**
     * 获取连接状态
     */
    public synchronized int getConnectionState() {
        return connectionState;
    }

    /***********************************************************************************************
     * IChannel相关方法
     **********************************************************************************************/
    /**
     * 当前通道已打开
     *
     * @return true 连接就绪
     */
    @Override
    public boolean isOpen() {
        return connectionState >= ConnectState.STATE_DATA_READY;
    }

    /**
     * 写入数据
     *
     * @param data 代写入数据
     */
    @Override
    public abstract boolean write(byte[] data);

    /**
     * 注册设备响应数据的监听者
     * @param callback 设备数据回调
     */
    @Override
    public void registerReceiveDataCallback(IReceiveDataCallback callback) {
        receiveDataCallbackList.add(callback);
    }

    /**
     * 解绑设备响应数据的监听者
     * @param callback 设备数据回调
     */
    @Override
    public boolean unregisterReceiveDataCallback(IReceiveDataCallback callback) {
        return receiveDataCallbackList.remove(callback);
    }

    public String getAddress(){
        return mac;
    }
}
