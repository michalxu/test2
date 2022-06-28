package com.goertek.db.port;

import android.content.ContentValues;

import com.github.mikephil.charting.data.BarEntry;
import com.goertek.db.bean.EQTable;
import com.goertek.db.bean.HearingTable;
import com.goertek.db.bean.HeartRateTable;
import com.goertek.db.bean.FrequencyWithdB;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.utils.timeUtils;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2021/7/6
 *
 * @author michal.xu
 */
public class RoxLitePalAdd {
    public void addHearingTest(String name, int age, boolean sex, List<BarEntry> data, byte[] wdrcData){
        if (data.size()==8){
            int size = data.size();
            LogUtils.d("辅听size="+size);
            HearingTable hearingTable = new HearingTable();
            List<FrequencyWithdB> dataList = new ArrayList<>();
            for (int i=0; i<data.size(); i++){
                dataList.add(new FrequencyWithdB((int)data.get(i).getY(),(int)data.get(i).getX()));
            }
            String mData = new Gson().toJson(dataList);
            hearingTable.setName(name);
            hearingTable.setUserAge(age);
            hearingTable.setUserSex(sex);
            hearingTable.setFrequencyWithdB(mData);
            hearingTable.setWdrcData(wdrcData);
            hearingTable.save();
        }
    }

    /**
     *
     * @param name 用户名
     * @param data getX()返回的是db值
     *             getY()返回的是频率值
     */
    public void addEQTest(String name, int age, boolean sex, List<BarEntry> data){
        List<EQTable> eqTableList = LitePal.where("name=?",name).find(EQTable.class);
        LogUtils.d("eq data.size()=="+data.size()+"eqTableList.size()="+eqTableList.size());
        if (data.size()==18&&eqTableList.size()==0){
            EQTable eqTable = new EQTable();

            List<FrequencyWithdB> dataList = new ArrayList<>();
            for (int i=0; i<data.size(); i++){
                dataList.add(new FrequencyWithdB((int)data.get(i).getY(),(int)data.get(i).getX()));
            }
            String mData = new Gson().toJson(dataList);
            LogUtils.d("mData="+mData);
            eqTable.setFrequencyWithdB(mData);
            eqTable.setName(name);
            eqTable.setUserAge(age);
            eqTable.setUserSex(sex);
            eqTable.save();
        }
    }

    public void addHeartRate(RoxLitePal litePal, int heartRate, long time){
        String date = timeUtils.timeStampToDate(time,"yyyy-MM-dd HH:mm:ss");
        LogUtils.d(date+"year="+date.substring(0,4)+"month="+date.substring(5,7)
                +"day="+date.substring(8,10)+"hour="+date.substring(11,13));
        int year = Integer.parseInt(date.substring(0,4));
        // 获取到的是真实月份，需要-1转换为calendar的月份
        int month = Integer.parseInt(date.substring(5,7))-1;
        int day = Integer.parseInt(date.substring(8,10));
        int hour = Integer.parseInt(date.substring(11,13));
        long mTime = timeUtils.getTimeStampForHour(year,month,day,hour);
        LogUtils.d("mTime="+mTime);
        List<HeartRateTable> heartRateTableList = litePal.roxLitePalCheck.getHeartRateTableForHour(year, month, day,hour);
        if (heartRateTableList.size()>0){
            //如果当天此小时内有数据，则更新数据，保存最大和最小数据
            int tempLowHeartRate = heartRateTableList.get(0).getLowHeartRate();
            if (tempLowHeartRate > heartRate){
                tempLowHeartRate = heartRate;
            }
            int tempHighHeartRate = heartRateTableList.get(0).getHighHeartRate();
            if (tempHighHeartRate < heartRate){
                tempHighHeartRate = heartRate;
            }
            ContentValues values = new ContentValues();
            values.put("lowHeartRate",tempLowHeartRate);
            values.put("highHeartRate",tempHighHeartRate);
            LitePal.updateAll(HeartRateTable.class,values,"time = ?", String.valueOf(mTime));
        }else {
            //如果此小时内没有数据，则最大最小值均为当前心率
            HeartRateTable heartRateTable = new HeartRateTable();
            heartRateTable.setLowHeartRate(heartRate);
            heartRateTable.setHighHeartRate(heartRate);
            heartRateTable.setTime(mTime);
            heartRateTable.save();
        }
    }

}
