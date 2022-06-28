package com.goertek.rox2.ui.main.pop_dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingTestDoneActivity;

public class EqPopDialog extends Dialog {

    private Button eqDialogDelete;
    private Button eqDialogCancel;
    private ImageView highPrecisionSwitch;
    private boolean isDetectOn;
    private boolean isHighPrecisionOn;
    private Context mContext;
    private String mName;
    private RoxLitePal mLitePal;
    private int mComeFrom;

    public void setSelectedItem(String selectedItem) {
        this.selectedItem = selectedItem;
    }

    private String selectedItem;
    public EqPopDialog(@NonNull Context context, int comeFrom, String name, RoxLitePal litePal) {
        super(context, R.style.bottom_dialog);
        setContentView(R.layout.eq_delete_dialog);
        mContext = context;
        mName = name;
        LogUtils.d("==mName"+mName);
        mLitePal = litePal;
        mComeFrom = comeFrom;
        Window window = getWindow();
        if (window != null) {
            // 设置弹出位置
            window.setGravity(Gravity.CENTER);
            // 宽度全屏
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        eqDialogDelete = findViewById(R.id.dialog_bnt_eq_delete);
        eqDialogCancel = findViewById(R.id.dialog_bnt_eq_cancel);
        setCanceled(true);
        eqDialogDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("eq delete click");
                if (mComeFrom == 1){
                    mLitePal.roxLitePalDelete.deleteEq(mName);
                    Intent intent = new Intent("com.example.deligence.ACTION_START");
                    intent.putExtra("srcActivity","EqPopDialog");
                    LogUtils.d("----mName="+mName);
                    intent.putExtra("deletItemName",mName);
                    mContext.startActivity(intent);
                    dismiss();
                }else if (mComeFrom == 2){
                    LogUtils.d("delete"+"mname="+mName);
                    if (mName.equals(selectedItem)){
                        Toast.makeText(mContext,"The mode is being used，Switch to default mode after deleted.",Toast.LENGTH_SHORT).show();
                    }
                    mLitePal.roxLitePalDelete.deleteHearing(mName);
                    Intent intent = new Intent("com.example.deligence.ACTION_START");
                    intent.putExtra("srcActivity","SelectHearingModeDialog");
                    intent.putExtra("deletItemName",mName);
                    mContext.startActivity(intent);
                    dismiss();
                    HearingTestDoneActivity.instance.finish();
                }

            }
        });
        eqDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("eq cancel click");
                dismiss();
            }
        });
    }

    /**
     * 构造方法
     *
     * @param context Context
     * @return BottomDialog
     */
    public static EqPopDialog with(Context context, int comFrom,  String name, RoxLitePal litePal) {
        return new EqPopDialog(context, comFrom,name,litePal);
    }

    /**
     * 设置是否可以取消
     *
     * @param cancel 是否可以取消
     * @return BottomDialog
     */
    public EqPopDialog setCanceled(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
        setCancelable(cancel);
        return this;
    }




    public interface OnClickListener {
        void heartReateDetectOnClick(boolean isDetectOn);
        void highPrecisionOnClick(boolean isHighPrecisionOn);
    }
}
