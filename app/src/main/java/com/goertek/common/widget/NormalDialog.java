package com.goertek.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.goertek.rox2.R;

/**
 * 文件名：NormalDialog
 * 描述：Dialog
 * 创建时间：2020/9/1
 * @author jochen.zhang
 */
public class NormalDialog extends Dialog {
    private LinearLayout mTitleLinearLayout;
    private TextView mTitleTextView;
    private View mNoTitleView;
    private TextView mTipsTextView;
    private Button mLeftButton;
    private View mButtonCenterView;
    private Button mRightButton;

    public NormalDialog(@NonNull Context context) {
        super(context, R.style.normal_dialog);
        setContentView(R.layout.dialog_normal);
        mTitleLinearLayout = findViewById(R.id.ll_dialog_title);
        mTitleTextView = findViewById(R.id.tv_dialog_title);
        mNoTitleView = findViewById(R.id.v_no_title);
        mTipsTextView = findViewById(R.id.tv_dialog_tips);
        mLeftButton = findViewById(R.id.btn_dialog_left);
        mButtonCenterView = findViewById(R.id.v_dialog_btn_center);
        mRightButton = findViewById(R.id.btn_dialog_right);

        mTitleLinearLayout.setVisibility(View.GONE);
        mButtonCenterView.setVisibility(View.GONE);
        mRightButton.setVisibility(View.GONE);
        mNoTitleView.setVisibility(View.VISIBLE);
        setCanceled(false);
    }

    /**
     * 构造方法
     *
     * @param context Context
     * @return NormalDialog
     */
    public static NormalDialog with(Context context) {
        return new NormalDialog(context);
    }

    /**
     * dismiss的时候将dialog置空
     */
    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * 设置是否可以取消
     *
     * @param cancel 是否可以取消
     */
    public NormalDialog setCanceled(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
        setCancelable(cancel);
        return this;
    }

    /**
     * 设置Title
     *
     * @param title 标题
     * @return NormalDialog
     */
    public NormalDialog setTitle(String title) {
        mTitleTextView.setText(title);
        mTitleLinearLayout.setVisibility(View.VISIBLE);
        mNoTitleView.setVisibility(View.GONE);
        return this;
    }

    /**
     * 设置Content
     *
     * @param content 内容
     * @return NormalDialog
     */
    public NormalDialog setContent(String content) {
        mTipsTextView.setText(content);
        return this;
    }

    /**
     * 设置左Button
     *
     * @param text            按钮文字
     * @param onClickListener 点击事件
     * @return NormalDialog
     */
    public NormalDialog setLeftButton(String text, final View.OnClickListener onClickListener) {
        mLeftButton.setText(text);
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                dismiss();
            }
        });
        return this;
    }

    /**
     * 设置右Button
     *
     * @param text            按钮文字
     * @param onClickListener 点击事件
     * @return NormalDialog
     */
    public NormalDialog setRightButton(String text, final View.OnClickListener onClickListener) {
        mRightButton.setText(text);
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                dismiss();
            }
        });
        mRightButton.setVisibility(View.VISIBLE);
        mButtonCenterView.setVisibility(View.VISIBLE);
        return this;
    }
}
