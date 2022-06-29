package com.test.lyratone_tf_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.lyratone.hearingaid.audio.LyratoneTestFitting;
import com.lyratone.hearingaid.audio.EHPP;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String LOG_TAG = "Lyratone";

    // 聆通SDK调用对象（测听验配模块）
    LyratoneTestFitting lyratoneTestFitting;

    // 噪音检测的数值获取方式 Handler
    Handler nmHandler;

    // 设置听力图 8个频点的记录值(左耳4个，右耳4个。顺序 500~4000)
    //int[] gains = new int[]{55, 60, 55, 50, 50, 60, 55, 50};

    // 设置听力图 8个频点的记录值(左耳4个，右耳4个。顺序 500~4000)
    //int[] gains = new int[]{30, 30, 30, 30, 30, 30, 30, 30};
    //int[] gains = new int[]{35, 35, 35, 35, 35, 35, 35, 35};
    //int[] gains = new int[]{40, 40, 40, 40, 40, 40, 40, 40};
    //int[] gains = new int[]{45, 45, 45, 45, 45, 45, 45, 45};
    int[] gains = new int[]{50, 50, 50, 50, 50, 50, 50, 50};
    //int[] gains = new int[]{55, 55, 55, 55, 55, 55, 55, 55};
    //int[] gains = new int[]{60, 60, 60, 60, 60, 60, 60, 60};
    //int[] gains = new int[]{65, 65, 65, 65, 65, 65, 65, 65};

    //int[] gains = new int[]{0, 0, 0, 0, 0, 0, 0, 0};

    Button btn_start_test;
    Button btn_vol_reduce;
    Button btn_vol_increase;
    Button btn_stop_test;

    Button btn_noise_meter;
    Button btn_get_wdrc_data;
    Button btn_set_am_data;
    RadioGroup radioGroup;
    RadioButton lt_radio_btn, lx_radio_btn, typec_radio_btn;

    TextView txt_hl_data;
    TextView txt_decibel;
    TextView txt_decibel2;
    EditText txt_spl_offset, txt_db_spl, txt_wdrc_data,txt_ad_l_500,txt_ad_l_1k,txt_ad_l_2k,txt_ad_l_4k,txt_ad_r_500,txt_ad_r_1k,txt_ad_r_2k,txt_ad_r_4k;

    // 测听时用的4个频点
    int[] freq = {500, 1000, 2000, 4000};
    int vFreq =500;
    // 默认的 测听初始dB，可自行修改
    int dBVal = 30;
    // 默认的 环境音检测调整值，可自行修改
    double offset = 0.0;
    // 测听Demo里的处理状态值
    int stat = 0;
    // 获取WDRC数据的处理状态值
    int stat4Data = 0;
    // 测听Demo里的是否左右声道的逻辑值
    boolean isRight = false;
    // 环境音检测Demo里的是否检测的逻辑值
    boolean isMeter = false;

    Map<String, EHPP> ehppMap;
    EHPP ehpp = null;

    // 测听Demo里的 展现用变量
    String tract;
    // txt_wdrc_data 展现用变量
    String wdrc_data_txt = "";
    // txt_spl_offset 展现用变量
    String spl_offset_txt = "";
    // txt_db_spl 展现用变量
    String db_spl_txt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "onCreate: [new Lib] lyratoneTestFitting:" + lyratoneTestFitting);
        // Lyratone SDK init
        lyratoneTestFitting = new LyratoneTestFitting(this);

        // 新加接口：可设置补偿的最大HL值
        lyratoneTestFitting.setTestMaxVal(90);

        Log.d(LOG_TAG, "onCreate: [new Lib] lyratoneTestFitting:" + lyratoneTestFitting);

        ehppMap = getEHPPData();// 目前3种耳机的数据
        ehpp = ehppMap.get("LT");

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

        radioGroup = findViewById(R.id.radioGroup);
        lt_radio_btn = findViewById(R.id.lt_radio_btn);
        lx_radio_btn = findViewById(R.id.lx_radio_btn);
        typec_radio_btn = findViewById(R.id.typec_radio_btn);

        btn_get_wdrc_data = findViewById(R.id.btn_get_wdrc_data);
        btn_start_test = findViewById(R.id.btn_start_test);
        btn_vol_increase = findViewById(R.id.btn_vol_increase);
        btn_vol_reduce = findViewById(R.id.btn_vol_reduce);
        btn_stop_test = findViewById(R.id.btn_stop_test);
        btn_noise_meter = findViewById(R.id.btn_noise_meter);
        btn_set_am_data = findViewById(R.id.btn_set_am_data);
        txt_hl_data = findViewById(R.id.txt_hl_data);
        txt_decibel = findViewById(R.id.txt_decibel);
        txt_decibel2 = findViewById(R.id.txt_decibel2);
        txt_spl_offset = findViewById(R.id.txt_spl_offset);
        txt_db_spl = findViewById(R.id.txt_db_spl);
        txt_wdrc_data = findViewById(R.id.txt_wdrc_data);

        txt_ad_l_500 = findViewById(R.id.txt_ad_l_500);
        txt_ad_l_1k = findViewById(R.id.txt_ad_l_1k);
        txt_ad_l_2k = findViewById(R.id.txt_ad_l_2k);
        txt_ad_l_4k = findViewById(R.id.txt_ad_l_4k);
        txt_ad_r_500 = findViewById(R.id.txt_ad_r_500);
        txt_ad_r_1k = findViewById(R.id.txt_ad_r_1k);
        txt_ad_r_2k = findViewById(R.id.txt_ad_r_2k);
        txt_ad_r_4k = findViewById(R.id.txt_ad_r_4k);

        btn_start_test.setOnClickListener(this);
        btn_vol_increase.setOnClickListener(this);
        btn_vol_reduce.setOnClickListener(this);
        btn_stop_test.setOnClickListener(this);
        btn_get_wdrc_data.setOnClickListener(this);
        btn_noise_meter.setOnClickListener(this);
        btn_set_am_data.setOnClickListener(this);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == lt_radio_btn.getId()) {
                    ehpp = ehppMap.get(lt_radio_btn.getText().toString());
                } else if (checkedId == lx_radio_btn.getId()) {
                    ehpp = ehppMap.get(lx_radio_btn.getText().toString());
                } else if (checkedId == typec_radio_btn.getId()) {
                    ehpp = ehppMap.get(typec_radio_btn.getText().toString());
                }

                lyratoneTestFitting.setHlVal(ehpp.getHL_VAL());
                lyratoneTestFitting.setSpkAdjust(ehpp.getSPK_ADJ_L(), ehpp.getSPK_ADJ_R());
            }
        });

        // 噪音检测的数值获取方式 Handler Msg
        // obj是数值；what判是否为1,1时表示采集音量大于45dB
        nmHandler = new Handler() {

            // 处理环境音线程发送的消息。
            // sdk 200毫秒发一次
            @Override
            public void handleMessage(android.os.Message msg) {
                String dbVal = msg.obj.toString();
                txt_decibel2.setText(dbVal); // 调整后的结果数

                double adj = Double.valueOf(dbVal) - offset;
                txt_decibel.setText(String.valueOf(Math.round(adj)));// 调整前的采集数

                if (msg.what == 1) {
                    Toast.makeText(MainActivity.this, "请在安静的环境下测听", Toast.LENGTH_LONG).show();
                }
            }

        };

        // 噪音检测的数值获取方式 Handler Msg
        lyratoneTestFitting.setNoiseMeterHandler(nmHandler);
        lyratoneTestFitting.setNoiseThreshold(45); // if over this threshold(dB spl) nmHandler's msg.what is 1
        lyratoneTestFitting.setMeterInterval(3); //interval unit sec

        lyratoneTestFitting.setHlVal(ehpp.getHL_VAL());
        gains[0] = Integer.valueOf(txt_ad_l_500.getText().toString());
        gains[1] = Integer.valueOf(txt_ad_l_1k.getText().toString());
        gains[2] = Integer.valueOf(txt_ad_l_2k.getText().toString());
        gains[3] = Integer.valueOf(txt_ad_l_4k.getText().toString());
        gains[4] = Integer.valueOf(txt_ad_r_500.getText().toString());
        gains[5] = Integer.valueOf(txt_ad_r_1k.getText().toString());
        gains[6] = Integer.valueOf(txt_ad_r_2k.getText().toString());
        gains[7] = Integer.valueOf(txt_ad_r_4k.getText().toString());

        lyratoneTestFitting.setAudiometryData(gains);

        byte[] wdrc_data = new byte[lyratoneTestFitting.DATA_LEN * 2 * 3];
        byte[] buff = null;
        lyratoneTestFitting.setSpkMPO(ehpp.getAH_MPO_L(), ehpp.getAH_MPO_R());
        lyratoneTestFitting.setMicMPI(ehpp.getAH_MPI_L(), ehpp.getAH_MPI_R());
        buff = lyratoneTestFitting.getWdrcData(0);// normal
        lyratoneTestFitting.copyCharArray(0, lyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

        lyratoneTestFitting.setSpkMPO(ehpp.getSCO_MPO_L(), ehpp.getSCO_MPO_R());
        lyratoneTestFitting.setMicMPI(ehpp.getSCO_MPI_L(), ehpp.getSCO_MPI_R());
        buff = lyratoneTestFitting.getWdrcData(3);// mobile
        lyratoneTestFitting.copyCharArray(1, lyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

        lyratoneTestFitting.setSpkMPO(ehpp.getA2DP_MPO_L(), ehpp.getA2DP_MPO_R());
        lyratoneTestFitting.setMicMPI(ehpp.getA2DP_MPI_L(), ehpp.getA2DP_MPI_R());
        buff = lyratoneTestFitting.getWdrcData(2);// music
        lyratoneTestFitting.copyCharArray(2, lyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);
    }

    @Override
    protected void onResume() {
        if (lyratoneTestFitting != null) {
            lyratoneTestFitting.stopTestAudio();
            lyratoneTestFitting.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (lyratoneTestFitting != null) {
            lyratoneTestFitting.stopTestAudio();
            lyratoneTestFitting.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (lyratoneTestFitting != null) {
            lyratoneTestFitting.stopTestAudio();
            lyratoneTestFitting.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_set_am_data:
                // 设置听力图 8个频点的记录值(左耳4个，右耳4个。顺序 500~4000)
                lyratoneTestFitting.setHlVal(ehpp.getHL_VAL());
                gains[0] = Integer.valueOf(txt_ad_l_500.getText().toString());
                gains[1] = Integer.valueOf(txt_ad_l_1k.getText().toString());
                gains[2] = Integer.valueOf(txt_ad_l_2k.getText().toString());
                gains[3] = Integer.valueOf(txt_ad_l_4k.getText().toString());
                gains[4] = Integer.valueOf(txt_ad_r_500.getText().toString());
                gains[5] = Integer.valueOf(txt_ad_r_1k.getText().toString());
                gains[6] = Integer.valueOf(txt_ad_r_2k.getText().toString());
                gains[7] = Integer.valueOf(txt_ad_r_4k.getText().toString());

                lyratoneTestFitting.setAudiometryData(gains);

                if (lyratoneTestFitting.isHLOnOff()) {
                    Toast.makeText(MainActivity.this, "建议开启辅听", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "建议不开启辅听", Toast.LENGTH_LONG).show();
                }

                double[] hlData = lyratoneTestFitting.getAudiometryHLData();
                String text = "界面展示用听力数据：L[" + hlData[0] + "," + hlData[1] + "," + hlData[2] + "," + hlData[3] + "] R[" + hlData[4] + "," + hlData[5] + "," + hlData[6] + "," + hlData[7] + "]";
                txt_hl_data.setText(text);
                break;
            case R.id.btn_get_wdrc_data:
                byte[] buff = null;
                switch (stat4Data) {
                    case 0:
                        // 获取WDRC3个场景的双耳数据[1084*2*3]
                        // 前提是需要设置了听力图 8个频点的记录值 lyratoneTestFitting.setAudiometryData
                        // buff = lyratoneTestFitting.getWdrcData();

                        byte[] wdrc_data = new byte[lyratoneTestFitting.DATA_LEN * 2 * 4];

                        lyratoneTestFitting.setSpkMPO(ehpp.getAH_MPO_L(), ehpp.getAH_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getAH_MPI_L(), ehpp.getAH_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(0);// 对话：安静场景
                        saveUnitData(buff);
                        lyratoneTestFitting.copyCharArray(0, lyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

                        buff = lyratoneTestFitting.getWdrcData(1);// 对话：噪音场景
                        lyratoneTestFitting.copyCharArray(1, lyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

                        lyratoneTestFitting.setSpkMPO(ehpp.getSCO_MPO_L(), ehpp.getSCO_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getSCO_MPI_L(), ehpp.getSCO_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(2);// SCO 打电话场景
                        lyratoneTestFitting.copyCharArray(2, lyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

                        lyratoneTestFitting.setSpkMPO(ehpp.getA2DP_MPO_L(), ehpp.getA2DP_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getA2DP_MPI_L(), ehpp.getA2DP_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(3);// A2DP 听书场景
                        lyratoneTestFitting.copyCharArray(3, lyratoneTestFitting.DATA_LEN * 2, buff, wdrc_data);

                        // 界面展示用 转16进制
                        //wdrc_data_txt = lyratoneTestFitting.toHexString(buff);
                        wdrc_data_txt = lyratoneTestFitting.toHexString(wdrc_data);
                        saveData(wdrc_data);
                        txt_wdrc_data.setText(wdrc_data_txt);

                        btn_get_wdrc_data.setText("获取AH场景的WDRC数据");
                        break;
                    case 1:
                        // 获取安静场景的WDRC数据[1084*2]
                        // 前提是需要设置了听力图 8个频点的记录值 lyratoneTestFitting.setAudiometryData
                        lyratoneTestFitting.setSpkMPO(ehpp.getAH_MPO_L(), ehpp.getAH_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getAH_MPI_L(), ehpp.getAH_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(0);// normal
                        // 界面展示用 转16进制
                        wdrc_data_txt = lyratoneTestFitting.toHexString(buff);
                        txt_wdrc_data.setText(wdrc_data_txt);

                        btn_get_wdrc_data.setText("获取SCO场景的WDRC数据");
                        break;
                    case 2:
                        // 获取噪音场景的WDRC数据[1084*2]
                        // 前提是需要设置了听力图 8个频点的记录值 lyratoneTestFitting.setAudiometryData
                        lyratoneTestFitting.setSpkMPO(ehpp.getSCO_MPO_L(), ehpp.getSCO_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getSCO_MPI_L(), ehpp.getSCO_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(3);// mobile
                        // 界面展示用 转16进制
                        wdrc_data_txt = lyratoneTestFitting.toHexString(buff);
                        txt_wdrc_data.setText(wdrc_data_txt);

                        btn_get_wdrc_data.setText("获取A2DP场景的WDRC数据");
                        break;
                    case 3:
                        // 获取打电话通话场景的WDRC数据1084*2]
                        // 前提是需要设置了听力图 8个频点的记录值 lyratoneTestFitting.setAudiometryData
                        lyratoneTestFitting.setSpkMPO(ehpp.getA2DP_MPO_L(), ehpp.getA2DP_MPO_R());
                        lyratoneTestFitting.setMicMPI(ehpp.getA2DP_MPI_L(), ehpp.getA2DP_MPI_R());
                        buff = lyratoneTestFitting.getWdrcData(2);// music
                        // 界面展示用 转16进制
                        wdrc_data_txt = lyratoneTestFitting.toHexString(buff);
                        txt_wdrc_data.setText(wdrc_data_txt);

                        btn_get_wdrc_data.setText("获取耳机侧用的WDRC数据");
                        stat4Data = -1;
                        break;
                }
                stat4Data++;
                break;
            case R.id.btn_start_test:
                if (isRight) {
                    tract = "右耳";
                } else {
                    tract = "左耳";
                }

                // 获取 需要播音的dB值
                db_spl_txt = txt_db_spl.getText().toString();
                try {
                    dBVal = Integer.valueOf(db_spl_txt);
                } catch (Exception e) {

                }

                // 此处是调用示例，根据实际交互设计，自行开发。
                // startTestAudio与setTestConfig的区别在于，start会先实例化，setTestConfig会判断实例是否为null
                if (stat == 0) {
                    lyratoneTestFitting.setSpkAdjust(ehpp.getSPK_ADJ_L(), ehpp.getSPK_ADJ_R());
                    Log.d(LOG_TAG, "startTestAudio: [begin tag] ");
                    //lyratoneTestFitting.stopTestAudio();
                    lyratoneTestFitting.startTestAudio(freq[0], isRight, dBVal);
                    vFreq = freq[0];
                    Log.d(LOG_TAG, "startTestAudio: [end tag] ");
                } else if (stat < 4) {
                    lyratoneTestFitting.setTestConfig(freq[stat], isRight, dBVal);
                    vFreq = freq[stat];
                }

                switch (stat) {
                    case 0:
                        btn_start_test.setText(tract + "500HZ");
                        break;
                    case 1:
                        btn_start_test.setText(tract + "1000HZ");
                        break;
                    case 2:
                        btn_start_test.setText(tract + "2000HZ");
                        break;
                    case 3:
                        btn_start_test.setText(tract + "4000HZ");
                        if (!isRight) {
                            stat = -1;
                            isRight = true;
                        }
                        break;
                    case 4:
                        if (isRight) {
                            stat = -1;
                            isRight = false;
                            btn_start_test.setText("开始测听");
                            lyratoneTestFitting.stopTestAudio();
                        }
                        break;
                }
                stat++;
                break;
            case R.id.btn_vol_increase:
                // 获取 需要播音的dB值
                db_spl_txt = txt_db_spl.getText().toString();
                try {
                    dBVal = Integer.valueOf(db_spl_txt);
                } catch (Exception e) {
                }

                dBVal += 10;
                txt_db_spl.setText(String.valueOf(dBVal));
                lyratoneTestFitting.setTestConfig(vFreq, isRight, dBVal);
                break;
            case R.id.btn_vol_reduce:
                // 获取 需要播音的dB值
                db_spl_txt = txt_db_spl.getText().toString();
                try {
                    dBVal = Integer.valueOf(db_spl_txt);
                } catch (Exception e) {
                }
                dBVal -= 10;
                txt_db_spl.setText(String.valueOf(dBVal));
                lyratoneTestFitting.setTestConfig(vFreq, isRight, dBVal);
                break;
            case R.id.btn_stop_test:
                stat = 0;
                isRight = false;
                btn_start_test.setText("开始测听");
                // 停在播音
                lyratoneTestFitting.stopTestAudio();
                break;
            case R.id.btn_noise_meter:
                // 获取调准值。
                spl_offset_txt = txt_spl_offset.getText().toString();

                try {
                    offset = Double.valueOf(spl_offset_txt);
                } catch (Exception e) {
                }

                lyratoneTestFitting.setNoiseMeterOffset(offset);

                if (!isMeter) {
                    // 开始检测
                    btn_noise_meter.setText("暂停检测");
                    lyratoneTestFitting.startNoiseMeter();
                    isMeter = true;
                } else {
                    // 停止检测
                    btn_noise_meter.setText("环境音检测");
                    lyratoneTestFitting.pauseNoiseMeter();
                    isMeter = false;
                    txt_decibel.setText("当前环境音");
                    txt_decibel2.setText("调整后分贝");
                }
                break;
        }
    }

    private void saveData(byte[] wdrc_data) {
        // 创建String对象保存文件名路径
        try {
            // 创建指定路径的文件
            File file = new File(Environment.getExternalStorageDirectory(), "conf_1084_8.dat");
            // 如果文件不存在
            if (file.exists()) {
                // 创建新的空文件
                file.delete();
            }
            file.createNewFile();
            Log.d(LOG_TAG,"[saveData] file path:"+file.getPath());
            // 获取文件的输出流对象
            FileOutputStream outStream = new FileOutputStream(file);
            // 获取字符串对象的byte数组并写入文件流
            outStream.write(wdrc_data);
            // 最后关闭文件输出流
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveUnitData(byte[] wdrc_data) {
        // 创建String对象保存文件名路径
        try {
            // 创建指定路径的文件
            File file = new File(Environment.getExternalStorageDirectory(), "conf_u.dat");
            // 如果文件不存在
            if (file.exists()) {
                // 创建新的空文件
                file.delete();
            }
            file.createNewFile();
            Log.d(LOG_TAG,"[saveData] file path:"+file.getPath());
            // 获取文件的输出流对象
            FileOutputStream outStream = new FileOutputStream(file);
            // 获取字符串对象的byte数组并写入文件流
            outStream.write(wdrc_data);
            // 最后关闭文件输出流
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, EHPP> getEHPPData() {
        // 建议采用 JSON保存，转成数据对象。

        Map<String, EHPP> ehppMap = new HashMap<>();
        EHPP lt_ehpp = new EHPP();

        lt_ehpp.setAH_MPI_L(new double[]{121.45, 119.10, 119.87, 119.86, 119.55, 123.05, 119.85, 118.76, 115.90, 117.14, 115.60, 0.0, 6400.0, 0.0, 20480.0, 28160.0});
        lt_ehpp.setAH_MPI_R(new double[]{121.45, 119.10, 119.87, 119.86, 119.55, 123.05, 119.85, 118.76, 115.90, 117.14, 115.60, 0.0, 6400.0, 0.0, 20480.0, 28160.0});
        lt_ehpp.setSCO_MPI_L(new double[]{120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0, 0.0, 6400.0, 2560.0, 23040.0, 28160.0});
        lt_ehpp.setSCO_MPI_R(new double[]{120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0, 0.0, 6400.0, 2560.0, 23040.0, 28160.0});
        lt_ehpp.setA2DP_MPI_L(new double[]{104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0, 0.0, 6400.0, 0.0, 20480.0, 28160.0});
        lt_ehpp.setA2DP_MPI_R(new double[]{104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0, 0.0, 6400.0, 0.0, 20480.0, 28160.0});

        lt_ehpp.setAH_MPO_L(new double[]{131.02, 128.73, 125.62, 123.84, 125.58, 126.46, 126.51, 124.25, 124.6, 128.77, 126.02, 104.5});
        lt_ehpp.setAH_MPO_R(new double[]{131.02, 128.73, 125.62, 123.84, 125.58, 126.46, 126.51, 124.25, 124.6, 128.77, 126.02, 104.5});
        lt_ehpp.setSCO_MPO_L(new double[]{120.52, 120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0});
        lt_ehpp.setSCO_MPO_R(new double[]{120.52, 120.23, 120.12, 120.34, 122.08, 122.96, 123.01, 120.75, 121.1, 125.27, 122.52, 101.0});
        lt_ehpp.setA2DP_MPO_L(new double[]{105.64, 104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0});
        lt_ehpp.setA2DP_MPO_R(new double[]{105.64, 104.23, 106.17, 108.32, 110.93, 112.55, 115.2, 113.71, 112.38, 112.92, 108.18, 108.0});

        lt_ehpp.setSPK_ADJ_L(new double[]{2.5, 10.5, 24.5, 18.0});
        lt_ehpp.setSPK_ADJ_R(new double[]{2.5, 10.5, 24.5, 18.0});


        lt_ehpp.setHL_VAL(new double[]{9.5, 5.5, 11.5, 15.0});
        // lt_ehpp.setSPK_ADJ_L(new double[]{0.0, 0.0, 0.0, 0.0});
        // lt_ehpp.setSPK_ADJ_R(new double[]{0.0, 0.0, 0.0, 0.0});
        //lt_ehpp.setHL_VAL(new double[]{0.0, 0.0, 0.0, 0.0});
        ehppMap.put("LT", lt_ehpp);

        EHPP lx_ehpp = new EHPP();
        lx_ehpp.setAH_MPI_L(new double[]{125.0, 125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setAH_MPI_R(new double[]{125.0, 125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setSCO_MPI_L(new double[]{125.0, 125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setSCO_MPI_R(new double[]{125.0, 125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setA2DP_MPI_L(new double[]{125.0, 125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
        lx_ehpp.setA2DP_MPI_R(new double[]{125.0, 125.0, 118.3, 109.6, 110.9, 114.4, 122.2, 123.6, 122.4, 118.1, 118.9, 120.4, 0.0, 6400.0, 5120.0, 23040.0, 30720.0});
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
        typec_ehpp.setAH_MPI_L(new double[]{121.80, 129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setAH_MPI_R(new double[]{121.80, 129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setSCO_MPI_L(new double[]{121.80, 129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setSCO_MPI_R(new double[]{121.80, 129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setA2DP_MPI_L(new double[]{121.80, 129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setA2DP_MPI_R(new double[]{121.80, 129.5, 128.3, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setAH_MPO_L(new double[]{121.80, 124.5, 126.30, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setAH_MPO_R(new double[]{121.80, 124.5, 126.30, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setSCO_MPO_L(new double[]{121.80, 124.5, 126.30, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setSCO_MPO_R(new double[]{121.80, 124.5, 126.30, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setA2DP_MPO_L(new double[]{121.80, 124.5, 126.30, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setA2DP_MPO_R(new double[]{121.80, 124.5, 126.30, 119.6, 119.7, 122.3, 124.6, 123.2, 124.8, 124.1, 118.5, 118.1});
        typec_ehpp.setSPK_ADJ_L(new double[]{28.0, 23.6, 20.2, 18.4});
        typec_ehpp.setSPK_ADJ_R(new double[]{28.0, 23.6, 20.2, 18.4});
        //typec_ehpp.setHL_VAL(new double[]{11.7, 9.5, 15.9, 13.4});
        typec_ehpp.setHL_VAL(new double[]{9.5, 5.5, 11.5, 15.0});
        // typec_ehpp.setSPK_ADJ_L(new double[]{0.0, 0.0, 0.0, 0.0});
        //typec_ehpp.setSPK_ADJ_R(new double[]{0.0, 0.0, 0.0, 0.0});
        //typec_ehpp.setHL_VAL(new double[]{0.0, 0.0, 0.0, 0.0});
        ehppMap.put("Type-C", typec_ehpp);
        return ehppMap;
    }

}
