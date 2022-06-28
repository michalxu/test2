package com.goertek.bluetooth.protocol.tws.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goertek.rox2.common.Const;

import java.util.Arrays;

/**
 * 文件名：TWSEQParams
 * 描述：蓝牙传输中使用的EQParams
 * 创建时间：2020/9/7
 * @author jochen.zhang
 */
public class TWSEQParams extends TWSBaseEntity {
    private static final String TAG = "TWSEQParams";
    /** 参数 */
    int[] params = new int[Const.MAX_FREQUENCY * 2];

    public TWSEQParams(int[] leftEq, int[] rightEq, boolean isDefault) {
        if (isDefault) {
            // 默认直接使用
            System.arraycopy(leftEq, 0, params, 0, Const.MAX_FREQUENCY);
            System.arraycopy(rightEq, 0, params, Const.MAX_FREQUENCY, Const.MAX_FREQUENCY);
        } else {
            // 非默认按定义换算
            for (int i = 0; i < Const.MAX_FREQUENCY; i++) {
                params[i] = Const.LIST_LEVEL[i][leftEq[i]];
                params[i + Const.MAX_FREQUENCY] = Const.LIST_LEVEL[i][rightEq[i]];
            }
        }
    }

    public TWSEQParams() {
    }

    public boolean equals(@Nullable TWSEQParams obj) {
        if (obj == null) {
            return false;
        }
        return toString().equals(obj.toString());
    }

    public int[] getParams() {
        return params;
    }

    public void setParams(int[] params) {
        this.params = params;
    }

    public void setParam(int index, int param) {
        if (index >= 0 && index < params.length) {
            params[index] = param;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return Arrays.toString(params);
    }
}
