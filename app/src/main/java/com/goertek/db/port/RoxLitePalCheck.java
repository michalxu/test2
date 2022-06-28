package com.goertek.db.port;

import com.goertek.db.bean.EQTable;
import com.goertek.db.bean.HearingTable;
import com.goertek.db.bean.HeartRateTable;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.utils.timeUtils;

import org.litepal.LitePal;

import java.util.List;

/**
 * 创建时间：2021/7/6
 *
 * @author michal.xu
 */
public class RoxLitePalCheck {
    //获取某年某月某天的所有数据，每小时一条数据
    //month 日历月份，比正常的小了，七月month=6
    public List<HeartRateTable> getHeartRateTable(int year,int month,int day ){
        String mYear = String.valueOf(year);
        String mMonth = String.valueOf(month);
        String mDay = String.valueOf(day);
        long startTime = timeUtils.getTimeStampForHour(year,month,day,0);
        long endTime = timeUtils.getTimeStampForHour(year,month,day+1,0);
        LogUtils.d("startTime="+startTime+"endTime="+endTime);
        LogUtils.d("mYear="+mYear+";mMonth="+mMonth+";mDay="+mDay);
        List<HeartRateTable> heartRateTableList = LitePal.where("time>=? and time<=?",String.valueOf(startTime),String.valueOf(endTime)).find(HeartRateTable.class);
        LogUtils.d("heartRateTableList.size=="+heartRateTableList.size());
        return heartRateTableList;
    }
    //获取某年某月某天某小时的数据
    //month 日历月份，比正常的小了，七月month=6
    public List<HeartRateTable> getHeartRateTableForHour(int year,int month,int day,int hour ){
        String mYear = String.valueOf(year);
        String mMonth = String.valueOf(month);
        String mDay = String.valueOf(day);
        String mHour = String.valueOf(hour);
        long time = timeUtils.getTimeStampForHour(year,month,day,hour);
        LogUtils.d("mYear="+mYear+";mMonth="+mMonth+";mDay="+mDay+"hour="+mHour+"time="+time);
        List<HeartRateTable> heartRateTableList = LitePal.where("time=?",String.valueOf(time)).find(HeartRateTable.class);
        LogUtils.d("heartRateTableList。size="+heartRateTableList.size());
        return heartRateTableList;
    }

    public List<EQTable> getEqTableByName(String name){
        List<EQTable> eQTableList;
        eQTableList = LitePal.where("name=?",name).find(EQTable.class);
        return eQTableList;
    }
    public List<EQTable> getEQTableList(){
        List<EQTable> eQTableList;
        eQTableList = LitePal.findAll(EQTable.class);
        return eQTableList;
    }
    public List<HearingTable> getHearingTableList(){
        List<HearingTable> hearingTableList;
        hearingTableList = LitePal.findAll(HearingTable.class);
        return hearingTableList;
    }

    public List<HearingTable> getHearingTableByName(String name){
        List<HearingTable> hearingTableList;
        hearingTableList = LitePal.where("name=?",name).find(HearingTable.class);
        return hearingTableList;
    }
}
