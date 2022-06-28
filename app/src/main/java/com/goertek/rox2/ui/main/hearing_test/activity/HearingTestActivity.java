package com.goertek.rox2.ui.main.hearing_test.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.common.entity.EQModel;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.UnConnectedActivity;
import com.goertek.rox2.ui.main.hearing_test.listener_and_view.HearingAutoTestListener;
import com.goertek.rox2.ui.main.hearing_test.listener_and_view.HearingTestmanuallyListener;
import com.goertek.rox2.ui.main.hearing_test.listener_and_view.MyCustomView;
import com.lyratone.hearingaid.audio.EHPP;
import com.lyratone.hearingaid.audio.LyratoneTestFitting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.goertek.common.utils.Utils.getContext;

public class HearingTestActivity extends BaseActivity {

    private int userAge;
    private boolean userSex;
    private ImageView hearingAnimationImage;
    private AnimationDrawable hearingAnimation;
    /** true 左耳测试；false 右耳测试 */
    private boolean isLeft = true;
    private boolean isPause = false;
    // 聆通SDK调用对象（测听验配模块）
    private LyratoneTestFitting lyratoneTestFitting;
    private EHPP ehpp = null;
    private final int[] gains = new int[]{10, 10, 10, 10, 10, 10, 10, 10};
    private EQModel eqModel;
    private final int[] eqDbValue = new int[]{0, 0, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0,0};
    private int testState = -1;//从频率500到频率4000，testState依次递增，0~3依次代表每个测试项
    private final int testNumbers = 9;
    private int dBVal = 5;//播放音乐的音量
    // 测听时用的9个频点
    private final int[] frequency = {500, 1000, 2000, 4000};
    private final int[] eqFrequency = new int[]{63,125,250,500,1000,2000,4000,8000,12500};
    String wdrc_data_txt = "";
    private final int CHANGE_VALUE = 100;
    public static HearingAutoTestListener mHearingAutoTestListener;
    public static HearingTestmanuallyListener mHearingTestManuallyListener;
    private RelativeLayout btnHearingTestNext;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private TextView earTypeText;
    private LinearLayout leftEarDoneLayout;
    private LinearLayout hearingTestLayout;
    private Button btnNestRightEar;
    private ImageView hearingTestBack;
    private final int dbValueOffset = 1;
    private final int dbIntervalTime = 100;
    //处理音乐改变的消息，调整音乐音量以及停止播放音乐并还原初始值
    private final Handler playHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CHANGE_VALUE){
                if (dBVal<=90){
                    dBVal +=dbValueOffset;
                    LogUtils.d("调整音量为："+dBVal+"频率为："+eqFrequency[testState%testNumbers]);
                    lyratoneTestFitting.setTestConfig(eqFrequency[testState%testNumbers], !isLeft, dBVal);
                    if (mHearingAutoTestListener!=null){
                        LogUtils.d("监听到音量变化，自动增加到 dBVal = "+dBVal);
                        mHearingAutoTestListener.ondBChange(dBVal);
                    }
                    sendValueChangeMessage();
                }else {
                    Toast.makeText(getContext(),"已达到最大音量",Toast.LENGTH_SHORT).show();
                }

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_test);
        initView();
        initData();
        setClickListener();
        startTest();

    }

    private void setClickListener() {
        hearingTestBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnNestRightEar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d("测右耳");
                isLeft = false;
                leftEarDoneLayout.setVisibility(View.GONE);
                hearingTestLayout.setVisibility(View.VISIBLE);
                testState++;
                dBVal = 5;
                lyratoneTestFitting.setTestConfig(eqFrequency[testState%testNumbers], !isLeft, dBVal);
                sendValueChangeMessage();
                int progress = testState%testNumbers+1;
                progressBar.setProgress(progress+1);
                progressTextView.setText(progress+"/"+testNumbers);
                earTypeText.setText("右耳");
            }
        });
        btnHearingTestNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d("next click");
                if (mHearingAutoTestListener!=null){
                    mHearingAutoTestListener.onNextTestClicked();
                }
                LogUtils.d("eqFrequency[]="+testState+"="+eqFrequency[testState%9]);
                if (testState > 2 && testState < 7){
                    gains[testState-3] = dBVal;
                    LogUtils.d("gains[testState-3]="+testState+"="+dBVal);
                }else if (testState > 10 && testState < 15){
                    gains[testState-7] = dBVal;
                    LogUtils.d("gains[testState-7]="+testState+"="+dBVal);
                }
                eqDbValue[testState] = dBVal;
                if (isLeft){
                    eqModel.setLeftEq(testState%eqFrequency.length,dBVal);
                }else {
                    eqModel.setRightEq(testState%eqFrequency.length,dBVal);
                }
                LogUtils.d("eqDbValue["+testState+"]="+dBVal);
                dBVal = 5;
                lyratoneTestFitting.setTestConfig(eqFrequency[testState%testNumbers], !isLeft, dBVal);
                playHandler.removeMessages(CHANGE_VALUE);
                // 用户点击YES
                if (testState<2*testNumbers-1 && (testState != testNumbers-1)) {
                    testState++;
                    // 测试还未结束，下一频率测试
                    LogUtils.d("用户点击YES,测试还未结束，下一频率测试");
                    // 1、刷新界面
                    dBVal = 5;
                    lyratoneTestFitting.setTestConfig(eqFrequency[testState%testNumbers], !isLeft, dBVal);
                    LogUtils.d("isleft = "+isLeft);
//                    vFreq = frequency[testState%testNumbers];
                    LogUtils.d("stat="+testState+"gains[stat]"+dBVal+"vFreq="+ eqFrequency[testState%testNumbers] );
                    sendValueChangeMessage();
                    int progress = testState%testNumbers+1;
                    progressBar.setProgress(progress+1);
                    progressTextView.setText(progress+"/"+testNumbers);
                } else {
                    //判断是否是左耳或右耳测试的最后一项
                    if (!isLeft){
                        testState = -1;
                        hearingAnimation.stop(); //测听结束，结束动画
                        LogUtils.d("右耳测试完毕，关闭聆通");
                        btnHearingTestNext.setClickable(false);
                        lyratoneTestFitting.stopTestAudio();
                        //测试完成，获取WDRC数据，向耳机发送数据
                        // 设置听力图 8个频点的记录值(左耳4个，右耳4个。顺序 500~4000)
                        lyratoneTestFitting.setHlVal(ehpp.getHL_VAL());
                        LogUtils.d(Arrays.toString(gains));
                        lyratoneTestFitting.setAudiometryData(gains);
                        double[] hlData = lyratoneTestFitting.getAudiometryHLData();
                        String text = "L[" + hlData[0] + "," + hlData[1] + "," + hlData[2] + "," + hlData[3] + "] R[" + hlData[4] + "," + hlData[5] + "," + hlData[6] + "," + hlData[7] + "]";
                        StringBuilder eq = new StringBuilder();
                        for (int i=0; i<eqDbValue.length; i++){
                            eq.append(eqDbValue[i]).append(",");
                        }
                        LogUtils.d("eq=="+eq.toString());
                        LogUtils.d(text);
                        // 获取安静场景的WDRC数据[1084*2*4]
                        // 前提是需要设置了听力图 8个频点的记录值 lyratoneTestFitting.setAudiometryData
                        byte[] wdrc_data = new byte[LyratoneTestFitting.DATA_LEN * 2 * 4];
                        byte[] buff;

                        lyratoneTestFitting.setSpkMPO(ehpp.getAH_MPO_L(), ehpp.getAH_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getAH_MPI_L(), ehpp.getAH_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(0);// 安静场景
                        lyratoneTestFitting.copyCharArray(0, LyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

                        buff = lyratoneTestFitting.getWdrcData(1);// 噪音场景
                        lyratoneTestFitting.copyCharArray(1, LyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

                        lyratoneTestFitting.setSpkMPO(ehpp.getSCO_MPO_L(), ehpp.getSCO_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getSCO_MPI_L(), ehpp.getSCO_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(2);// SCO 打电话场景
                        // saveUnitData(buff);
                        lyratoneTestFitting.copyCharArray(2, LyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

                        lyratoneTestFitting.setSpkMPO(ehpp.getA2DP_MPO_L(), ehpp.getA2DP_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getA2DP_MPI_L(), ehpp.getA2DP_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(3);// A2DP 听歌场景
                        lyratoneTestFitting.copyCharArray(3, LyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);


                        // 界面展示用 转16进制
                        wdrc_data_txt = lyratoneTestFitting.toHexString(wdrc_data);
                        LogUtils.d(wdrc_data_txt);
                        Intent intent = new Intent(HearingTestActivity.this,HearingTestDoneActivity.class);
                        intent.putExtra("comeFrom",0);
                        intent.putExtra("userAge",userAge);
                        intent.putExtra("userSex",userSex);
                        intent.putExtra("hlData",hlData);
                        intent.putExtra("frequency",eqFrequency);
                        intent.putExtra("WDRCData",wdrc_data);
                        intent.putExtra("eqDbValue",eqDbValue);
                        startActivity(intent);
                        finish();
                        //测试完成，将WNRC数据发送出去
                        //senddata();
                    }else {
                        //左耳测试完毕，展示左耳done，延时1s测右耳
                        leftEarDoneLayout.setVisibility(View.VISIBLE);
                        hearingTestLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void initView() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        userAge = bundle.getInt("userAge");
        userSex = bundle.getBoolean("userSex");
        hearingAnimationImage = findViewById(R.id.hearing_animation);
        hearingAnimation = (AnimationDrawable) hearingAnimationImage.getBackground();
        eqModel = new EQModel();
        btnNestRightEar = findViewById(R.id.btn_next_right_ear);
        btnNestRightEar.setClickable(true);
        hearingTestBack = findViewById(R.id.tv_hearing_test_back);
        hearingTestLayout = findViewById(R.id.hearing_test_layout);
        hearingTestLayout.setClickable(true);
        leftEarDoneLayout = findViewById(R.id.left_ear_done);
        earTypeText = findViewById(R.id.tv_ear_type);
        progressTextView = findViewById(R.id.tv_test_progress);
        progressBar = findViewById(R.id.test_pregressbar);
        btnHearingTestNext = findViewById(R.id.btn_hearing_test_next);
        // 获取权限    Michal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int REQUEST_CODE_PERMISSION_STORAGE = 1;
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            for (String str : permissions) {
                if (ActivityCompat.checkSelfPermission(this, str) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_STORAGE);
                }
            }
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void initData() {
        dBVal = 0;
        // Lyratone SDK init
        lyratoneTestFitting = new LyratoneTestFitting(HearingTestActivity.this);
        // 新加接口：可设置补偿的最大HL值
        lyratoneTestFitting.setTestMaxVal(80);//90
        Map<String, EHPP> ehppMap = getEHPPData();// 目前3种耳机的数据
        ehpp = ehppMap.get("LT");
        lyratoneTestFitting.setHlVal(ehpp.getHL_VAL());
        lyratoneTestFitting.setAudiometryData(gains);
        lyratoneTestFitting.setSpkMPO(ehpp.getAH_MPO_L(), ehpp.getAH_MPO_R());
        lyratoneTestFitting.setMicMPI(ehpp.getAH_MPI_L(), ehpp.getAH_MPI_R());
        testState = 0;
        earTypeText.setText("左耳");
        progressBar.setProgress(testState+1);
        progressTextView.setText(1+"/"+testNumbers);
    }

    private void startTest() {
        hearingAnimation.start(); // 开始动画
        btnHearingTestNext.setClickable(true);
        mHearingTestManuallyListener = new HearingTestmanuallyListener() {
            @Override
            public void ondBChange(int angle) {
                dBVal = angle/4;  //圆总共360度,dbValue最大90,angle/4转换成dbValue
                LogUtils.d("手动调整音量db = "+dBVal);

                lyratoneTestFitting.setTestConfig(eqFrequency[testState%testNumbers], !isLeft, dBVal);
            }

            @Override
            public void onProgressTouchDown() {
                playHandler.removeMessages(CHANGE_VALUE);
                LogUtils.d("移除消息");
            }

            @Override
            public void onProgressTouchUp() {
                LogUtils.d("自动增加");
                sendValueChangeMessage();
            }

            @Override
            public void onViewPauseClicked(boolean pause) {
                LogUtils.d("暂停 isPause = "+pause);
                isPause = pause;
                if (isPause){
                    hearingAnimation.stop();
                    lyratoneTestFitting.stopTestAudio();
                    playHandler.removeMessages(CHANGE_VALUE);
//                    hearingTestProgressView.setEnabled(false);
                }else {
//                    if (mHearingAutoTestListener != null){
//                        mHearingAutoTestListener.onPauseClicked();
//                    }
                    hearingAnimation.start();
                    lyratoneTestFitting.setTestConfig(eqFrequency[testState%testNumbers], !isLeft, dBVal);
//                    hearingTestProgressView.setEnabled(true);
                    sendValueChangeMessage();
                }

            }
        };
        MyCustomView.setHearingTestManuallyListener(mHearingTestManuallyListener);
        if (isLeft){
            testState = 0;
        }else {
            testState = testNumbers;
        }
        dBVal = 5;
        lyratoneTestFitting.setSpkAdjust(ehpp.getSPK_ADJ_L(), ehpp.getSPK_ADJ_R());
        //lyratoneTestFitting.stopTestAudio();
        lyratoneTestFitting.startTestAudio(eqFrequency[testState%testNumbers], !isLeft, dBVal);

        LogUtils.d("开始测试");
        sendValueChangeMessage();

    }

    public static void setHearingAutoTestListener(HearingAutoTestListener hearingAutoTestListener){
        mHearingAutoTestListener = hearingAutoTestListener;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (playHandler != null && lyratoneTestFitting.isPlaying()){
            sendValueChangeMessage();
        }
    }
    private void sendValueChangeMessage(){
        playHandler.removeMessages(CHANGE_VALUE);
        Message message = Message.obtain();
        message.what = CHANGE_VALUE;
        playHandler.sendMessageDelayed(message,dbIntervalTime);
        LogUtils.d("发送音量改变消息");
    }

    @Override
    public void onStop() {
        super.onStop();
        playHandler.removeMessages(CHANGE_VALUE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hearingAnimation.isRunning()){
            hearingAnimation.stop();
        }
        if (lyratoneTestFitting != null){
            lyratoneTestFitting.stopTestAudio();
        }
    }
    /*
    聆通使用的耳机类型
 */
    private Map<String, EHPP> getEHPPData() {
        // 建议采用 JSON保存，转成数据对象。

        Map<String, EHPP> ehppMap = new HashMap<>();
        EHPP lt_ehpp = new EHPP();
        //--------- 此次 变更点--start----------
        lt_ehpp.setAH_MPI_L(new double[]{121.44997359999999, 119.10641380000001, 119.86738009999999, 119.8648795, 119.55306870000001, 123.0452607, 119.8451253, 118.7634723, 115.9053928, 117.1402286, 115.5972943, 0.0, 6400.0, 0.0, 20480.0, 28160.0});
        lt_ehpp.setAH_MPI_R(new double[]{121.44997359999999, 119.10641380000001, 119.86738009999999, 119.8648795, 119.55306870000001, 123.0452607, 119.8451253, 118.7634723, 115.9053928, 117.1402286, 115.5972943, 0.0, 6400.0, 0.0, 20480.0, 28160.0});
        lt_ehpp.setSCO_MPI_L(new double[]{120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0, 0.0, 6400.0, 2560.0, 23040.0, 28160.0});
        lt_ehpp.setSCO_MPI_R(new double[]{120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0, 0.0, 6400.0, 2560.0, 23040.0, 28160.0});
        lt_ehpp.setA2DP_MPI_L(new double[]{104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0, 0.0, 6400.0, 0.0, 20480.0, 28160.0});
        lt_ehpp.setA2DP_MPI_R(new double[]{104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0, 0.0, 6400.0, 0.0, 20480.0, 28160.0});

        lt_ehpp.setAH_MPO_L(new double[]{131.01999999999998, 128.73000000000002, 125.62, 123.84, 125.58, 126.46, 126.51, 124.25, 124.6, 128.76999999999998, 126.02, 104.5});
        lt_ehpp.setAH_MPO_R(new double[]{131.01999999999998, 128.73000000000002, 125.62, 123.84, 125.58, 126.46, 126.51, 124.25, 124.6, 128.76999999999998, 126.02, 104.5});
        lt_ehpp.setSCO_MPO_L(new double[]{120.52, 120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0});
        lt_ehpp.setSCO_MPO_R(new double[]{120.52, 120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0});
        lt_ehpp.setA2DP_MPO_L(new double[]{105.64, 104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0});
        lt_ehpp.setA2DP_MPO_R(new double[]{105.64, 104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0});
        //lt_ehpp.setSPK_ADJ_L(new double[]{10.5, 18.5, 19.8, 13.5});
        //lt_ehpp.setSPK_ADJ_R(new double[]{10.5, 18.5, 19.8, 13.5});

        lt_ehpp.setSPK_ADJ_L(new double[]{2.5, 10.5, 24.5, 18.0});
        lt_ehpp.setSPK_ADJ_R(new double[]{2.5, 10.5, 24.5, 18.0});
        lt_ehpp.setHL_VAL(new double[]{9.5, 5.5, 11.5, 9.5});
        //--------- 此次 变更点--end----------

//        lt_ehpp.setSPK_ADJ_L(new double[]{28.0, 23.6, 20.2, 18.4,9.5, 5.5, 11.5, 9.5, 5.5});
//        lt_ehpp.setSPK_ADJ_R(new double[]{28.0, 23.6, 20.2, 18.4,9.5, 5.5, 11.5, 9.5, 5.5});
//        lt_ehpp.setHL_VAL(new double[]{9.5, 5.5, 11.5, 9.5, 5.5});
//        lt_ehpp.setHL_VAL(new double[]{9.5, 5.5, 11.5, 9.5, 5.5, 11.5, 15.0,11.5, 15.0});
        // lt_ehpp.setSPK_ADJ_L(new double[]{0.0, 0.0, 0.0, 0.0});
        // lt_ehpp.setSPK_ADJ_R(new double[]{0.0, 0.0, 0.0, 0.0});
        //lt_ehpp.setHL_VAL(new double[]{0.0, 0.0, 0.0, 0.0});
        ehppMap.put("LT", lt_ehpp);

        EHPP lx_ehpp = new EHPP();
        lx_ehpp.setAH_MPI_L(new double[]{125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setAH_MPI_R(new double[]{125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setSCO_MPI_L(new double[]{125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setSCO_MPI_R(new double[]{125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setA2DP_MPI_L(new double[]{125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setA2DP_MPI_R(new double[]{125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setAH_MPO_L(new double[]{122.1, 120.0, 116.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4});
        lx_ehpp.setAH_MPO_R(new double[]{122.1, 120.0, 116.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4});
        lx_ehpp.setSCO_MPO_L(new double[]{122.1, 120.0, 116.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4});
        lx_ehpp.setSCO_MPO_R(new double[]{122.1, 120.0, 116.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4});
        lx_ehpp.setA2DP_MPO_L(new double[]{122.1, 120.0, 116.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4});
        lx_ehpp.setA2DP_MPO_R(new double[]{122.1, 120.0, 116.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4});
        lx_ehpp.setSPK_ADJ_L(new double[]{19.0, 14.1, 12.5, 18.6});
        lx_ehpp.setSPK_ADJ_R(new double[]{19.0, 14.1, 12.5, 18.6});
        //fb3_ehpp.setHL_VAL(new double[]{11.7, 9.5, 15.9, 13.4});
        lx_ehpp.setHL_VAL(new double[]{9.5, 5.5, 11.5, 15.0});
        //fb3_ehpp.setSPK_ADJ_L(new double[]{0.0, 0.0, 0.0, 0.0});
        //fb3_ehpp.setSPK_ADJ_R(new double[]{0.0, 0.0, 0.0, 0.0});
        //fb3_ehpp.setHL_VAL(new double[]{0.0, 0.0, 0.0, 0.0});
        ehppMap.put("LX", lx_ehpp);

        EHPP typec_ehpp = new EHPP();
        typec_ehpp.setAH_MPI_L(new double[]{129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1, 0.0, 6400.0,
                1280.0, 20480.0, 28160.0});
        typec_ehpp.setAH_MPI_R(new double[]{129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1, 0.0, 6400.0,
                1280.0, 20480.0, 28160.0});
        typec_ehpp.setSCO_MPI_L(new double[]{129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1, 0.0, 6400.0,
                1280.0, 20480.0, 28160.0});
        typec_ehpp.setSCO_MPI_R(new double[]{129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1, 0.0, 6400.0,
                1280.0, 20480.0, 28160.0});
        typec_ehpp.setA2DP_MPI_L(new double[]{129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1, 0.0, 6400.0,
                1280.0, 20480.0, 28160.0});
        typec_ehpp.setA2DP_MPI_R(new double[]{129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1, 0.0, 6400.0,
                1280.0, 20480.0, 28160.0});
        typec_ehpp.setAH_MPO_L(new double[]{121.80000000000001, 124.5, 126.30000000000001, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8,
                124.1, 118.5, 118.1});
        typec_ehpp.setAH_MPO_R(new double[]{121.80000000000001, 124.5, 126.30000000000001, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8,
                124.1, 118.5, 118.1});
        typec_ehpp.setSCO_MPO_L(new double[]{121.80000000000001, 124.5, 126.30000000000001, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8,
                124.1, 118.5, 118.1});
        typec_ehpp.setSCO_MPO_R(new double[]{121.80000000000001, 124.5, 126.30000000000001, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8,
                124.1, 118.5, 118.1});
        typec_ehpp.setA2DP_MPO_L(new double[]{121.80000000000001, 124.5, 126.30000000000001, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8,
                124.1, 118.5, 118.1});
        typec_ehpp.setA2DP_MPO_R(new double[]{121.80000000000001, 124.5, 126.30000000000001, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8,
                124.1, 118.5, 118.1});
        typec_ehpp.setSPK_ADJ_L(new double[]{28.0, 23.6, 20.2, 18.4,9.5, 5.5, 11.5, 9.5, 5.5});
        typec_ehpp.setSPK_ADJ_R(new double[]{28.0, 23.6, 20.2, 18.4,9.5, 5.5, 11.5, 9.5, 5.5});
        //typec_ehpp.setHL_VAL(new double[]{11.7, 9.5, 15.9, 13.4});
        typec_ehpp.setHL_VAL(new double[]{9.5, 5.5, 11.5, 9.5, 5.5, 11.5, 15.0,11.5, 15.0});
        // typec_ehpp.setSPK_ADJ_L(new double[]{0.0, 0.0, 0.0, 0.0});
        //typec_ehpp.setSPK_ADJ_R(new double[]{0.0, 0.0, 0.0, 0.0});
        //typec_ehpp.setHL_VAL(new double[]{0.0, 0.0, 0.0, 0.0});
        ehppMap.put("Type-C", typec_ehpp);
        return ehppMap;
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
                Intent intent = new Intent(HearingTestActivity.this, UnConnectedActivity.class);
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