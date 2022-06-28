package com.goertek.db.bean;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import org.litepal.crud.LitePalSupport;

/**
 * 创建时间：2021/7/6
 *
 * @author michal.xu
 */
public class HearingTable extends LitePalSupport {
    private String name;  //测试的用户名
    private int userAge;
    private boolean userSex;
    //将dbVlue和frequency放在一个entry中，一个频点对应一个db值，左右耳总共有8个数据
    private String frequencyWithdB;
    private byte[] WdrcData;
    public String getFrequencyWithdB() {
        return frequencyWithdB;
    }

    public void setFrequencyWithdB(String frequencyWithdB) {
        this.frequencyWithdB = frequencyWithdB;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public boolean isUserSex() {
        return userSex;
    }

    public void setUserSex(boolean userSex) {
        this.userSex = userSex;
    }

    public byte[] getWdrcData() {
        return WdrcData;
    }

    public void setWdrcData(byte[] wdrcData) {
        WdrcData = wdrcData;
    }
}
