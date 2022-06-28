package com.goertek.rox2.common.entity;

import androidx.annotation.NonNull;

import com.goertek.bluetooth.protocol.tws.entity.TWSEQParams;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.LogUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 文件名：EQModel
 * 描述：单EQ配置类
 * mName     EQ名名称
 * leftEqs   左耳EQ   --   音频等阶
 * rightEqs  右耳EQ   --   音频等阶
 * meanEqs   平均EQ   --   左右均值向下取整
 * isDefault 是否是默认EQ
 * 创建时间：2020/9/7
 * @author jochen.zhang
 */
@XStreamAlias("EQ")
public class EQModel implements Serializable {
    private static final String TAG = "EQModel";
    @XStreamAlias("name")
    private String mName;
    @XStreamAlias("leftEqs")
    private int[] leftEqs = new int[Const.MAX_FREQUENCY];
    @XStreamAlias("rightEq")
    private int[] rightEqs = new int[Const.MAX_FREQUENCY];
    /** Eq 均值 */
    private int[] meanEqs;
    /** 是否是默认EQ */
    private boolean isDefault = false;

    private byte[] WDRC;

    public int[] getGains() {
        return gains;
    }

    public void setGains(int[] gains) {
        this.gains = gains;
    }

    private int[] gains = new int[]{10, 10, 10, 10, 10, 10, 10, 10};
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int[] getMeanEqs() {
        if (meanEqs == null) {
            meanEqs = new int[Const.MAX_FREQUENCY];
            for (int i = 0; i < Const.MAX_FREQUENCY; i++) {
                meanEqs[i] = (leftEqs[i] + rightEqs[i]) / 2;
            }
        }
        return meanEqs;
    }

    public int[] getLeftEqs() {
        return leftEqs;
    }

    public int getLeftEq(int index) {
        if (index < 0 || index >= leftEqs.length) {
            LogUtils.e("getLeftEq IndexOutOfBoundsException List size: " + leftEqs.length + " use index: " + index);
            return 0;
        }
        return leftEqs[index];
    }

    public void setLeftEq(int index, int eq) {
        if (eq < 0) {
            eq = 0;
        }
//        if (!isDefault && eq >= Const.MAX_LEVEL) {
//            eq = Const.MAX_LEVEL - 1;
//        }
        if (index < 0 || index >= leftEqs.length) {
            LogUtils_goertek.e(TAG, "setLeftEq IndexOutOfBoundsException List size: " + leftEqs.length + " use index: " + index);
            return;
        }
        leftEqs[index] = eq;
        // 单耳值变更，清空均值，需重新计算
        meanEqs = null;
    }

    public int[] getRightEqs() {
        return rightEqs;
    }

    public int getRightEq(int index) {
        if (index < 0 || index >= rightEqs.length) {
            LogUtils_goertek.e(TAG, "getRightEq IndexOutOfBoundsException List size: " + rightEqs.length + " use index: " + index);
            return 0;
        }
        return rightEqs[index];
    }

    public void setRightEq(int index, int eq) {
        if (eq < 0) {
            eq = 0;
        }
//        if (!isDefault && eq >= Const.MAX_LEVEL) {
//            eq = Const.MAX_LEVEL - 1;
//        }
        if (index < 0 || index >= rightEqs.length) {
            LogUtils_goertek.e(TAG, "setRightEq IndexOutOfBoundsException List size: " + rightEqs.length + " use index: " + index);
            return;
        }
        rightEqs[index] = eq;
        // 单耳值变更，清空均值，需重新计算
        meanEqs = null;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public TWSEQParams getParams() {
        return new TWSEQParams(leftEqs, rightEqs, isDefault);
    }

    @NonNull
    @Override
    public String toString() {
        return "\n/****************** EQModel ******************" +
               "\n *[名称]:" + mName +
               "\n *[左耳]:" + Arrays.toString(leftEqs) +
               "\n *[右耳]:" + Arrays.toString(rightEqs) +
               "\n *[均值]:" + Arrays.toString(getMeanEqs()) +
               "\n *********************************************/";
    }

    public byte[] getWDRC() {
        return WDRC;
    }

    public void setWDRC(byte[] WDRC) {
        this.WDRC = WDRC;
    }
}
