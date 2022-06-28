package com.goertek.bluetooth.protocol.function;

/**
 * 文件名：IResultListener
 * 描述：
 * 创建时间：2019/4/11.
 * @author jochen.zhang
 */
public interface IResultListener {
    /**
     * 处理成功
     *
     * @param payload 返回应用层数据
     */
    void onSuccess(byte[] payload);

    /**
     * 处理失败
     *
     * @param errorCode 错误码
     */
    void onFailed(int errorCode);
}
