package com.goertek.rox2.ui.main.pop_dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.tws.entity.TWSResult;
import com.goertek.common.utils.Utils;
import com.goertek.db.bean.FrequencyWithdB;
import com.goertek.db.bean.HearingTable;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.adapter.hearingAdapter;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingTestDoneActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2021/7/19
 *
 * @author michal.xu
 */
public class SelectHearingModeDialog extends Dialog {

    private Context mContext;
    private List<View> itemList = new ArrayList<>();
    private List<TextView> selectItemList = new ArrayList<>();
    private List<TextView> selectNameList = new ArrayList<>();
    private List<HearingTable>  hearingTableList;
    private float downXPisition = 0f;
    private float upXPisition = 0f;
    private float downYPosition = 0f;
    private int selectPosition = 0;
    private RoxLitePal litePal;

    private String selectItem;
    public SelectHearingModeDialog(@NonNull Context context) {
        super(context, R.style.bottom_dialog);
        setContentView(R.layout.select_hearing_mode);
        LogUtils.d("oncreste");
        litePal = RoxLitePal.getInstance();
        mContext = context;
        Window window = getWindow();
        window.setWindowAnimations(R.style.bottom_dialog_out);
        if (window != null) {
            // 设置弹出位置
            window.setGravity(Gravity.BOTTOM);
            // 宽度全屏
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
//        initView();
//        setListener();
        setCanceled(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d("onStart");
        hearingTableList= litePal.roxLitePalCheck.getHearingTableList();
        List<String> heaingNameList = new ArrayList<>();
        for (int i=0; i <hearingTableList.size(); i++){
            heaingNameList.add(hearingTableList.get(i).getName());
        }
        LogUtils.d("heaingNameList.size="+heaingNameList.size());
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL,false);
        hearingAdapter adapter = new hearingAdapter(heaingNameList,mContext,this);
        RecyclerView recyclerView = findViewById(R.id.heaing_item_recycle_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }


    private void setListener() {
        for (int i=0; i<itemList.size(); i++){
            selectPosition = i;
            LinearLayout layout = (LinearLayout) itemList.get(i);
            TextView selected = (TextView) layout.getChildAt(2);
            selectItemList.add(selected);
            TextView edit = (TextView) layout.getChildAt(3);
            selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtils.d("select");
                    selected.setText("selected");
                    selected.setBackground(mContext.getResources().getDrawable(R.drawable.michal_bg_gray));
                    for (int j=0; j<selectItemList.size(); j++){
                        TextView select = selectItemList.get(j);
                        if (selected!=select){
                            LogUtils.d("其他设为select， j= "+j);
                            select.setText("select");
                            select.setBackground(mContext.getResources().getDrawable(R.drawable.michal_bg_blue));
                        }
                    }
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, HearingTestDoneActivity.class);
                    intent.putExtra("name",selectNameList.get(selectPosition).getText());
                    intent.putExtra("comeFrom",2);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    private void initView() {
        if (itemList.size()>0) {
            for (int i=0; i<itemList.size(); i++){
                LinearLayout layout = (LinearLayout) itemList.get(i);
                TextView name = (TextView) layout.getChildAt(0);
                TextView select = (TextView) layout.getChildAt(2);
                name.setText(hearingTableList.get(i).getName());
                selectNameList.add(name);
                select.setText("selecte");
                select.setBackground(mContext.getResources().getDrawable(R.drawable.michal_bg_blue));
            }
        }
    }

    /**
     * 构造方法
     *
     * @param context Context
     * @return BottomDialog
     */
    public static SelectHearingModeDialog with(Context context) {
        return new SelectHearingModeDialog(context);
    }

    /**
     * 设置是否可以取消
     *
     * @param cancel 是否可以取消
     * @return BottomDialog
     */
    public SelectHearingModeDialog setCanceled(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
        setCancelable(cancel);
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downXPisition = event.getX();
                upXPisition = downXPisition;
                downYPosition = event.getY();
                LogUtils.d("downYPosition =="+downYPosition+"downXPisition="+downXPisition);
                if (downYPosition<0){
//                    sendEq();
                    this.dismiss();
                }
                break;
            case MotionEvent.ACTION_UP:
                upXPisition = event.getX();
                //断定是用户滑动屏幕，切换
                break;
            case MotionEvent.ACTION_MOVE:
                float tempXposition = event.getX();
                float tempYposition = event.getY();
                if (downXPisition>400&&downXPisition<700&&downYPosition<300){
                    if (tempYposition-downYPosition>50){
//                        sendEq();
                        this.dismiss();
                    }
                }
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d("onStop");
    }

    private void sendEq(){
        List<HearingTable> hearingTableList = litePal.roxLitePalCheck.getHearingTableByName(selectItem);
        if (hearingTableList.size()==1){
            String data = hearingTableList.get(0).getFrequencyWithdB();
            List<FrequencyWithdB> frequencyWithdBList = new Gson().fromJson(data,new TypeToken<List<FrequencyWithdB>>(){}.getType());
            int[] tempDb = new int[frequencyWithdBList.size()];
            for (int j=0; j<frequencyWithdBList.size();j++){
                tempDb[j] = frequencyWithdBList.get(j).getdBValue();
            }
            Utils.setEq(tempDb, new IRspListener<byte[]>() {
                @Override
                public void onSuccess(byte[] object) {

                }

                @Override
                public void onFailed(int errorCode) {

                }
            });
        }
    }
    public String getSelectItem() {
        return selectItem;
    }

    public void setSelectItem(String selectItem) {
        this.selectItem = selectItem;
    }

}
