package com.goertek.db.bean;

import com.github.mikephil.charting.data.Entry;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * 创建时间：2021/7/6
 *
 * @author michal.xu
 */
public class EQTable extends LitePalSupport {
    private String name;  //测试的用户名
    private int userAge;
    private boolean userSex;
    //包含了多个频点和其对应的dB值，以key_value依次排列
    private String frequencyWithdB;
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
}
