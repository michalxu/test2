package com.goertek.bluetooth.protocol.function;
/**
 * 文件名：IRspListener
 * 描述：
 * 创建时间：2019/9/9
 * @author jochen.zhang
 */
public interface IRspListener<T> {
    /**
     * 处理成功
     *
     * @param object 返回对象
     */
    void onSuccess(T object);

    /**
     * 处理失败
     *
     * @param errorCode 错误码
     */
    void onFailed(int errorCode);
}
