package com.goertek.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.goertek.rox2.R;

public class BottomDialog extends Dialog {
    private static final String TAG = "BottomDialog";

    private EditText mEditText;
    private Button mButtonSend;
    private Button mButtonSendSave;
    private Context mContext;
    public TextView nameExist;

    public BottomDialog(@NonNull Context context) {
        super(context, R.style.bottom_dialog);
        setContentView(R.layout.dialog_bottom);
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
        }

        mEditText = findViewById(R.id.et_dialog_bottom);
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();
        nameExist = findViewById(R.id.name_exists);
        mButtonSend = findViewById(R.id.btn_dialog_bottom_send);
        mButtonSendSave = findViewById(R.id.btn_dialog_bottom_send_and_save);
        mButtonSend.setEnabled(false);
        mButtonSendSave.setEnabled(false);
        mButtonSendSave.setBackground(context.getResources().getDrawable(R.drawable.michal_btn_save_bg_gray));
        mButtonSend.setBackground(context.getResources().getDrawable(R.drawable.michal_btn_save_bg_gray));
        setCanceled(true);
    }

    /**
     * 构造方法
     *
     * @param context Context
     * @return BottomDialog
     */
    public static BottomDialog with(Context context) {
        return new BottomDialog(context);
    }

    /**
     * 设置是否可以取消
     *
     * @param cancel 是否可以取消
     * @return BottomDialog
     */
    public BottomDialog setCanceled(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
        setCancelable(cancel);
        return this;
    }

    /**
     * 设置EditText
     *
     * @param placeHolder 占位字符串
     * @return BottomDialog
     */
    public BottomDialog setEditText(String placeHolder) {
        mEditText.setHint(placeHolder);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mButtonSend.setEnabled(s.length() != 0);
                mButtonSendSave.setEnabled(s.length() != 0);
                mButtonSendSave.setBackground(mContext.getResources().getDrawable(R.drawable.michal_save_bg_blue));
                mButtonSend.setBackground(mContext.getResources().getDrawable(R.drawable.michal_save_bg_blue));
            }
        });
        return this;
    }

    /**
     * 设置Button
     *
     * @param onSaveClickListener 点击事件
     * @return BottomDialog
     */
    public BottomDialog setButton( final OnSaveClickListener onSaveClickListener) {
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonSend.setBackground(mContext.getResources().getDrawable(R.drawable.michal_bg_btn_next_gray));
                if (onSaveClickListener != null) {
                    onSaveClickListener.onSaveClick(mButtonSend, mEditText);
                }
            }
        });
        mButtonSendSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonSendSave.setBackground(mContext.getResources().getDrawable(R.drawable.michal_bg_btn_next_gray));
                if (onSaveClickListener != null) {
                    onSaveClickListener.onSaveSendClick(mButtonSendSave, mEditText);
                }
            }
        });
        return this;
    }

    public interface OnSaveClickListener {
        void onSaveClick(Button btn, EditText editText);
        void onSaveSendClick(Button btn, EditText editText);
    }
}
