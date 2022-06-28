package com.goertek.db.bean;

/**
 * 创建时间：2021/7/20
 *
 * @author michal.xu
 */
public class FrequencyWithdB {
    private int frequency;

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getdBValue() {
        return dBValue;
    }

    public void setdBValue(int dBValue) {
        this.dBValue = dBValue;
    }

    private int dBValue;

    /**
     *
     * @param frequency 频点的值
     * @param dBValue   频点对应的db值
     */
    public FrequencyWithdB(int frequency, int dBValue){
        this.frequency = frequency;
        this.dBValue = dBValue;
    }
}
