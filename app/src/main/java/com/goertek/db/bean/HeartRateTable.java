package com.goertek.db.bean;

import org.litepal.crud.LitePalSupport;

/**
 * 创建时间：2021/7/6
 *
 * @author michal.xu
 */
public class HeartRateTable extends LitePalSupport {
    private int lowHeartRate;  //一个时间段中，最低心率
    private int highHeartRate; //一个时间段中，最高心率
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


//    //需要保存，某年某月某日某小时内的心率(最高、最低)
//    private int year;
//    private int month;
//    private int day;
//    private int hour;
    public int getLowHeartRate() {
        return lowHeartRate;
    }

    public int getHighHeartRate() {
        return highHeartRate;
    }

//    public int getYear() {
//        return year;
//    }
//
//    public int getMonth() {
//        return month;
//    }
//
//    public int getDay() {
//        return day;
//    }
//
//    public int getHour() {
//        return hour;
//    }

    public void setLowHeartRate(int lowHeartRate) {
        this.lowHeartRate = lowHeartRate;
    }

    public void setHighHeartRate(int highHeartRate) {
        this.highHeartRate = highHeartRate;
    }

//    public void setYear(int year) {
//        this.year = year;
//    }
//
//    public void setMonth(int month) {
//        this.month = month;
//    }
//
//    public void setDay(int day) {
//        this.day = day;
//    }
//
//    public void setHour(int hour) {
//        this.hour = hour;
//    }



}
