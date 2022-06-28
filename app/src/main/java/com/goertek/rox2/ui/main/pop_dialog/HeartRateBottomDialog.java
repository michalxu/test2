package com.goertek.rox2.ui.main.pop_dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.common.utils.Utils;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;

public class HeartRateBottomDialog extends Dialog {
    private static final String TAG = "BottomDialog";

    private ImageView heartRateDetectSwitch;
    private ImageView highPrecisionSwitch;

    public void setDetectOn(boolean detectOn) {
        isDetectOn = detectOn;
        initView();
    }

    public void setHighPrecisionOn(boolean highPrecisionOn) {
        isHighPrecisionOn = highPrecisionOn;
        initView();

    }

    private boolean isDetectOn = true;
    private boolean isHighPrecisionOn = true;
    private Context mContext;
    private float downXPisition = 0f;
    private float upXPisition = 0f;
    private float downYPosition = 0f;
    public HeartRateBottomDialog(@NonNull Context context) {
        super(context, R.style.bottom_dialog);
        setContentView(R.layout.heart_rate_dialog_bottom);
        mContext = context;
        Window window = getWindow();
        if (window != null) {
            // 设置弹出位置
            window.setGravity(Gravity.BOTTOM);
            // 宽度全屏
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            window.setWindowAnimations(R.style.bottom_dialog_out);
        }

        heartRateDetectSwitch = findViewById(R.id.heart_rate_detect_switch);
        highPrecisionSwitch = findViewById(R.id.high_precision_switch);
        initView();
        setCanceled(true);
    }

    /**
     * 构造方法
     *
     * @param context Context
     * @return BottomDialog
     */
    public static HeartRateBottomDialog with(Context context) {
        return new HeartRateBottomDialog(context);
    }

    /**
     * 设置是否可以取消
     *
     * @param cancel 是否可以取消
     * @return BottomDialog
     */
    public HeartRateBottomDialog setCanceled(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
        setCancelable(cancel);
        return this;
    }

    private void initView(){
        if (isDetectOn){
            LogUtils.d("isDetectOn="+isDetectOn);
            Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.switch_on,null);
            heartRateDetectSwitch.setBackground(drawable);
        }else {
            LogUtils.d("isDetectOn="+isDetectOn);
            Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.switch_off,null);
            heartRateDetectSwitch.setBackground(drawable);
        }
        if (isHighPrecisionOn){
            LogUtils.d("isHighPrecisionOn="+isHighPrecisionOn);
            Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.switch_on,null);
            highPrecisionSwitch.setBackground(drawable);
        }else {
            LogUtils.d("isHighPrecisionOn="+isHighPrecisionOn);
            Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.switch_off,null);
            highPrecisionSwitch.setBackground(drawable);
        }
    }

    /**
     * 设置Button
     *
     * @param onClickListener 点击事件
     * @return BottomDialog
     */
    public HeartRateBottomDialog setButton( final OnClickListener onClickListener) {

        heartRateDetectSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDetectOn = !isDetectOn;
                if (onClickListener != null) {
                    Utils.setHeartRateDetectAuto(isDetectOn, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                if (isDetectOn){
                                    heartRateDetectSwitch.setBackground(mContext.getResources().getDrawable(R.drawable.switch_on));
                                }else {
                                    heartRateDetectSwitch.setBackground(mContext.getResources().getDrawable(R.drawable.switch_off));
                                }
                            }
                        }

                        @Override
                        public void onFailed(int errorCode) {
                            isDetectOn = !isDetectOn;
                        }
                    });
//                    onClickListener.heartReateDetectOnClick(isHighPrecisionOn);
                }
            }
        });
        highPrecisionSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHighPrecisionOn = !isHighPrecisionOn;
                if (onClickListener != null) {
                    Utils.setHeartRateDetectAccurately(isHighPrecisionOn, new IRspListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] object) {
                            if (object[4]==0x00){
                                if (isHighPrecisionOn){
                                    highPrecisionSwitch.setBackground(mContext.getResources().getDrawable(R.drawable.switch_on));
                                }else {
                                    highPrecisionSwitch.setBackground(mContext.getResources().getDrawable(R.drawable.switch_off));
                                }
                            }
                        }

                        @Override
                        public void onFailed(int errorCode) {
                            isHighPrecisionOn = !isHighPrecisionOn;
                        }
                    });
//                    onClickListener.highPrecisionOnClick(isHighPrecisionOn);
                }
            }
        });
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downXPisition = event.getX();
                upXPisition = downXPisition;
                downYPosition = event.getY();
                if (downYPosition<0){
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
                if (downXPisition>400&&downXPisition<700&&downYPosition<100){
                    if (tempYposition-downYPosition>50){
                        this.dismiss();
                    }
                }
            default:
                break;
        }
        return false;
    }

    public interface OnClickListener {
        void heartReateDetectOnClick(boolean isDetectOn);
        void highPrecisionOnClick(boolean isHighPrecisionOn);
    }
}
