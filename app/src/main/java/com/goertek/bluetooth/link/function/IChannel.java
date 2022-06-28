package com.goertek.bluetooth.link.function;

public interface IChannel {
    /**
     * 当前通道已打开
     *
     * @return true 连接就绪
     */
    boolean isOpen();

    /**
     * 写入数据
     *
     * @param data 代写入数据
     */
    boolean write(byte[] data);

    /**
     * 注册设备响应数据的监听者
     * @param callback 设备数据回调
     */
    void registerReceiveDataCallback(IReceiveDataCallback callback);

    /**
     * 解绑设备响应数据的监听者
     * @param callback 设备数据回调
     */
    boolean unregisterReceiveDataCallback(IReceiveDataCallback callback);
}
