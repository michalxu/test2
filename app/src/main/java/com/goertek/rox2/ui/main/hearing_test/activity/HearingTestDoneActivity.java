package com.goertek.rox2.ui.main.hearing_test.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.tws.entity.TWSResult;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.common.utils.MusicUtils;
import com.goertek.common.utils.Utils;
import com.goertek.common.widget.BottomDialog;
import com.goertek.db.bean.EQTable;
import com.goertek.db.bean.FrequencyWithdB;
import com.goertek.db.bean.HearingTable;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.MyApplication;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.MainActivity;
import com.goertek.rox2.ui.main.UnConnectedActivity;
import com.goertek.rox2.ui.main.hearing_test.listener_and_view.MyBarChart;
import com.goertek.rox2.ui.main.pop_dialog.EqPopDialog;
import com.goertek.rox2.ui.main.utils.SendWdrcData;
import com.goertek.rox2.ui.main.utils.WdrcDataListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HearingTestDoneActivity extends BaseActivity implements OnChartValueSelectedListener {

    public static HearingTestDoneActivity instance;
    private BottomDialog dialog;
    private int dataCount;
    private int eqDataCount = 9;
    private TextView tvHearingSuggestion;
    private TextView tvHearingSuggestionText;
    private TextView eqSuggestionText;
    private RelativeLayout userInfoLayout;
    private TextView btnEqDelete;
    private boolean isSetLineChart;  //区分设置折线图和柱状图使用的自定义x轴
    private LinearLayout lineChartLayout;
    private LinearLayout barChartLayout;
    private LinearLayout btnEQPlayLayout;
    private LineChart mLineChart;
    private MyBarChart mBarChart;
    private double[] hlData = new double[]{0,0,0,0,0,0,0,0};
    private int[] frequency = new int[]{63,125,250,500,1000,2000,4000,8000,12500};
    private Button btnSendData;
    private byte[] WDRCData = null;
    private int[] eqDbValue = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private int comeFrom; //判断是从测试页面进入的，还是从EQ展示页面进来的，还是辅听展示页面进来的
    private RoxLitePal litePal;
    ArrayList<BarEntry> eqData;//eqData.getX() 保存的db值，eqData.getY() 保存的frequency
    ArrayList<BarEntry> hearingData;
    private String userName = "michal";
    private String selectedItem;
    private int  userAge ;
    private boolean userSex ;
    private Bundle bundle;
    private TextView tvUserAge;
    private TextView tvUserName;
    private ImageView tvUserSex;
    private ImageView hearingTestDoneBack;
    private Button playOriginalMusic;
    private Button playOptimizedMusic;
    private AnimationDrawable OriginalMusicAnim;
    private AnimationDrawable OptimizedMusicAnim;
    private boolean isPlayOriginalMusic = false;
    private boolean isPlayOptimizedMusic = false;
    //判断用户有没有进行试听且没设置eq或辅听
    private boolean isSendDataClick = false;
    private boolean isAuditionClick = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_test_done);
        instance = this;
        litePal = RoxLitePal.getInstance();
        initView();
        setClickListener();
        getDataFromIntent();
        if (comeFrom == 0){
            //从测试页面进来，需要展示EQ和辅听测试结果
            hlData = bundle.getDoubleArray("hlData");
            frequency = bundle.getIntArray("frequency");
            WDRCData = bundle.getByteArray("WDRCData");
            eqDbValue = bundle.getIntArray("eqDbValue");
            userAge = bundle.getInt("userAge");
            userSex = bundle.getBoolean("userSex");
            setUserInfo();

            if (hlData!=null){
                dataCount = hlData.length/2;
                eqDataCount = eqDbValue.length/2;
                LogUtils.d("eqDataCount="+eqDataCount);
            }

            initLineChart();
            setLineChartData(hlData);
            initBarChart();
            setBarChartData(eqDbValue);
        }else if (comeFrom == 1){
            //从EQ展示页面进来，需要展示EQ的结果
            userName = bundle.getString("name");
            LogUtils.d("userName="+userName);
            initBarChart();
            getEqDataByName();
            setUserInfo();
            if (hlData!=null){
                eqDataCount = eqDbValue.length/2;
                LogUtils.d("eqDataCount="+eqDataCount);
            }
            setBarChartData(eqDbValue);
            tvHearingSuggestion.setVisibility(View.GONE);
            tvHearingSuggestionText.setVisibility(View.GONE);
            lineChartLayout.setVisibility(View.GONE);
            eqSuggestionText.setVisibility(View.GONE);
            btnSendData.setText("Send to Earbuds");
            userInfoLayout.setVisibility(View.VISIBLE);
        }else if (comeFrom == 2){
            //从辅听展示页面进来，需要展示辅听结果
            userName = bundle.getString("name");
            selectedItem = bundle.getString("selectedItem");
            List<HearingTable> hearingTableList = litePal.roxLitePalCheck.getHearingTableList();
            if (hearingTableList.size() > 0){
                getHearingDtaByName(userName);
                if (hlData!=null){
                    dataCount = hlData.length/2;
                    LogUtils.d("dataCount="+dataCount);
                }
            }
            initLineChart();
            setLineChartData(hlData);
            btnEQPlayLayout.setVisibility(View.GONE);
            barChartLayout.setVisibility(View.GONE);
            eqSuggestionText.setVisibility(View.GONE);
            btnSendData.setText("Send to Earbuds");
            userInfoLayout.setVisibility(View.VISIBLE);
            setUserInfo();
        }
    }

    private void setUserInfo() {
        tvUserAge.setText(userAge+"");
        tvUserName.setText(userName);
        if (userSex){
            tvUserSex.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.icon_female_2,null));
        }else {
            tvUserSex.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.icon_male_2,null));
        }
    }

    private void getHearingDtaByName(String userName) {
        List<HearingTable> hearingTableList = litePal.roxLitePalCheck.getHearingTableByName(userName);
        LogUtils.d("hearingTableList.size()="+hearingTableList.size());
        if (hearingTableList.size()==1){
            String data = hearingTableList.get(0).getFrequencyWithdB();
            userSex = hearingTableList.get(0).isUserSex();
            userAge = hearingTableList.get(0).getUserAge();
            List<FrequencyWithdB> dataList;
            dataList = new Gson().fromJson(data,new TypeToken<List<FrequencyWithdB>>(){}.getType());
            for (int i=0; i<dataList.size(); i++){
                hlData[i] = dataList.get(i).getdBValue();
                LogUtils.d("hlData[i]="+hlData[i]);
            }
        }else {
            LogUtils.d("eq数据有问题,eqTables.size()="+hearingTableList.size());
        }
    }

    private void getEqDataByName() {
        List<EQTable> eqTables = litePal.roxLitePalCheck.getEqTableByName(userName);
        if (eqTables.size()==1){
            String data = eqTables.get(0).getFrequencyWithdB();
            userSex = eqTables.get(0).isUserSex();
            userAge = eqTables.get(0).getUserAge();
            List<FrequencyWithdB> dataList;
            dataList = new Gson().fromJson(data,new TypeToken<List<FrequencyWithdB>>(){}.getType());
            for (int i=0; i<dataList.size(); i++){
                eqDbValue[i] = dataList.get(i).getdBValue();
            }
        }else {
            LogUtils.d("eq数据有问题,eqTables.size()="+eqTables.size());
        }
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        bundle = intent.getExtras();
        comeFrom = bundle.getInt("comeFrom");
    }

    private void setClickListener() {
        playOriginalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] data = new byte[eqDbValue.length];
                for (int i=0; i<eqDbValue.length; i++){
                    data[i] = (byte) (eqDbValue[i]- MyApplication.STANDAR_EQ[i]);
                }
                if (MusicUtils.isMusicActive()){
                    MusicUtils.stop();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LogUtils.d("MusicUtils.stop()");
                }
                Utils.setAudition(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        isAuditionClick = true;
                        //判断点击之前是否在播放音乐
                        if (isPlayOriginalMusic){
                            MusicUtils.stop();
                            LogUtils.d("isPlayOriginalMusic  stop");
                            isPlayOriginalMusic = false;
                            OriginalMusicAnim.stop();
                            playOriginalMusic.setText("Original");
                            playOriginalMusic.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.michal_bg_gray,null));
                            Drawable drawable = ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.icon_music,null);
                            drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
                            playOriginalMusic.setCompoundDrawables(drawable,null,null,null);
                        }else {
                            LogUtils.d("isPlayOriginalMusic  not  start");
                            if (isPlayOptimizedMusic){
                                LogUtils.d("isPlayOptimizedMusic  stop");
                                OptimizedMusicAnim.stop();
                                isPlayOptimizedMusic = false;
                                playOptimizedMusic.setText("Optimized");
                                playOptimizedMusic.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.michal_bg_gray,null));
                                Drawable drawable = ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.icon_music,null);
                                drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
                                playOptimizedMusic.setCompoundDrawables(drawable,null,null,null);
                            }
                            // 未播放
                            boolean result = MusicUtils.play("music.wav", new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    LogUtils.d("开始试听");
                                }
                            });
                            if (!result){
                                LogUtils.d("播放失败");
                            }
                            isPlayOriginalMusic = true;
                            playOriginalMusic.setText("");
                            playOriginalMusic.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.michal_bg_blue_retry,null));
                            OriginalMusicAnim.setBounds(0,0,OriginalMusicAnim.getMinimumWidth(),OriginalMusicAnim.getMinimumHeight());
                            playOriginalMusic.setCompoundDrawables(OriginalMusicAnim,null,null,null);
                            OriginalMusicAnim.start();
                        }

                    }
                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });
        playOptimizedMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] data = new byte[eqDbValue.length];
                for (int i=0; i<eqDbValue.length; i++){
                    data[i] = (byte) (eqDbValue[i]-MyApplication.STANDAR_EQ[i]);
                }
                if (MusicUtils.isMusicActive()){
                    MusicUtils.stop();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LogUtils.d("MusicUtils.stop()");
                }
                Utils.setAudition(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        if (object[4]==0x00){
                            isAuditionClick = true;
                            if (isPlayOptimizedMusic){
                                MusicUtils.stop();
                                LogUtils.d("isPlayOptimizedMusic  stop");
                                isPlayOptimizedMusic = false;
                                OptimizedMusicAnim.stop();
                                playOptimizedMusic.setText("Optimized");
                                playOptimizedMusic.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.michal_bg_gray,null));
                                Drawable drawable = ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.icon_music,null);
                                if (drawable!=null){
                                    drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
                                }
                                playOptimizedMusic.setCompoundDrawables(drawable,null,null,null);
                            }else {
                                LogUtils.d("isPlayOptimizedMusic  not  start");
                                if (isPlayOriginalMusic){
                                    LogUtils.d("isPlayOriginalMusic  stop");
                                    OriginalMusicAnim.stop();
                                    isPlayOriginalMusic = false;
                                    playOriginalMusic.setText("Original");
                                    playOriginalMusic.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.michal_bg_gray,null));
                                    Drawable drawable = ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.icon_music,null);
                                    if (drawable!=null){
                                        drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
                                    }
                                    playOriginalMusic.setCompoundDrawables(drawable,null,null,null);
                                }

                                // 未播放
                                boolean result = MusicUtils.play("music.wav", new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        LogUtils.d("开始试听");
                                    }
                                });
                                if (!result){
                                    LogUtils.d("播放失败");
                                }
                                isPlayOptimizedMusic = true;
                                playOptimizedMusic.setText("");
                                playOptimizedMusic.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.michal_bg_blue_retry,null));
                                OptimizedMusicAnim.setBounds(0,0,OptimizedMusicAnim.getMinimumWidth(),OptimizedMusicAnim.getMinimumHeight());
                                playOptimizedMusic.setCompoundDrawables(OptimizedMusicAnim,null,null,null);
                                OptimizedMusicAnim.start();
                            }
                        }
                    }
                    @Override
                    public void onFailed(int errorCode) {

                    }
                });


            }
        });
        hearingTestDoneBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HearingTestDoneActivity.this, MainActivity.class);
                startActivity(intent);
                instance = null;
                finish();
            }
        });
        btnEqDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EqPopDialog dialog = EqPopDialog.with(HearingTestDoneActivity.this,comeFrom,userName,litePal);
                dialog.setSelectedItem(selectedItem);
                dialog.setCanceled(true);
                dialog.show();

            }
        });
        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSendDataClick = true;
                if (comeFrom == 0){
                    dialog = BottomDialog.with(HearingTestDoneActivity.this);
                    dialog.setEditText(getString(R.string.test_save_name_place));
                    dialog.setButton(new BottomDialog.OnSaveClickListener() {
                        @Override
                        public void onSaveClick(Button btn, EditText editText) {
                            tvUserName.setText(editText.getText());
                            btn.setEnabled(false);
                            String name = editText.getText().toString();

                            // 判断是否有重名EQ
                            if (litePal.roxLitePalCheck.getEqTableByName(name).size()>0) {
                                dialog.nameExist.setVisibility(View.VISIBLE);
                                return;
                            }else if (litePal.roxLitePalCheck.getHearingTableByName(name).size()>0){
                                dialog.nameExist.setVisibility(View.VISIBLE);
                                return;
                            }else {
                                dialog.nameExist.setVisibility(View.GONE);
                            }
                            //只保存到数据库，不设置
                            litePal.roxLitePalAdd.addHearingTest(name,userAge,userSex,hearingData,WDRCData);
                            litePal.roxLitePalAdd.addEQTest(name,userAge,userSex,eqData);
                            Intent intent = new Intent(HearingTestDoneActivity.this, MainActivity.class);
                            intent.putExtra("srcActivity","testDoneActivity");
                            intent.putExtra("userName",name);
                            intent.putExtra("eqData",eqDbValue);
                            intent.putExtra("hearingData",hlData);
                            startActivity(intent);
                            instance = null;
                            finish();
                        }

                        @Override
                        public void onSaveSendClick(Button btn, EditText editText) {
                            tvUserName.setText(editText.getText());
                            tvUserName.setText(editText.getText());
                            btn.setEnabled(false);
                            String name = editText.getText().toString();

                            // 判断是否有重名EQ
                            if (litePal.roxLitePalCheck.getEqTableByName(name).size()>0) {
                                dialog.nameExist.setVisibility(View.VISIBLE);
                                return;
                            }else if (litePal.roxLitePalCheck.getHearingTableByName(name).size()>0){
                                dialog.nameExist.setVisibility(View.VISIBLE);
                                return;
                            }else {
                                dialog.nameExist.setVisibility(View.GONE);
                            }
                            LogUtils.d("完成测试，发送数据");
                            LogUtils.d("WNRC = "+ Arrays.toString(WDRCData));
                            LogUtils.d("完成测试，发送数据");
                            final boolean[] setSuccess = {false,false};
                            int[] data = new int[eqDbValue.length];
                            for (int i=0; i<eqDbValue.length; i++){
                                data[i] = (eqDbValue[i]-MyApplication.STANDAR_EQ[i]);
                            }
                            Utils.setEq(data, new IRspListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] result) {
                                    if (result[4] == 0x00){
                                        setSuccess[1] = true;
                                    }else {
                                        setSuccess[1] = false;
                                    }

                                }

                                @Override
                                public void onFailed(int errorCode) {

                                }
                            });
                            SendWdrcData sendWdrcData = new SendWdrcData(WDRCData);
                            sendWdrcData.sendData(new WdrcDataListener() {
                                @Override
                                public void onFinish() {
                                    setSuccess[0] = true;
                                    if (setSuccess[0]&&setSuccess[1]){
                                        litePal.roxLitePalAdd.addHearingTest(name,userAge,userSex,hearingData,WDRCData);
                                        litePal.roxLitePalAdd.addEQTest(name,userAge,userSex,eqData);
                                        Intent intent = new Intent(HearingTestDoneActivity.this, MainActivity.class);
                                        intent.putExtra("srcActivity","testDoneActivity");
                                        intent.putExtra("userName",name);
                                        intent.putExtra("eqData",eqDbValue);
                                        intent.putExtra("hearingData",hlData);
                                        startActivity(intent);
                                        instance = null;
                                        finish();
                                    }else {
                                        Toast.makeText(HearingTestDoneActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
//                            Utils.setWdrcDta(WDRCData, new IRspListener<byte[]>() {
//                                @Override
//                                public void onSuccess(byte[] result) {
//                                    if (result[4] == 0x00){
//                                        setSuccess[0] = true;
//                                    }else {
//                                        setSuccess[0] = false;
//                                    }
//                                    if (setSuccess[0]&&setSuccess[1]){
//                                        litePal.roxLitePalAdd.addHearingTest(name,userAge,userSex,hearingData,WDRCData);
//                                        litePal.roxLitePalAdd.addEQTest(name,userAge,userSex,eqData);
//                                        Intent intent = new Intent(HearingTestDoneActivity.this, MainActivity.class);
//                                        intent.putExtra("srcActivity","testDoneActivity");
//                                        intent.putExtra("userName",name);
//                                        intent.putExtra("eqData",eqDbValue);
//                                        intent.putExtra("hearingData",hlData);
//                                        startActivity(intent);
//                                        instance = null;
//                                        finish();
//                                    }else {
//                                        Toast.makeText(HearingTestDoneActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailed(int errorCode) {
//
//                                }
//                            });
                        }
                    });
                    dialog.show();
                }else if (comeFrom == 1){
                    //发送消息

                    //跳转到主页面
                    Intent intent = new Intent(HearingTestDoneActivity.this, MainActivity.class);
                    startActivity(intent);
                    instance = null;
                    finish();
                }

            }
        });
    }

    private void initView() {
        playOriginalMusic = findViewById(R.id.play_original_music);
        playOptimizedMusic = findViewById(R.id.play_adjust_music);
        OriginalMusicAnim = (AnimationDrawable) ResourcesCompat.getDrawable(this.getResources(),R.drawable.audition_contrast_anim,null);
        OptimizedMusicAnim = (AnimationDrawable) ResourcesCompat.getDrawable(this.getResources(),R.drawable.audition_contrast_anim,null);
        hearingTestDoneBack = findViewById(R.id.tv_hearing_test_done_back);
        tvUserAge = findViewById(R.id.eq_user_age);
        tvUserName = findViewById(R.id.eq_user_name);
        tvUserSex = findViewById(R.id.eq_user_sex);
        mLineChart = findViewById(R.id.lineChart);
        mBarChart = findViewById(R.id.bar_chart);
        btnSendData = findViewById(R.id.btn_send_data);
        tvHearingSuggestion = findViewById(R.id.tv_hearing_suggestion);
        tvHearingSuggestionText = findViewById(R.id.tv_hearing_suggestion_text);
        eqSuggestionText = findViewById(R.id.eq_suggestion);
        userInfoLayout = findViewById(R.id.eq_user_info);
        btnEqDelete = findViewById(R.id.eq_btn_delete);
        lineChartLayout = findViewById(R.id.line_chart_layout);
        barChartLayout = findViewById(R.id.bar_chart_layout);
        btnEQPlayLayout = findViewById(R.id.btn_eq_play_layout);

        XAxisRenderer.xOffset = 0;
        YAxisRenderer.yOffest = 0;
        BarLineChartBase.isDrawDashLine = -1;
    }

    private void setBarChartData(int[] eqDbValue) {
        ArrayList<BarEntry> yValsTop = new ArrayList<>();
        eqData = new ArrayList<>();
        for (int i=0;i<2*eqDataCount;i++){
//            yValsTop.add(new BarEntry(i, (float) eqDbValue[i]));
            eqData.add(new BarEntry((float) eqDbValue[i],frequency[i%eqDataCount],i<eqDataCount));//第三个参数用来判断是左耳还是右耳
        }
        for (int i=0;i<eqDataCount;i++){
            yValsTop.add(new BarEntry(i, (float) (eqDbValue[i]+eqDbValue[i+eqDataCount])/2));
//            eqData.add(new BarEntry((float) eqDbValue[i],frequency[i%eqDataCount],i<eqDataCount));//第三个参数用来判断是左耳还是右耳
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(yValsTop,"EQData");
        barDataSet.setDrawIcons(false);
        barDataSet.setDrawValues(false);

        barDataSet.setHighlightEnabled(false);
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        BarData data = new BarData(dataSets);
        data.setBarWidth(0.5f);
        mBarChart.setData(data);
        mBarChart.invalidate();
    }

    private void initBarChart() {
        mBarChart.setIsDrawHalfRoundBar(true);
        mBarChart.setStartColor(Color.parseColor("#2378F1"));
        mBarChart.setEndColor(Color.parseColor("#B3D2FF"));
        mBarChart.setIsDrawGrident(true);
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawValueAboveBar(true);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setPinchZoom(false);
        mBarChart.setGridBackgroundColor(Color.WHITE);
//        mBarChart.setBackgroundColor(getResources().getColor(R.color.light_color));
        //去掉左右边线
        mBarChart.getAxisLeft().setDrawAxisLine(true);
        mBarChart.getAxisRight().setDrawAxisLine(false);
        //去掉网格线和顶部边线
        isSetLineChart = false;
        XAxis xAxis = mBarChart.getXAxis();
        IndexAxisValueFormatter xAxisValueFormatter = new MyBarChartXAisValueFormatter();
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(8.5f);
        xAxis.setLabelCount(9);
        xAxis.setValueFormatter(xAxisValueFormatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridColor(Color.parseColor("#26091931"));
        xAxis.setGridLineWidth(2);
        YAxis yAxis = mBarChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);
        yAxis.setAxisMaximum(100f);
        yAxis.setAxisMinimum(0f);
        yAxis.setEnabled(true);
        yAxis.setAxisLineColor(Color.TRANSPARENT);
        mBarChart.getXAxis().setDrawGridLines(true);
        mBarChart.getXAxis().setDrawAxisLine(true);
        mBarChart.getAxisRight().setDrawGridLines(false);
        mBarChart.getAxisRight().setDrawLabels(false);
        mBarChart.getXAxis().setDrawLabels(true);
        mBarChart.getLegend().setEnabled(false); // Hide the legend
        mBarChart.setScaleEnabled(false);
    }

    private void initLineChart() {
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setBackground(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.michal_bg_white,null));
        mLineChart.setGridBackgroundColor(Color.WHITE);
        mLineChart.setScaleEnabled(false);
        mLineChart.setDoubleTapToZoomEnabled(false);
        isSetLineChart = true;
        IndexAxisValueFormatter axisValueFormatter = new MyLineChartXAisValueFormatter();
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAxisMaximum(dataCount);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(3f);
        xAxis.setLabelCount(dataCount,false);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.parseColor("#26091931"));
        xAxis.setGridLineWidth(2);
        LogUtils.d("x轴="+xAxis.getYOffset());
        xAxis.setValueFormatter(axisValueFormatter);
        xAxis.setLabelCount(4,false);
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(false);
        YAxis yAxis = mLineChart.getAxisLeft();
        mLineChart.getAxisRight().setEnabled(false);
        yAxis.setDrawLabels(true);
        yAxis.setDrawAxisLine(false);
        yAxis.setLabelCount(6,false);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setSpaceTop(100f);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);
//        yAxis.setValueFormatter(indexAxisValueFormatter);
        mLineChart.setDrawGridBackground(true);
//        mLineChart.setGridBackgroundColor(R.color.light_blue);
    }

    private void setLineChartData(double[] hlData) {
        List<Entry> valsComp1 = new ArrayList<>();
        List<Entry> valsComp2 = new ArrayList<>();
        hearingData = new ArrayList<>();
        for (int i=0;i<dataCount;i++){
            valsComp1.add(new Entry(i, (float) hlData[i]));//left数据是hlData前dataCount个
            valsComp2.add(new Entry(i, (float) hlData[i+dataCount]));//left数据是hlData后dataCount个
            LogUtils.d("hearingData添加："+"hlData[i]="+hlData[i]+"frequency[i]="+frequency[i]+"true");
            hearingData.add(new BarEntry((float) hlData[i],frequency[i],true));//第三个参数用来判断是左耳还是右耳
        }
        for (int i=0;i<dataCount;i++){
            LogUtils.d("hearingData添加："+"hlData[i]="+hlData[i]+"frequency[i]="+frequency[i]+"false");
            hearingData.add(new BarEntry((float) hlData[i+dataCount],frequency[i],false));
        }

        LineDataSet setComp1 = new LineDataSet(valsComp1, "Left");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.parseColor("#D900C0AD"));
        setComp1.setDrawValues(false); //设置是否显示交点处的数值
        setComp1.setDrawCircleHole(false);
        setComp1.setCircleColor(Color.parseColor("#D900C0AD"));
        setComp1.setDrawFilled(true);
        setComp1.setFillDrawable(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.line_chart_fill_color_green,null));
        setComp1.setMode(LineDataSet.Mode.LINEAR);
        LineDataSet setComp2 = new LineDataSet(valsComp2, "Right");
        setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp2.setColor(Color.parseColor("#2F7FF1"));
        setComp2.setCircleColor(Color.parseColor("#2F7FF1"));
        setComp2.setMode(LineDataSet.Mode.LINEAR);
        setComp2.setFillDrawable(ResourcesCompat.getDrawable(HearingTestDoneActivity.this.getResources(),R.drawable.line_chart_fill_color_blue,null));
        setComp2.setDrawFilled(true);
        setComp2.setDrawValues(false);
        setComp2.setDrawCircleHole(false);
        LimitLine limitLine = new LimitLine(20);
        limitLine.enableDashedLine(10,10,0);
        limitLine.setLineColor(Color.GRAY);
        LimitLine limitLine1 = new LimitLine(40);
        limitLine1.setLineColor(Color.GRAY);
        limitLine1.enableDashedLine(10,10,0);
        LimitLine limitLine2 = new LimitLine(60);
        limitLine2.setLineColor(Color.GRAY);
        limitLine2.enableDashedLine(10,10,0);
        LimitLine limitLine3 = new LimitLine(80,"Normal");
        limitLine3.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        limitLine3.setLineWidth(1);
        limitLine3.setLineColor(Color.parseColor("#2378F1"));
        limitLine3.enableDashedLine(10,10,0);
        Legend legend = mLineChart.getLegend(); // 获取图例，但是在数据设置给chart之前是不可获取的
        legend.setEnabled(true);    // 是否绘制图例
        legend.setTextColor(Color.GRAY);    // 图例标签字体颜色，默认BLACK
        legend.setTextSize(12); // 图例标签字体大小[6,24]dp,默认10dp
        legend.setTypeface(null);   // 图例标签字体
        legend.setWordWrapEnabled(false);    // 当图例超出时是否换行适配，这个配置会降低性能，且只有图例在底部时才可以适配。默认false
        legend.setMaxSizePercent(1f); // 设置，默认0.95f,图例最大尺寸区域占图表区域之外的比例
        legend.setForm(Legend.LegendForm.CIRCLE);   // 设置图例的形状，SQUARE, CIRCLE 或者 LINE
        legend.setFormSize(8); // 图例图形尺寸，dp，默认8dp
        legend.setXEntrySpace(6);  // 设置水平图例间间距，默认6dp
        legend.setYEntrySpace(0);  // 设置垂直图例间间距，默认0
//        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        List<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(setComp3);
        dataSets.add(setComp1);
        dataSets.add(setComp2);
        LineData lineData = new LineData(dataSets);
        mLineChart.getAxisLeft().addLimitLine(limitLine);
        mLineChart.getAxisLeft().addLimitLine(limitLine1);
        mLineChart.getAxisLeft().addLimitLine(limitLine2);
        mLineChart.getAxisLeft().addLimitLine(limitLine3);

        mLineChart.setData(lineData);
        mLineChart.invalidate();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog!=null){
            if (dialog.isShowing()){
                dialog.dismiss();
                dialog =null;
            }
        }
        if (comeFrom == 1){
            if (!isSendDataClick&&isAuditionClick){
                getEqDataByName();
                int[] data = new int[eqDbValue.length];
                for (int i=0; i<eqDbValue.length; i++){
                    data[i] =  (eqDbValue[i]-MyApplication.STANDAR_EQ[i]);
                }
                Utils.setEq(data, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {

                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d("onKeyDown");
        if (comeFrom == 2){
            if(keyCode == KeyEvent.KEYCODE_BACK){
                LogUtils.d("onKeyDown");
                Intent backIntent = new Intent(HearingTestDoneActivity.this, MainActivity.class);
                Bundle back_bundle = new Bundle();
                back_bundle.putString("srcActivity","SelectHearingModeDialog");
                backIntent.putExtras(back_bundle);
                setResult(RESULT_OK,backIntent);
//                startActivity(backIntent);
                HearingTestDoneActivity.this.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    public class MyLineChartXAisValueFormatter extends IndexAxisValueFormatter{
        private final String[] xStrs = new String[]{"500","1000", "2000", "4000"};
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int position = (int)value;
            if (position>=4){
                position=0;
            }
            LogUtils.d("返回lineChart");
            return xStrs[position];
        }
    }
    public class MyBarChartXAisValueFormatter extends IndexAxisValueFormatter{
        private final String[] barXStrs = new String[]{"63","125","250","500","1000", "2000", "4000","8000","12500"};
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int position = (int)value;
            if (position>=9){
                position=0;
            }
            LogUtils.d("返回barChart");
            return barXStrs[position];

        }
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
     * 连接状态变化
     *
     * @param state 连接状态
     */
    private void onConnectState(int state) {
        switch (state) {
            case ConnectState.STATE_DISCONNECTED:
                finish();
                Intent intent = new Intent(HearingTestDoneActivity.this, UnConnectedActivity.class);
                startActivity(intent);
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
}