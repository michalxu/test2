package com.goertek.bluetooth.protocol.model;

import com.goertek.bluetooth.protocol.tws.TWSCommand;
import com.goertek.common.utils.ProtocolUtils;

/**
 * 文件名：ParseResultEvent
 * 描述：
 * 创建时间：2018/5/17.
 * @author jochen.zhang
 */
public class ParseResultEvent {
    private boolean isSuccess;
    private String SOF;
    private String tag;
    private byte[] payload;

    public ParseResultEvent(String SOF, byte[] payload) {
        this.SOF = SOF;
        this.payload = payload;
        switch (SOF) {
            case TWSCommand.SOF:
                isSuccess = payload.length >= 5;
                if (isSuccess) {
                    // OpCode
                    tag = ProtocolUtils.byteToHexStr(payload[1]);
                }
                break;
            default:
                isSuccess = false;
                break;
        }
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSOF(String SOF) {
        this.SOF = SOF;
    }

    public String getSOF() {
        return SOF;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }
}
