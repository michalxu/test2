package com.goertek.rox2.ui.main.heart_rate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.protocol.ProtocolAPI;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.function.NotifyListener;
import com.goertek.bluetooth.protocol.model.ParseResultEvent;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.common.utils.SharedPreferenceUtils;
import com.goertek.common.utils.Utils;
import com.goertek.db.bean.HeartRateTable;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.MainActivity;
import com.goertek.rox2.ui.main.UnConnectedActivity;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingEnvironmentTestActivity;
import com.goertek.rox2.ui.main.hearing_test.listener_and_view.MyBarChart;
import com.goertek.rox2.ui.main.pop_dialog.BottomPopCalendarDialog;
import com.goertek.rox2.ui.main.pop_dialog.HeartRateBottomDialog;
import com.goertek.rox2.ui.main.utils.calendar.MonthUtils;
import com.goertek.rox2.ui.main.utils.timeUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HeartRateActivity extends BaseActivity implements NotifyListener {

    private ImageView heartRateSetting;
    private BottomPopCalendarDialog dialog;
    private RoxLitePal litePal;
    private TextView tvHeartRateValue;
    private TextView tvHeartRateTime;
    private TextView tvHeartRateDate;
    private LinearLayout btnDayWeek;
    private TextView daySwitch;
    private TextView weekSwitch;
    private Button btnHeartRateMeasure;
    private int chartChangeAction;//??????chart???????????????????????????????????????????????????????????????(????????????????????????)
    private final int SLIP_LEFT = 0;
    private final int SLIP_RIGHT = 1;
    private final int CHANGE_TO_DAY = 2;
    private final int CHANGE_TO_WEEK = 3;
    private boolean isDay = true; // ??????????????????????????????????????????????????????
    private boolean isRight = false;//?????????????????????????????????????????????
    private XAxis xAxis;
    private MyBarChart mHeartRateBarChart;
    private float downXPisition = 0f;
    private float upXPisition = 0f;
    private NumberFormat dayFormat;//???????????????????????????2???
    private Calendar calendar;
    private int selectedDay;
    private int selectedMonth;  //calendar??????mont??????0??????
    private int selectedYear;
//    private boolean hasData = false;
    private TextView lowHeartRate;
    private TextView highHeartRate;
    private TextView and;
    private String[] selectedMonthStr = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sept","Oct","Nov","Dec"};
    private float mLowHeartRate;
    private float mHighHeartRate;
    private ImageView heartRateBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);
        initView();
        setFalseData();
        getLatestData();
        initChart();
        List<BarEntry> data = getDataFromDataBaseForDay(selectedYear,selectedMonth,selectedDay);
        refreshView(data);
        refreshChart(data);
        setListener();
        ProtocolAPI.getDefault().registerNotifyListener(this);
    }

    private void getLatestData() {
        SharedPreferenceUtils.useMichalSharedPreference("latestHeartRate");
        int latestHeartRate = SharedPreferenceUtils.get("heartRate",-1);
        Long latestTime = SharedPreferenceUtils.get("time",-1L);
        if (latestHeartRate==-1){
            tvHeartRateValue.setText("--");
            tvHeartRateTime.setVisibility(View.GONE);
        }else {
            tvHeartRateTime.setVisibility(View.VISIBLE);
            String date = timeUtils.timeStampToDate(latestTime,"");
            String latestMonth = date.substring(5,7);
            String latestDay = date.substring(8,10);
            int latestHour = Integer.parseInt(date.substring(11,13));
            String latestMin = date.substring(14,16);
            if (latestHour>12){
                latestHour = latestHour-12;
                NumberFormat numberFormat = new DecimalFormat("00");
                tvHeartRateValue.setText(latestHeartRate+"");
                tvHeartRateTime.setText("Latest: "+numberFormat.format(latestHour)+":"+latestMin+" "+"PM"+ latestMonth+"/"+latestDay);
            }else {
                tvHeartRateValue.setText(latestHeartRate+"");
                tvHeartRateTime.setText("Latest: "+latestHour+":"+latestMin+" "+"AM"+ latestMonth+"/"+latestDay);
            }
        }
    }

    private void setFalseData() {
        Random random = new Random();
        int i = random.nextInt(20)+1;
        int lowHeartRate = 60+i;
        int highHeartRate= 100+i;
        int year= 2021;
        int month= 6;   //?????????0??????
//        int day= 1+i;
        int day =23;
        int hour= 2+i;
        int min= 28+i;

        long time = timeUtils.getTimeStampForHour(year,month,day,hour);
        Calendar calendar = Calendar.getInstance();
        long latestTime = calendar.getTimeInMillis();
        litePal.roxLitePalAdd.addHeartRate(litePal,lowHeartRate,latestTime);
        litePal.roxLitePalAdd.addHeartRate(litePal,highHeartRate,time);
        SharedPreferenceUtils.useMichalSharedPreference("latestHeartRate");
        SharedPreferenceUtils.put("heartRate",lowHeartRate);
        SharedPreferenceUtils.put("time",latestTime);
    }

    private List<BarEntry> getDataFromDataBaseForDay(int year,int month,int day ) {
        List<HeartRateTable> dataList = litePal.roxLitePalCheck.getHeartRateTable(year,month,day);
        LogUtils.d("dataList.size="+dataList.size());
        List<BarEntry> data = new ArrayList<BarEntry>(){};
        for (int i =0; i<24; i++){
            BarEntry entry = new BarEntry(i,0f,0f);
            data.add(entry);
        }
        if (dataList.size()>0){
            for (int j=0; j<dataList.size(); j++){
                long time =  dataList.get(j).getTime();
                calendar.setTimeInMillis(time);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                float lowHeartRate = dataList.get(j).getLowHeartRate();
                float highHeartRate = dataList.get(j).getHighHeartRate();
                BarEntry entry = new BarEntry(hour,lowHeartRate,highHeartRate);
                data.set(hour,entry);
            }
        }
        return data;
    }

    private List<BarEntry> getDataFromDataBaseByWeek(int year,int month,int day ) {
        int lowHeartRateInWeek;  //???????????????????????????
        int highHeartRateInWeek;  //???????????????????????????
        calendar.set(year, month, day);
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);  //????????????????????????
        int dayInMonth = MonthUtils.getMonthDayCount(calendar); //?????????????????????
        List<BarEntry> data = new ArrayList<BarEntry>(){};
        for (int i =0; i<7; i++){
            //???????????????????????????7????????????
            BarEntry entry = new BarEntry(i,0f,0f);
            data.add(entry);
        }
        LogUtils.d("day="+day+";dayweek="+dayInWeek);
        int fisrtDay = day - dayInWeek+1;  //??????????????????????????????????????????
        int lastDay = day + 7 - dayInWeek;  //?????????????????????????????????????????????
        if (fisrtDay<1){
            //?????????????????????1????????????????????????????????????????????????????????????????????????
            calendar.set(year,month-1,day);  //???????????????????????????????????????????????????
            int preMonthDays = MonthUtils.getMonthDayCount(calendar);   //????????????????????????
            fisrtDay = preMonthDays+1 + fisrtDay;          //????????????????????????????????????
            for (int i = fisrtDay; i< preMonthDays+1; i++){
                //????????????????????????
                getData(data, year, month - 1, i, i - fisrtDay);
            }
            for (int i= 1; i<7-dayInWeek+2; i++){
                //?????????????????????
                getData(data, year, month, i, dayInWeek + i - 3);
            }
        }else if (lastDay > dayInMonth){
            //????????????????????????????????????????????????????????????????????????
            calendar.set(year, month+1, day); //???????????????????????????????????????????????????
            lastDay = lastDay - dayInMonth;   //???????????????????????????????????????
            for (int j=fisrtDay; j<dayInMonth+1; j++){
                //?????????????????????
                getData(data, year, month, j, j - fisrtDay);
            }
            for (int j=1; j<lastDay; j++){
                //????????????????????????
                getData(data, year, month + 1, j, j + dayInWeek);
            }
        }else {
            for (int j=fisrtDay; j<lastDay+1; j++){
                //?????????????????????
                getData(data, year, month, j, j - fisrtDay);
            }
        }

        return data;
    }

    private void getData(List<BarEntry> data, int year, int month, int day, int i ){
        //??????????????????????????????
        List<HeartRateTable> dataList = litePal.roxLitePalCheck.getHeartRateTable(year,month,day);
        float lowHeartRateDay ;
        float highHeartRateDay ;
        //???????????????????????????????????????????????????????????????????????????????????????????????????
        if (dataList.size()>0){
            lowHeartRateDay = dataList.get(0).getLowHeartRate();
            highHeartRateDay = dataList.get(0).getHighHeartRate();
            for (int j=0; j<dataList.size(); j++){
                float lowHeartRate = dataList.get(j).getLowHeartRate();
                float highHeartRate = dataList.get(j).getHighHeartRate();
                if (lowHeartRateDay>lowHeartRate){
                    lowHeartRateDay = (int) lowHeartRate;
                }
                if (highHeartRateDay<highHeartRate){
                    highHeartRateDay = (int) highHeartRate;
                }
                BarEntry entry = new BarEntry(i,lowHeartRateDay,highHeartRateDay);
                data.set(i,entry);
            }
//            int tempDay = dataList.get(0).getDay();

        }
    }
    private void setListener() {
        heartRateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        heartRateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean[] switchStatus = {false,false};
                Utils.getHeartRateDetectAutoStatus(new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        LogUtils.d("object=="+ ProtocolUtils.bytesToHexStr(object));
                        if (object[5]==0x01){
                            switchStatus[0] = true;
                        }else if (object[5]==0x00){
                            switchStatus[0] = false;
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
                Utils.getHeartRateDetectAccurately(new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        LogUtils.d("object=="+ ProtocolUtils.bytesToHexStr(object));
                        if (object[5]==0x01){
                            switchStatus[1] = true;
                        }else if (object[5]==0x00){
                            switchStatus[1] = false;
                        }
                        HeartRateBottomDialog dialog = HeartRateBottomDialog.with(HeartRateActivity.this);
                        dialog.setButton(new HeartRateBottomDialog.OnClickListener() {
                            @Override
                            public void heartReateDetectOnClick(boolean isDetectOn) {
                                Utils.setHeartRateDetectAuto(isDetectOn, new IRspListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] object) {
                                        LogUtils.d("??????????????????");
                                    }

                                    @Override
                                    public void onFailed(int errorCode) {

                                    }
                                });
                            }

                            @Override
                            public void highPrecisionOnClick(boolean isHighPrecisionOn) {
                                Utils.setHeartRateDetectAccurately(isHighPrecisionOn, new IRspListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] object) {
                                        LogUtils.d("??????????????????");
                                    }

                                    @Override
                                    public void onFailed(int errorCode) {

                                    }
                                });
                            }
                        });
                        dialog.setDetectOn(switchStatus[0]);
                        dialog.setHighPrecisionOn(switchStatus[1]);
                        LogUtils.d("switchStatus[0]="+switchStatus[0]+"???switchStatus[1]="+switchStatus[1]);
                        dialog.show();
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });
        //????????????
        tvHeartRateDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog == null){
                    LogUtils.d("??????dialog=");
                   dialog = BottomPopCalendarDialog.with(HeartRateActivity.this,selectedMonth,selectedDay);
                }else {
                    dialog.initMonthView(selectedYear,selectedMonth);
                }
                dialog.setSelectedMonth(selectedMonth);
                dialog.setSelectedDay(selectedDay);
                dialog.setCancelClickListener(new BottomPopCalendarDialog.SetListener() {
                    @Override
                    public void onCancelClick() {
                        dialog.dismiss();
                    }
                    @Override
                    public void onDayClick() {
                        selectedMonth = dialog.month+dialog.isCurrentMonth;
                        selectedDay = dialog.selectedDay;
                        dialog.setSelectedMonth(selectedMonth);
                        dialog.setSelectedDay(selectedDay);
                        LogUtils.d("selectedMonth = "+selectedMonth+";selectedDay="+selectedDay);
                        tvHeartRateDate.setText(selectedMonthStr[selectedMonth]+" "+dayFormat.format(selectedDay));
                        List<BarEntry> data;
                        if (isDay){
                            data = getDataFromDataBaseForDay(selectedYear,selectedMonth,selectedDay);
                        }else {
                            data = getDataFromDataBaseByWeek(selectedYear,selectedMonth,selectedDay);
                        }
                        refreshView(data);
                        refreshChart(data);
//                        dialog.dismiss();
                        dialog.cancel();
//                        dialog.
                    }
                });
                dialog.show();
            }
        });
        //??????????????????????????????
        btnDayWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDay = !isDay;
                if (!isDay){
                    chartChangeAction = CHANGE_TO_WEEK;
                    setAxis(isDay);
                    LogUtils.d("????????????week");
                    List<BarEntry> data = getDataFromDataBaseByWeek(selectedYear,selectedMonth,selectedDay);
                    refreshView(data);
                    refreshChart(data);
                    daySwitch.setBackgroundResource(0);
                    daySwitch.setTextColor(Color.parseColor("#FF091931"));
                    weekSwitch.setBackground(getResources().getDrawable(R.drawable.michal_bg_blue));
                    weekSwitch.setTextColor(Color.WHITE);
                }else {
                    chartChangeAction = CHANGE_TO_DAY;
                    setAxis(isDay);
                    LogUtils.d("????????????day");
                    List<BarEntry> data = getDataFromDataBaseForDay(selectedYear,selectedMonth,selectedDay);
                    refreshView(data);
                    refreshChart(data);
                    daySwitch.setBackground(getResources().getDrawable(R.drawable.michal_bg_blue));
                    daySwitch.setTextColor(Color.WHITE);
                    weekSwitch.setBackgroundResource(0);
                    weekSwitch.setTextColor(Color.parseColor("#FF091931"));
                }

            }
        });
        //?????????????????????????????????????????????????????????
        mHeartRateBarChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        downXPisition = motionEvent.getX();
                        upXPisition = downXPisition;
                        break;
                    case MotionEvent.ACTION_UP:
                        upXPisition = motionEvent.getX();
                        //????????????????????????????????????
                        if ((upXPisition-downXPisition)>30){
                            chartChangeAction = SLIP_RIGHT;
                            isRight = true;
                            setAxis(isDay);
                            changeDate(isRight,isDay);
                            List<BarEntry> data;
                            if (isDay){
                                data = getDataFromDataBaseForDay(selectedYear,selectedMonth,selectedDay);
                            }else {
                                data = getDataFromDataBaseByWeek(selectedYear,selectedMonth,selectedDay);
                            }
                            refreshView(data);
                            refreshChart(data);
                        }else if ((upXPisition-downXPisition)<-30){
                            chartChangeAction = SLIP_LEFT;
                            isRight = false;
                            setAxis(isDay);
                            changeDate(isRight,isDay);
                            List<BarEntry> data;
                            if (isDay){
                                data = getDataFromDataBaseForDay(selectedYear,selectedMonth,selectedDay);
                            }else {
                                data = getDataFromDataBaseByWeek(selectedYear,selectedMonth,selectedDay);
                            }
                            refreshView(data);
                            refreshChart(data);
                        }

                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        btnHeartRateMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setHeartRateOnMeasure(new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] payload) {
                        int heartRate = Utils.byteToInt(payload[5]);
                        tvHeartRateValue.setText(heartRate+"");
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH)+1;
                        int hour = calendar.get(Calendar.HOUR);
                        int min = calendar.get(Calendar.MINUTE);
                        NumberFormat numberFormat = new DecimalFormat("00");
                        //????????????????????????
                        long timeStamp = timeUtils.getTimeStampForHour(year,month,day,hour);
                        //??????????????????????????????
                        if (litePal ==null){
                            litePal = RoxLitePal.getInstance();
                        }
                        litePal.roxLitePalAdd.addHeartRate(litePal,heartRate,timeStamp);
                        List<BarEntry> data;
                        if (isDay){
                            data = getDataFromDataBaseForDay(selectedYear,selectedMonth,selectedDay);
                        }else {
                            data = getDataFromDataBaseByWeek(selectedYear,selectedMonth,selectedDay);
                        }
                        refreshView(data);
                        refreshChart(data);
                        if (hour>12){
                            hour = hour-12;
                            tvHeartRateTime.setText("Latest: "+numberFormat.format(hour)+":"+numberFormat.format(min)+" PM "+numberFormat.format(month)+"/"+numberFormat.format(day));
                        }else {
                            tvHeartRateTime.setText("Latest: "+numberFormat.format(hour)+":"+numberFormat.format(min)+" AM "+numberFormat.format(month)+"/"+numberFormat.format(day));
                        }
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });

            }
        });
    }

    private void initView() {
        litePal = RoxLitePal.getInstance();
        heartRateBack = findViewById(R.id.tv_heart_rate_back);
        lowHeartRate = findViewById(R.id.low_heart_rate);
        highHeartRate = findViewById(R.id.high_heart_rate);
        and = findViewById(R.id.and);
        tvHeartRateValue = findViewById(R.id.tv_heart_rate_value);
        tvHeartRateTime = findViewById(R.id.tv_heart_time);
        tvHeartRateDate = findViewById(R.id.tv_heart_rate_date);
        heartRateSetting = findViewById(R.id.tv_heart_rate_setting);
        btnDayWeek = findViewById(R.id.day_week_switch);
        daySwitch = findViewById(R.id.day_switch);
        weekSwitch = findViewById(R.id.week_switch);
        btnHeartRateMeasure = findViewById(R.id.tv_heart_rate_measure);
        mHeartRateBarChart = findViewById(R.id.heart_rate_bar_chart);
        calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        dayFormat = new DecimalFormat("00");
        tvHeartRateDate.setText(selectedMonthStr[selectedMonth]+" "+dayFormat.format(selectedDay));
    }

    private void initChart() {
        mHeartRateBarChart.setIsDrawRoundBar(true);
        mHeartRateBarChart.getDescription().setEnabled(false);
        //??????x?????????

        xAxis = mHeartRateBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.parseColor("#FFD6D8DA"));
        xAxis.setAxisLineWidth(1);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        setAxis(isDay);  //???????????????????????????
        HeartRateYAxisValueFormatter yAxisValueFormatter = new HeartRateYAxisValueFormatter();
        YAxis yAxis = mHeartRateBarChart.getAxisLeft();
        yAxis.setAxisMaximum(192f);
        yAxis.setAxisMinimum(0f);
        yAxis.setLabelCount(4,true);
        //?????????????????????
        yAxis.setDrawGridLines(true);
        yAxis.setGridLineWidth(1);
        yAxis.setGridColor(Color.parseColor("#FFD6D8DA"));
        yAxis.setDrawLabels(true);
        yAxis.setAxisLineColor(Color.parseColor("#FFD6D8DA"));
        yAxis.setAxisLineWidth(1.2f);
        yAxis.setEnabled(true);
        yAxis.setGranularity(1f);

        //??????????????????????????????
        yAxis.setValueFormatter(yAxisValueFormatter);

        YAxis rightYAsis = mHeartRateBarChart.getAxisRight();
        rightYAsis.setDrawAxisLine(true);
        rightYAsis.setAxisLineColor(Color.parseColor("#FFD6D8DA"));
        rightYAsis.setAxisLineWidth(1.2f);
        rightYAsis.setDrawGridLines(false);
        rightYAsis.setEnabled(true);
        rightYAsis.setDrawLabels(false);
//        mHeartRateBarChart.setBackgroundColor(Color.WHITE);
        //????????????
        mHeartRateBarChart.setScaleEnabled(false);
    }

    private void refreshChart(List<BarEntry> data) {
        mHeartRateBarChart.clear();
        BarDataSet barDataSet = new BarDataSet(data,"day");
        //?????????????????????
        barDataSet.setDrawValues(false);
        //?????????????????????
        barDataSet.setColor(Color.GRAY);
        barDataSet.setHighlightEnabled(true);
        //???????????????
        Legend legend = mHeartRateBarChart.getLegend();
        legend.setEnabled(false);
        BarData barData = new BarData(barDataSet);
        if (isDay){
            barData.setBarWidth(0.5f);
        }else {
            barData.setBarWidth(0.2f);
        }
        mHeartRateBarChart.setData(barData);
        mHeartRateBarChart.getData().notifyDataChanged();
        mHeartRateBarChart.notifyDataSetChanged();
        mHeartRateBarChart.invalidate();

    }

    private void changeDate(boolean isRight,boolean isDay) {
        int year = calendar.get(Calendar.YEAR);
        if (isRight&&isDay){
            selectedDay--;
            if (selectedDay == 0){
                selectedMonth--;
                if (selectedMonth == -1){
                    selectedMonth = 11;
                     year--;
                }
                selectedDay = timeUtils.getDaysByYearMonth(year,selectedMonth);
            }
        }else if (!isRight&&isDay){
            selectedDay++;
            int dayInMonth = timeUtils.getDaysByYearMonth(year,selectedMonth);
            if (selectedDay > dayInMonth){
                selectedDay = 1;
                selectedMonth++;
                if (selectedMonth > 12){
                    selectedMonth = 0;
                }
            }
        }else if (isRight&&!isDay){
            LogUtils.d("????????????selectedDay="+selectedDay+";selectedMonth="+selectedMonth);
            selectedDay = selectedDay-7;
            if (selectedDay<1){
                selectedMonth--;
                if (selectedMonth == -1){
                    selectedMonth = 11;
                    year--;
                }
                int preMonthDay = timeUtils.getDaysByYearMonth(year,selectedMonth);
                selectedDay = preMonthDay+selectedDay;
            }
            LogUtils.d("selectedDay="+selectedDay+";selectedMonth="+selectedMonth);
        }else if (!isRight&&!isDay){
            selectedDay = selectedDay+7;
            int currentMonthDay = timeUtils.getDaysByYearMonth(year,selectedMonth);
            if (selectedDay>currentMonthDay){
                selectedMonth++;
                if (selectedMonth > 12){
                    selectedMonth = 0;
                }
                int nextMonthDay = selectedDay-currentMonthDay;
                selectedDay = nextMonthDay;
            }

        }
        tvHeartRateDate.setText(selectedMonthStr[selectedMonth]+" "+dayFormat.format(selectedDay));
    }

    private void setAxis(boolean isDay){
        //?????????x????????????
        IndexAxisValueFormatter indexAxisValueFormatter = new HeartRateIndexAxisValueFormatter();
        xAxis.setValueFormatter(indexAxisValueFormatter);
        //????????????????????????x????????????Label
        if (isDay){
            XAxisRenderer.xOffset = 60;
            YAxisRenderer.yOffest = 120;
            BarLineChartBase.isDrawDashLine = 0;
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(23.5f);
            xAxis.setLabelCount(23);
        }else {
            XAxisRenderer.xOffset = 0;
            YAxisRenderer.yOffest = 120;
            BarLineChartBase.isDrawDashLine = 1;
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(6.5f);
            xAxis.setLabelCount(6);
        }
    }

    @Override
    public void onNotify(ParseResultEvent result) {
        //??????????????????????????????????????????????????????????????????
        LogUtils.d("onNotify heart");
        byte[] payload = result.getPayload();
        int heartRate = Utils.byteToInt(payload[5]);
        tvHeartRateValue.setText(heartRate+"");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+1;
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        NumberFormat numberFormat = new DecimalFormat("00");
        //????????????????????????
        long timeStamp = timeUtils.getTimeStampForHour(year,month,day,hour);
        //??????????????????????????????
        if (litePal ==null){
            litePal = RoxLitePal.getInstance();
        }
        litePal.roxLitePalAdd.addHeartRate(litePal,heartRate,timeStamp);
        List<BarEntry> data;
        if (isDay){
            data = getDataFromDataBaseForDay(selectedYear,selectedMonth,selectedDay);
        }else {
            data = getDataFromDataBaseByWeek(selectedYear,selectedMonth,selectedDay);
        }
        refreshView(data);
        refreshChart(data);
        if (hour>12){
            hour = hour-12;
            tvHeartRateTime.setText("Latest: "+numberFormat.format(hour)+":"+numberFormat.format(min)+" PM "+numberFormat.format(month)+"/"+numberFormat.format(day));
        }else {
            tvHeartRateTime.setText("Latest: "+numberFormat.format(hour)+":"+numberFormat.format(min)+" AM "+numberFormat.format(month)+"/"+numberFormat.format(day));
        }
    }
    private class HeartRateIndexAxisValueFormatter extends IndexAxisValueFormatter{

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if (isDay){
                switch ((int) value){
                    case 0:
                        return "00:00";
                    case 6:
                        return "06:00";
                    case 12:
                        return "12:00";
                    case 18:
                        return "18:00";

                    default:
                        return "";
                }
            }else {
                switch ((int) value){
                    case 0:
                        return "Sun";
                    case 1:
                        return "Mon";
                    case 2:
                        return "Tue";
                    case 3:
                        return "Wed";
                    case 4:
                        return "Thu";
                    case 5:
                        return "Fri";
                    case 6:
                        return "Sat";
                    default:
                        return "";
                }
            }
        }
    }
    private class HeartRateYAxisValueFormatter extends IndexAxisValueFormatter{

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int position = (int)value;
//            LogUtils.d("value = "+value);
            if (position == 0){
                return "0";
            }else if (position == 64){
                return "64";
            }else if (position == 128){
                return "128";
            }else if (position == 192){
                return "192";
            }
            return "";
        }
    }
    private void refreshView(List<BarEntry> data ){
        float hasData = 0;
        if (data.size()>0){
            Entry entry = getLowHeartRate(data);
            hasData = entry.getX();
            if (hasData>0){
                mLowHeartRate = entry.getY();
            }
            mHighHeartRate = getHighHeartRate(data);
        }
        if (hasData>0){
            and.setVisibility(View.VISIBLE);
            highHeartRate.setVisibility(View.VISIBLE);
            highHeartRate.setText((int)mHighHeartRate+"");
            lowHeartRate.setText((int)mLowHeartRate+"");

        }else {
            lowHeartRate.setText("- -");
            and.setVisibility(View.GONE);
            highHeartRate.setVisibility(View.GONE);
        }
    }

    /**
     * ?????????data??????????????????????????????
     * @param data  getY() 1????????????lowHeartRate
     *              getData() 1????????????highHeartRate
     * @return entry   x????????????????????????y???????????????
     */
    private Entry getLowHeartRate(List<BarEntry> data){
        int hasData= 0;
        float lowHeartRate = -1;
        Entry entryResult = new Entry(hasData,lowHeartRate);
        for (int i=0;i<data.size();i++){
            BarEntry entry = data.get(i);
            float temp = entry.getY();
            //?????????????????????0???????????????????????????????????????
            if (temp>0){
                if (lowHeartRate==-1){
                    lowHeartRate = temp;
                    hasData = 2;
                }else if (lowHeartRate>temp){
                    lowHeartRate = temp;
                }
            }
        }
        entryResult.setX(hasData);
        entryResult.setY(lowHeartRate);
        return entryResult;
    }
    /**
     * ?????????data??????????????????????????????
     * @param data
     * @return
     */
    private float getHighHeartRate(List<BarEntry> data){
        float highlowHeartRate;
        highlowHeartRate = data.get(0).getY();
        for (int i=0;i<data.size();i++){
            BarEntry entry = data.get(i);
            float temp = (float) entry.getData();
            if (temp > highlowHeartRate){
                highlowHeartRate = temp;
            }
        }
        return highlowHeartRate;
    }

    @Override
    protected boolean isRegisterLocalBroadcast() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getCode() == Const.EventCode.ConnectStateCode) {
            onConnectState((int) event.getData());
        }
    }
    /**
     * ??????????????????
     *
     * @param state ????????????
     */
    private void onConnectState(int state) {
        switch (state) {
            case ConnectState.STATE_DISCONNECTED:
                Intent intent = new Intent(HeartRateActivity.this, UnConnectedActivity.class);
                startActivity(intent);
                finish();
                break;
            case ConnectState.STATE_CONNECTING:
            case ConnectState.STATE_CONNECTED:
//                mBluetoothTextView.setText(R.string.drawer_bluetooth_connecting);
                break;
            case ConnectState.STATE_DATA_READY:
//                mBluetoothTextView.setText(R.string.drawer_bluetooth_connected);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProtocolAPI.getDefault().unregisterNotifyListener(this);
    }
}