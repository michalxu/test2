package com.goertek.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class FullVideoView extends VideoView {
    public FullVideoView(Context context) {
        super(context);
    }

    public FullVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //得到默认的大小（0，宽度测量规范）
        int width = getDefaultSize(0, widthMeasureSpec);
        //得到默认的大小（0，高度度测量规范）
        int height = getDefaultSize(0, heightMeasureSpec);
        //设置测量尺寸,将高和宽放进去
        setMeasuredDimension(width, height);
    }
}
