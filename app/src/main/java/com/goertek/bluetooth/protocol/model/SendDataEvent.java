package com.goertek.bluetooth.protocol.model;

import com.goertek.bluetooth.protocol.function.IResultListener;
import com.goertek.common.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件名：SendDataEvent
 * 描述：
 * 创建时间：2018/5/16.
 * @author jochen.zhang
 */
public class SendDataEvent {
    /** Link Layer Data */
    private List<byte[]> datas = new ArrayList<>();
    private String SOF;
    private String tag;
    private IResultListener listener;
    private int timeOut;
    private int index = -1;

    public void addData(byte[] data) {
        this.datas.add(data);
    }

    public void setSOF(String SOF) {
        this.SOF = SOF;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setListener(IResultListener listener) {
        this.listener = listener;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<byte[]> getDatas() {
        return datas;
    }

    public String getSOF() {
        return SOF;
    }

    public String getTag() {
        return tag;
    }

    public IResultListener getListener() {
        return listener;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public int getIndex() {
        return index;
    }

    public boolean isResult(ParseResultEvent result) {
        return (SOF.equals(result.getSOF()) && tag.equals(result.getTag()));
    }

    @Override
    public String toString() {
        StringBuilder resultBuilder = new StringBuilder("SOF: " + SOF + " Tag: " + tag + " TimeOut: " + timeOut + "\n");
        for (int i = 0; i < datas.size(); i++) {
            resultBuilder.append(Arrays.toString(datas.get(i)));
            resultBuilder.append("\n");
        }
        return resultBuilder.toString();
    }
}
