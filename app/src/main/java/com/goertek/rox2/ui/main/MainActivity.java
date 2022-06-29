package com.goertek.rox2.ui.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.BarEntry;
import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.protocol.ProtocolAPI;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.function.NotifyListener;
import com.goertek.bluetooth.protocol.model.ParseResultEvent;
import com.goertek.bluetooth.protocol.tws.entity.TWSBattery;
import com.goertek.bluetooth.protocol.tws.entity.TWSEQParams;
import com.goertek.common.base.BaseActivity;
import com.goertek.common.event.Event;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.common.utils.ScreenUtils;
import com.goertek.common.utils.SharedPreferenceUtils;
import com.goertek.common.utils.Utils;
import com.goertek.db.bean.EQTable;
import com.goertek.db.bean.FrequencyWithdB;
import com.goertek.db.bean.HeartRateTable;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.MyApplication;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;
import com.goertek.rox2.service.BluetoothService;
import com.goertek.rox2.ui.main.adapter.eqAdapter;
import com.goertek.rox2.ui.main.adapter.hearingAdapter;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingEnvironmentTestActivity;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingTestDoneActivity;
import com.goertek.rox2.ui.main.heart_rate.HeartRateActivity;
import com.goertek.rox2.ui.main.pop_dialog.SelectHearingModeDialog;
import com.goertek.rox2.ui.main.utils.timeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends BaseActivity implements NotifyListener {

    private Context mContext;
    private String address = "michalTest";
    //判断各个开关的状态
    private boolean hasTestHeartRate;
    private int ancControlMode = 0;
    private int ancMode=-1;
    private int ambientMode = -2;
    private boolean isCheckEQ = false;  //判断是否是主动查询，如果是，则不给耳机发送eq
    private boolean isCheckAnc = false;
    private SelectHearingModeDialog dialog;

    private RoxLitePal litePal;
    private Bundle bundle;
    private TextView leftBatteryTextView;
    private ProgressBar leftBatteryProgress;
    private TextView rightBatteryTextView;
    private ProgressBar rightBatteryProgress;
    private TextView boxBatteryTextView;
    private ProgressBar boxBatteryProgress;
    //心率检测部分
    private View heartRateView;
    private TextView heartRrateValue;
    private TextView HeartRateTestTime;
    private TextView heartRateUint;
    private TextView heartRateText;
    //Noice Control部分，ANC及其子控件和子布局
    private Button btnANC;
    private LinearLayout ancOnLayout;
    private LinearLayout ancOnAdaptiveMode;
    private ImageView adaptiveImage;
    private TextView adaptiveText1;
    private TextView adaptiveText2;
    private LinearLayout ancOnStableMode;
    private ImageView stableImage;
    private TextView stableText1;
    private TextView stableText2;
    private Button btnAncOff;
    private Button btnAmbient;
    private LinearLayout ambientLayout;
    private LinearLayout ambientPassthroughMode;
    private ImageView passthroughImage;
    private TextView passthroughText1;
    private TextView passthroughText2;
    private LinearLayout ambientVocalBoosterMode;
    private ImageView vocalImage;
    private TextView vocalText1;
    private TextView vocalText2;
    private LinearLayout ambientCustomMode;
    private ImageView customImage;
    private TextView customText1;
    private TextView customText2;
    //Noice Control的图标，需要不断切换
    private Drawable drawableAncOn;
    private Drawable drawableAncOff;
    private Drawable drawableOn;
    private Drawable drawableOff;
    private Drawable drawableAmbientOn;
    private Drawable drawableAmbientOff;

    private ImageView toHearingTestView;
    private ImageView settingImageView;
    private RecyclerView eqRecycleView;
    private final List<String> eqNameItemList = new ArrayList<>();

    private eqAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (bundle==null){
            bundle = intent.getExtras();
            LogUtils.d("get bundle onNewIntent");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // 启动蓝牙状态监控服务
        BluetoothService.bindService(this, mServiceConnection);
        LogUtils.d("MainActivity");
        // 沉浸式体验
        setStatusBarColor(getResources().getColor(R.color.color_main_status_bar), true);
        setNavigationColor(getResources().getColor(R.color.color_main_navigation));

        initView();
        setListener();
        ProtocolAPI.getDefault().registerNotifyListener(this);
        if (bundle==null){
            Intent intent = getIntent();
            bundle = intent.getExtras();
            LogUtils.d("get bundle create");
        }
        //初始化数据
        initData();
        checkAncEq();
        requestPermission();
    }

    private void checkAncEq() {
        LogUtils.d("向耳机查询EQmode，更新EQ mode的UI");
        //向耳机查询EQmode，更新EQ mode的UI
        mAdapter.setClickable(false);
        Utils.getEq(new IRspListener<TWSEQParams>() {
            @Override
            public void onSuccess(TWSEQParams object) {
                LogUtils.d("查询eq成功");
                mAdapter.setClickable(true);
                int[] eqDb = object.getParams();
                StringBuilder eqDbString = new StringBuilder();
                for (int i=0; i<eqDb.length; i++){
                    eqDb[i] = eqDb[i]+ MyApplication.STANDAR_EQ[i];

                }
                LogUtils.d(eqDbString.toString());
                boolean match = false;
                String matchName = "";
                int[] tempDb;
                List<EQTable> eqTableList = litePal.roxLitePalCheck.getEQTableList();
                if (eqTableList.size()>0){
                    for (int i=0; i<eqTableList.size(); i++){
                        String data = eqTableList.get(i).getFrequencyWithdB();
                        LogUtils.d("michal.."+data);
                        List<FrequencyWithdB> frequencyWithdBList = new Gson().fromJson(data,new TypeToken<List<FrequencyWithdB>>(){}.getType());
                        tempDb = new int[frequencyWithdBList.size()];
                        for (int j=0; j<frequencyWithdBList.size();j++){
                            tempDb[j] = frequencyWithdBList.get(j).getdBValue();
                            if (tempDb[j] != eqDb[j]){
                                match = false;
                                LogUtils.d("match false");
                                break;
                            }else {
                                match = true;
                            }
                        }
                        if (match){
                            matchName = eqTableList.get(i).getName();
                            break;
                        }
                    }
                }
                if (match){
                    for (int i=0; i<eqNameItemList.size(); i++){
                        if (eqNameItemList.get(i).equals(matchName)){
                            //更新EQ mode部分的UI
                            isCheckEQ = true;
                            mAdapter.setSelectedUserName(matchName);
                            mLinearLayoutManager.scrollToPosition(eqNameItemList.indexOf(matchName));
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailed(int errorCode) {
                LogUtils.d("查询eq失败");
                Toast.makeText(MainActivity.this,"查询失败",Toast.LENGTH_SHORT).show();
            }
        });

        LogUtils.d("查询anc");
        //向耳机查询NoiceControl，更新Noice Control的UI
        btnANC.setClickable(false);
        btnAmbient.setClickable(false);
        btnAncOff.setClickable(false);
        Utils.getNoiceControlStatus(new IRspListener<byte[]>() {
            @Override
            public void onSuccess(byte[] object) {
                isCheckAnc = true;
                btnANC.setClickable(true);
                btnAmbient.setClickable(true);
                btnAncOff.setClickable(true);
                ancControlMode = object[5];
                if (ancControlMode == 0){
                    ancMode = object[6];
                    btnANC.performClick();
                }else if (ancControlMode == 1){
                    btnAncOff.performClick();
                }else if (ancControlMode == 2){
                    ambientMode = object[6];
                    btnAmbient.performClick();
                }
                LogUtils.d("接收的查询anc结果=="+"ancControlMode = "+object[5]+"ancMode ="+ object[6]);
            }

            @Override
            public void onFailed(int errorCode) {
                Toast.makeText(MainActivity.this,"查询失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        ArrayList<BarEntry> eqData = new ArrayList<>();
        int[] frequency = new int[]{63,125,250,500,1000,2000,4000,8000,12500};
        eqData = new ArrayList<>();
        for (int i=0;i<2*9;i++){
            eqData.add(new BarEntry(50,frequency[i%9],i<9));//第三个参数用来判断是左耳还是右耳
        }
        litePal.roxLitePalAdd.addEQTest("Default mode",1980,true,eqData);
        List<EQTable> eqTableList = litePal.roxLitePalCheck.getEQTableList();
        for (int i=0; i <eqTableList.size(); i++){
            eqNameItemList.add(eqTableList.get(i).getName());
        }
        //创建布局管理器，垂直设置LinearLayoutManager.VERTICAL，水平设置LinearLayoutManager.HORIZONTAL
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //创建适配器，将数据传递给适配器
        mAdapter = new eqAdapter(eqNameItemList, MainActivity.this);
        //设置布局管理器
        eqRecycleView.setLayoutManager(mLinearLayoutManager);
        //设置适配器adapter
        eqRecycleView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bundle==null){
            Intent intent = getIntent();
            bundle = intent.getExtras();
            LogUtils.d("get bundle onResume");

        }
        if (litePal==null){
            litePal = RoxLitePal.getInstance();
        }
        refreshViewState();
    }

    private void initView() {
        mContext = MainActivity.this;
        litePal = RoxLitePal.getInstance();
        eqRecycleView = findViewById(R.id.eq_recycle_view);
        toHearingTestView = findViewById(R.id.iv_hearing_to_test);
        settingImageView = findViewById(R.id.iv_setting);
        heartRateView = findViewById(R.id.iv_to_test_heart_rate);
        heartRrateValue = findViewById(R.id.tv_main_heart_rate_value);
        HeartRateTestTime = findViewById(R.id.tv_main_heart_time);

        //AncOn及其子控件和子布局
        btnANC = findViewById(R.id.btn_anc);
        ancOnLayout = findViewById(R.id.anc_on_layout);
        ancOnAdaptiveMode = (LinearLayout) ancOnLayout.getChildAt(0);
        adaptiveImage = (ImageView) ancOnAdaptiveMode.getChildAt(0);
        adaptiveText1 = (TextView)ancOnAdaptiveMode.getChildAt(1);
        adaptiveText2 = (TextView)ancOnAdaptiveMode.getChildAt(2);
        ancOnStableMode = (LinearLayout) ancOnLayout.getChildAt(2);
        stableImage = (ImageView) ancOnStableMode.getChildAt(0);
        stableText1 = (TextView)ancOnStableMode.getChildAt(1);
        stableText2 = (TextView)ancOnStableMode.getChildAt(2);
        btnAncOff = findViewById(R.id.btn_anc_off);

        //Ambient及其子控件和子布局
        btnAmbient = findViewById(R.id.btn_ambient);
        ambientLayout = findViewById(R.id.anc_ambient_layout);
        ambientPassthroughMode = (LinearLayout) ambientLayout.getChildAt(0);
        passthroughImage = (ImageView) ambientPassthroughMode.getChildAt(0);
        passthroughText1 = (TextView)ambientPassthroughMode.getChildAt(1);
        passthroughText2 = (TextView)ambientPassthroughMode.getChildAt(2);
        ambientVocalBoosterMode = (LinearLayout) ambientLayout.getChildAt(2);
        vocalImage = (ImageView) ambientVocalBoosterMode.getChildAt(0);
        vocalText1 = (TextView)ambientVocalBoosterMode.getChildAt(1);
        vocalText2 = (TextView)ambientVocalBoosterMode.getChildAt(2);
        ambientCustomMode = (LinearLayout) ambientLayout.getChildAt(4);
        customImage = (ImageView) ambientCustomMode.getChildAt(0);
        customText1 = (TextView)ambientCustomMode.getChildAt(1);
        customText2 = (TextView)ambientCustomMode.getChildAt(2);

        //先缓存控件背景
        drawableAncOn = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_anc_on,null);
        drawableAncOn.setBounds(0,0,drawableAncOn.getMinimumWidth(),drawableAncOn.getMinimumHeight());
        drawableAncOff = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_anc_off,null);
        drawableAncOff.setBounds(0,0,drawableAncOff.getMinimumWidth(),drawableAncOff.getMinimumHeight());
        drawableOn = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_on,null);
        drawableOn.setBounds(0,0,drawableOn.getMinimumWidth(),drawableOn.getMinimumHeight());
        drawableOff = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_anc_off,null);
        drawableOff.setBounds(0,0,drawableOff.getMinimumWidth(),drawableOff.getMinimumHeight());
        drawableAmbientOn = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_ambient_on,null);
        drawableAmbientOn.setBounds(0,0,drawableAmbientOn.getMinimumWidth(),drawableAmbientOn.getMinimumHeight());
        drawableAmbientOff = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_ambient_off,null);
        drawableAmbientOff.setBounds(0,0,drawableAmbientOff.getMinimumWidth(),drawableAmbientOff.getMinimumHeight());

        heartRateUint = findViewById(R.id.main_heart_rate_uint);
        heartRateText = findViewById(R.id.main_heart_rate_text);
        //获取引用的左耳电量布局，并获取其子控件
        LinearLayout leftlinearLayout = findViewById(R.id.left_charge);
        LinearLayout leftLinearLayoutChild = (LinearLayout) leftlinearLayout.getChildAt(0);
        leftBatteryProgress = (ProgressBar)leftlinearLayout.getChildAt(1);
        leftBatteryTextView = (TextView) leftLinearLayoutChild.getChildAt(1);
        //获取引用的右耳电量布局，并获取其子控件
        LinearLayout rightlinearLayout = findViewById(R.id.right_charge);
        LinearLayout rightLinearLayoutChild = (LinearLayout) rightlinearLayout.getChildAt(0);
        rightBatteryProgress = (ProgressBar)rightlinearLayout.getChildAt(1);
        rightBatteryTextView= (TextView) rightLinearLayoutChild.getChildAt(1);
        //获取引用的右耳电量布局，并获取其子控件
        LinearLayout boxlinearLayout = findViewById(R.id.box_charge);
        LinearLayout boxlinearLayoutChild = (LinearLayout) boxlinearLayout.getChildAt(0);
        boxBatteryProgress = (ProgressBar)boxlinearLayout.getChildAt(1);
        boxBatteryTextView= (TextView) boxlinearLayoutChild.getChildAt(1);
    }


    private void setListener(){
        hearingAdapter.Listener listener = new hearingAdapter.Listener() {
            @Override
            public void onEditClick(String name, String selecteedItem) {
                //监听到edit click
                Intent intent = new Intent(MainActivity.this, HearingTestDoneActivity.class);
                intent.putExtra("name",name);
                intent.putExtra("selectedItem",selecteedItem);
                intent.putExtra("comeFrom",2);
                startActivityForResult(intent,101);
            }
        };
        hearingAdapter.setListener(listener);
        //心率检测
        heartRateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HeartRateActivity.class);
                startActivity(intent);
            }
        });
        //Anc Adaptive模式开启
        btnANC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d("btnANC");
                btnANC.setCompoundDrawables(null,drawableAncOn,null,null);
                btnANC.setTextColor(getResources().getColor(R.color.text_color_blue));
                ancOnLayout.setVisibility(View.VISIBLE);
                if (!isCheckAnc){
                    byte[] noiceControl = new byte[]{0x00,0x00};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                btnAncOff.setCompoundDrawables(null,drawableOff,null,null);
                                btnAncOff.setTextColor(getResources().getColor(R.color.text_color_black));
                                btnAmbient.setCompoundDrawables(null,drawableAmbientOff,null,null);
                                btnAmbient.setTextColor(getResources().getColor(R.color.text_color_black));
                                ambientLayout.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //点击Anc之后，默认发送ANC adaptive mode指令
                if (ancMode==0){
                    ancOnAdaptiveMode.performClick();
                }else if (ancMode == 1){
                    ancOnStableMode.performClick();
                }
            }
        });
        //ancAdaptiveMode的点击事件
        ancOnAdaptiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isCheckAnc){
                    ancMode = -1;
                    //发送ANC adaptive mode指令
                    byte[] noiceControl = new byte[]{0x00,0x00};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                adaptiveImage.setImageResource(R.drawable.ic_mode_1_on);
                                adaptiveText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                                adaptiveText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                                stableImage.setImageResource(R.drawable.ic_mode_2_off);
                                stableText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                stableText2.setTextColor(getResources().getColor(R.color.text_color_black));
                            }
                      }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    adaptiveImage.setImageResource(R.drawable.ic_mode_1_on);
                    adaptiveText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                    adaptiveText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                    stableImage.setImageResource(R.drawable.ic_mode_2_off);
                    stableText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    stableText2.setTextColor(getResources().getColor(R.color.text_color_black));
                }
                LogUtils.d("ancOnAdaptiveMode.onClick()");
                isCheckAnc = false;
            }
        });
        ancOnStableMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送ANC adaptive mode指令
                ancMode = -1;
                if (!isCheckAnc){
                    byte[] noiceControl = new byte[]{0x00,0x01};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                adaptiveImage.setImageResource(R.drawable.ic_mode_1_off);
                                adaptiveText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                adaptiveText2.setTextColor(getResources().getColor(R.color.text_color_black));
                                stableImage.setImageResource(R.drawable.ic_mode_2_on);
                                stableText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                                stableText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                            }
                        }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    adaptiveImage.setImageResource(R.drawable.ic_mode_1_off);
                    adaptiveText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    adaptiveText2.setTextColor(getResources().getColor(R.color.text_color_black));
                    stableImage.setImageResource(R.drawable.ic_mode_2_on);
                    stableText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                    stableText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                }
                isCheckAnc = false;
            }
        });
        //Anc关闭
        btnAncOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d("btnAncOff");
                if (!isCheckAnc){
                    //发送ANC adaptive mode指令
                    byte[] noiceControl = new byte[]{0x01,0x00};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                btnAncOff.setCompoundDrawables(null,drawableOn,null,null);
                                btnAncOff.setTextColor(getResources().getColor(R.color.text_color_blue));
                                btnANC.setCompoundDrawables(null,drawableAncOff,null,null);
                                btnANC.setTextColor(getResources().getColor(R.color.text_color_black));
                                ancOnLayout.setVisibility(View.GONE);
                                btnAmbient.setCompoundDrawables(null,drawableAmbientOff,null,null);
                                btnAmbient.setTextColor(getResources().getColor(R.color.text_color_black));
                                ambientLayout.setVisibility(View.GONE);
                            }
                      }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    btnAncOff.setCompoundDrawables(null,drawableOn,null,null);
                    btnAncOff.setTextColor(getResources().getColor(R.color.text_color_blue));
                    btnANC.setCompoundDrawables(null,drawableAncOff,null,null);
                    btnANC.setTextColor(getResources().getColor(R.color.text_color_black));
                    ancOnLayout.setVisibility(View.GONE);
                    btnAmbient.setCompoundDrawables(null,drawableAmbientOff,null,null);
                    btnAmbient.setTextColor(getResources().getColor(R.color.text_color_black));
                    ambientLayout.setVisibility(View.GONE);
                }
                isCheckAnc = false;
            }
        });
        //Anc Ambient模式开启
        btnAmbient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d("btnAmbient");
                if (ambientMode ==0){
                    ambientPassthroughMode.performClick();
                }else if (ambientMode == 1){
                    ambientVocalBoosterMode.performClick();
                }else if (ambientMode == 2){
                    ambientCustomMode.performClick();
                }
                if (!isCheckAnc){
                    byte[] noiceControl = null;
                    //点击Ambient，默认发送Passthrough mode指令
                    noiceControl = new byte[]{0x02,0x00};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                btnAmbient.setCompoundDrawables(null,drawableAmbientOn,null,null);
                                btnAmbient.setTextColor(getResources().getColor(R.color.text_color_blue));
                                ambientLayout.setVisibility(View.VISIBLE);
                                btnAncOff.setCompoundDrawables(null,drawableOff,null,null);
                                btnAncOff.setTextColor(getResources().getColor(R.color.text_color_black));
                                btnANC.setCompoundDrawables(null,drawableAncOff,null,null);
                                btnANC.setTextColor(getResources().getColor(R.color.text_color_black));
                                ancOnLayout.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    btnAmbient.setCompoundDrawables(null,drawableAmbientOn,null,null);
                    btnAmbient.setTextColor(getResources().getColor(R.color.text_color_blue));
                    ambientLayout.setVisibility(View.VISIBLE);
                    btnAncOff.setCompoundDrawables(null,drawableOff,null,null);
                    btnAncOff.setTextColor(getResources().getColor(R.color.text_color_black));
                    btnANC.setCompoundDrawables(null,drawableAncOff,null,null);
                    btnANC.setTextColor(getResources().getColor(R.color.text_color_black));
                    ancOnLayout.setVisibility(View.GONE);
                }

            }
        });
        ambientPassthroughMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isCheckAnc){
                    byte[] noiceControl = new byte[]{0x02,0x00};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                passthroughImage.setImageResource(R.drawable.ic_mode_3_on);
                                passthroughText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                                passthroughText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                                vocalImage.setImageResource(R.drawable.ic_mode_4_off);
                                vocalText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                vocalText2.setTextColor(getResources().getColor(R.color.text_color_black));
                                customImage.setImageResource(R.drawable.ic_mode_5_off);
                                customText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                customText2.setTextColor(getResources().getColor(R.color.text_color_black));
                            }
                       }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    ambientMode = -2;
                    passthroughImage.setImageResource(R.drawable.ic_mode_3_on);
                    passthroughText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                    passthroughText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                    vocalImage.setImageResource(R.drawable.ic_mode_4_off);
                    vocalText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    vocalText2.setTextColor(getResources().getColor(R.color.text_color_black));
                    customImage.setImageResource(R.drawable.ic_mode_5_off);
                    customText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    customText2.setTextColor(getResources().getColor(R.color.text_color_black));

                }
                isCheckAnc = false;
           }
        });
        ambientVocalBoosterMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isCheckAnc){
                    byte[] noiceControl = new byte[]{0x02,0x01};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                passthroughImage.setImageResource(R.drawable.ic_mode_3_off);
                                passthroughText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                passthroughText2.setTextColor(getResources().getColor(R.color.text_color_black));
                                vocalImage.setImageResource(R.drawable.ic_mode_4_on);
                                vocalText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                                vocalText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                                customImage.setImageResource(R.drawable.ic_mode_5_off);
                                customText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                customText2.setTextColor(getResources().getColor(R.color.text_color_black));
                            }
                       }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    passthroughImage.setImageResource(R.drawable.ic_mode_3_off);
                    passthroughText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    passthroughText2.setTextColor(getResources().getColor(R.color.text_color_black));
                    vocalImage.setImageResource(R.drawable.ic_mode_4_on);
                    vocalText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                    vocalText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                    customImage.setImageResource(R.drawable.ic_mode_5_off);
                    customText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    customText2.setTextColor(getResources().getColor(R.color.text_color_black));

                }
                isCheckAnc = false;
            }
        });
        ambientCustomMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (litePal.roxLitePalCheck.getHearingTableList().size()>0&&!isCheckAnc){
                    dialog = SelectHearingModeDialog.with(MainActivity.this);
                    dialog.show();
                    byte[] noiceControl = new byte[]{0x02,0x02};
                    Utils.setNoiceControl(noiceControl, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                passthroughImage.setImageResource(R.drawable.ic_mode_3_off);
                                passthroughText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                passthroughText2.setTextColor(getResources().getColor(R.color.text_color_black));
                                vocalImage.setImageResource(R.drawable.ic_mode_4_off);
                                vocalText1.setTextColor(getResources().getColor(R.color.text_color_black));
                                vocalText2.setTextColor(getResources().getColor(R.color.text_color_black));
                                customImage.setImageResource(R.drawable.ic_mode_5_on);
                                customText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                                customText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                            }
                       }

                        @Override
                        public void onFailed(int errorCode) {
                            Toast.makeText(MainActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else if (!isCheckAnc){
                    Toast.makeText(MainActivity.this,"You haven't added personal mode yet. Go to have a hearing test ",Toast.LENGTH_SHORT).show();
                }else {
                    passthroughImage.setImageResource(R.drawable.ic_mode_3_off);
                    passthroughText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    passthroughText2.setTextColor(getResources().getColor(R.color.text_color_black));
                    vocalImage.setImageResource(R.drawable.ic_mode_4_off);
                    vocalText1.setTextColor(getResources().getColor(R.color.text_color_black));
                    vocalText2.setTextColor(getResources().getColor(R.color.text_color_black));
                    customImage.setImageResource(R.drawable.ic_mode_5_on);
                    customText1.setTextColor(getResources().getColor(R.color.text_color_blue));
                    customText2.setTextColor(getResources().getColor(R.color.text_color_blue));
                    isCheckAnc = false;
                }

            }
        });

        //辅听测试
        toHearingTestView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HearingEnvironmentTestActivity.class);
                startActivity(intent);
            }
        });
        //设置界面
        settingImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
    }


    private void refreshViewState() {
        LogUtils.d("refreshViewState");
        //更新电量
        Utils.refreshBattery();


        //查询最新的心率数据，并更新UI
        SharedPreferenceUtils.useMichalSharedPreference("latestHeartRate");
        int latestHeartRate = SharedPreferenceUtils.get("heartRate",-1);
        Long latestTime = SharedPreferenceUtils.get("time",-1L);
        if (latestHeartRate!=-1){
            heartRateView.setBackground(getResources().getDrawable(R.drawable.bg_hr_1));
            setViewHeight(MainActivity.this,heartRateView,200);
            heartRrateValue.setVisibility(View.VISIBLE);
            HeartRateTestTime.setVisibility(View.VISIBLE);
            heartRateUint.setVisibility(View.VISIBLE);
            heartRrateValue.setText(latestHeartRate+"");
            String date = timeUtils.timeStampToDate(latestTime,"");
            String latestMonth = date.substring(5,7);
            String latestDay = date.substring(8,10);
            int latestHour = Integer.parseInt(date.substring(11,13));
            String latestMin = date.substring(14,16);
            if (latestHour>12){
                latestHour = latestHour-12;
                NumberFormat numberFormat = new DecimalFormat("00");
                HeartRateTestTime.setText("Latest: "+numberFormat.format(latestHour)+":"+latestMin+" PM "+latestMonth+"/"+latestDay);
            }else {
                HeartRateTestTime.setText("Latest: "+date.substring(11,13)+":"+latestMin+" AM "+latestMonth+"/"+latestDay);
            }
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            LogUtils.d("year="+year+",month="+month+",day="+day);
            List<HeartRateTable> heartRateTableList = litePal.roxLitePalCheck.getHeartRateTable(year,month,day);
            int minHeartRate = -1;
            int maxHeartRate = -1;
            LogUtils.d("heartRateTableList.size()="+heartRateTableList.size());
            if (heartRateTableList.size()>0){
                int tempMin;
                int tempMax;
                for (int i=0;i<heartRateTableList.size();i++){
                    tempMin = heartRateTableList.get(i).getLowHeartRate();
                    if (minHeartRate == -1){
                        minHeartRate = tempMin;
                    }
                    if (minHeartRate>tempMin){
                        minHeartRate = tempMin;
                    }
                    tempMax = heartRateTableList.get(i).getHighHeartRate();
                    if (maxHeartRate == -1){
                        maxHeartRate = tempMax;
                    }
                    if (maxHeartRate<tempMax){
                        maxHeartRate = tempMax;
                    }
                }
                heartRateText.setVisibility(View.VISIBLE);
                heartRateText.setText("Your heart rate ranged from "+minHeartRate+" to "+maxHeartRate+" today.");
            }else {
                heartRateText.setVisibility(View.INVISIBLE);
            }
        }else {
            heartRateText.setVisibility(View.GONE);
            heartRateView.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.bg_hr_2,null));
            setViewHeight(MainActivity.this,heartRateView,108);
            heartRrateValue.setVisibility(View.GONE);
            HeartRateTestTime.setVisibility(View.GONE);
            heartRateUint.setVisibility(View.GONE);
        }
        if (bundle != null){
            LogUtils.d("bunlde not null");
            String srcActivity = bundle.getString("srcActivity");
            if (srcActivity!=null){
                if (srcActivity.equals("EqPopDialog")){
                    LogUtils.d("==EqPopDialog");
                    String name = bundle.getString("deletItemName");
                    LogUtils.d("name="+name);
                    bundle = null;
                    if (eqNameItemList.contains(name)){
                        int position = eqNameItemList.indexOf(name);
                        mAdapter.deleteItem(position);
                        eqNameItemList.remove(name);
                        if (eqNameItemList.contains("Default mode")){
                            mAdapter.setSelectedUserName("Default mode");
                            mLinearLayoutManager.scrollToPosition(eqNameItemList.indexOf("Default mode"));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }else if (srcActivity.equals("testDoneActivity")){
                    LogUtils.d("==testDoneActivity");
                    String name = bundle.getString("userName");
                    bundle = null;
                    LogUtils.d("name="+name);
                    eqNameItemList.add(name);
                    mAdapter.addItem(name);
                    mAdapter.setSelectedUserName(name);
                    mLinearLayoutManager.scrollToPosition(eqNameItemList.indexOf(name));
                    mAdapter.notifyDataSetChanged();

                    isCheckAnc=true;
                    btnAmbient.performClick();
                    ambientCustomMode.performClick();
                }else if (srcActivity.equals("SelectHearingModeDialog")){
                    LogUtils.d("srcActivity==SelectHearingModeDialog");
                    if (dialog!=null&&litePal.roxLitePalCheck.getHearingTableList().size()>0){
                        dialog.show();
                    }
                }
            }
        }

    }

    private void setBattery(int box, int left, int right) {
        if (box < 0 || box > 100) {
            LogUtils.e("setBattery error box " + box);
            box = -1;
        }
        if (left < 0 || left > 100) {
            LogUtils.e( "setBattery error left " + left);
            left = -1;
        }
        if (right < 0 || right > 100) {
            LogUtils.e( "setBattery error right " + right);
            right = -1;
        }
        int normalColor = getResources().getColor(R.color.color_draw_text_normal);
        int lowColor = getResources().getColor(R.color.color_draw_text_red);
        if (box < 0) {
            boxBatteryTextView.setTextColor(normalColor);
            boxBatteryTextView.setText("");
        } else {
            boxBatteryTextView.setTextColor(box <= Const.LOW_POWER ? lowColor : normalColor);
            boxBatteryTextView.setText(box + "%");
            boxBatteryProgress.setProgress(box);

        }
        if (left < 0) {
            leftBatteryTextView.setTextColor(normalColor);
            leftBatteryTextView.setText("");
        } else {
            leftBatteryTextView.setTextColor(left <= Const.LOW_POWER ? lowColor : normalColor);
            leftBatteryTextView.setText(left + "%");
            leftBatteryProgress.setProgress(left);
        }
        if (right < 0) {
            rightBatteryTextView.setTextColor(normalColor);
            rightBatteryTextView.setText("");
        } else {
            rightBatteryTextView.setTextColor(right <= Const.LOW_POWER ? lowColor : normalColor);
            rightBatteryTextView.setText(right + "%");
            rightBatteryProgress.setProgress(right);
        }
    }

    @Override
    public void onNotify(ParseResultEvent result) {
        //主动上报数据，是心率数据，更新最新的心率数据
        byte[] payload = result.getPayload();
        heartRrateValue.setText(Utils.byteToInt(payload[5])+"");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+1;
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        NumberFormat numberFormat = new DecimalFormat("00");
        if (hour>12){
            hour = hour-12;
            HeartRateTestTime.setText("Latest: "+numberFormat.format(hour)+":"+numberFormat.format(min)+" PM "+numberFormat.format(month)+"/"+numberFormat.format(day));
        }else {
            HeartRateTestTime.setText("Latest: "+numberFormat.format(hour)+":"+numberFormat.format(min)+" AM "+numberFormat.format(month)+"/"+numberFormat.format(day));
        }
    }
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑蓝牙状态监控服务
        ProtocolAPI.getDefault().unregisterNotifyListener(this);
        unbindService(mServiceConnection);
    }

    /**
     * 清空电量
     */
    private void clearBattery() {
        boxBatteryTextView.setText("");
        leftBatteryTextView.setText("");
        rightBatteryTextView.setText("");
    }
    @Override
    public void onEvent(Event event) {
        switch (event.getCode()) {
            case Const.EventCode.ConnectStateCode:
                onConnectState((int) event.getData());
                break;
            case Const.EventCode.BatteryStateCode:
                //接收到电量改变的消息，更新电量UI
                TWSBattery battery = (TWSBattery) event.getData();
                setBattery(battery.getBox(), battery.getLeft(), battery.getRight());
                break;

            default:
                break;
        }
    }

    @Override
    protected boolean isRegisterLocalBroadcast() {
        return true;
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
                Intent intent = new Intent(MainActivity.this,UnConnectedActivity.class);
                startActivity(intent);
                clearBattery();
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

    public static void setViewHeight(Context context, View target, float scale) {
        ViewGroup.LayoutParams layoutParams = target.getLayoutParams();
        layoutParams.height = ScreenUtils.dp2px(scale);
        target.setLayoutParams(layoutParams);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101&& resultCode == RESULT_OK){
            dialog.show();
        }
    }

    private void requestPermission(){
        // 获取权限
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
}