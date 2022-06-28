package com.goertek.bluetooth.link.function;

/**
 * 文件名：IReceiveDataCallback
 * 描述：
 * 创建人：jochen.zhang
 * 创建时间：2019/4/10.
 */
public interface IReceiveDataCallback {
    /**
     * 接收数据
     *
     * @param data byte[]
     */
    void onReceive(byte[] data);
}
