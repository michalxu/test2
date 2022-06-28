package com.goertek.bluetooth.protocol.function;

import com.goertek.bluetooth.protocol.model.ParseResultEvent;

/**
 * 文件名：NotifyListener
 * 描述：设备主动上报
 * 创建时间：2019/4/10.
 * @author jochen.zhang
 */
public interface NotifyListener {
    /**
     * 接收主动上报数据
     *
     * @param result ParseResultEvent
     */
    void onNotify(ParseResultEvent result);
}
