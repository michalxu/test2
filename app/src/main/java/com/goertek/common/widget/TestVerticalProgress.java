package com.goertek.common.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;

/**
 * 文件名：TestVerticalProgress
 * 描述：竖直进度条
 * 创建时间：2020/8/13
 * @author jochen.zhang
 */
public class TestVerticalProgress extends View {
    private static final String TAG = "TestVerticalProgress";
    /** 画笔 */
    private Paint mPaint;
    /** 最大进度 */
    private int max;
    /** 当前进度 */
    private int progress;
    /** 进度条背景Size */
    private float backgroundSize;
    /** 进度条距离边框距离 */
    private int backgroundMargin;
    /** 背景条渐变色 */
    private final int shaderTopColor;
    private final int shaderCenterColor;
    private LinearGradient mShader;
    /** 高亮进度 */
    private Bitmap mHighLightBitmap;
    /** 普通进度 */
    private Bitmap mNormalBitmap;

    public TestVerticalProgress(Context context) {
        this(context, null);
    }

    public TestVerticalProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestVerticalProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        // 进度点
        mNormalBitmap = getBitmap(R.mipmap.test_normal_dot);
        mHighLightBitmap = getBitmap(R.mipmap.test_high_light_dot);
        // 进度条距离边框距离
        backgroundMargin = mHighLightBitmap.getWidth() / 2;
        // 渐变色
        shaderTopColor = context.getResources().getColor(R.color.color_tvp_progress_top);
        shaderCenterColor = context.getResources().getColor(R.color.color_tvp_progress_center);
        // 进度条背景Size
        backgroundSize = context.getResources().getDimension(R.dimen.vp_background_size);
        // 最大进度
        max = Const.MAX_LEVEL - 1;
        // 进度
        progress = -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景
        mPaint.setStrokeWidth(backgroundSize);
        mPaint.setStrokeCap(Paint.Cap.BUTT);

        int centerX = getWidth() / 2;
        float lineY0 = backgroundMargin;
        float lineY1 = getHeight() - backgroundMargin;

        if (mShader == null) {
            mShader = new LinearGradient(centerX, lineY0, centerX, lineY1, new int[]{shaderTopColor, shaderCenterColor, shaderTopColor}, null, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mShader);
        canvas.drawLine(centerX, lineY0, centerX, lineY1, mPaint);


        if (progress >= 0) {
            // 绘制进度条
            float length = getHeight() - 2 * backgroundMargin;
            Bitmap bitmap = isSelected() ? mHighLightBitmap : mNormalBitmap;
            int offsetX = -bitmap.getWidth() / 2;
            int offsetY = -bitmap.getHeight() / 2;
            float progressY = (((max - progress) / (float) max) * length) + backgroundMargin + offsetY;
            int progressX = centerX + offsetX;

            canvas.drawBitmap(bitmap, progressX, progressY, mPaint);
        }
    }

    /**
     * 获取进度的最大值
     *
     * @return 进度条最大值
     */
    public int getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     *
     * @param max 进度条最大值
     */
    public void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度.需要同步
     *
     * @return 进度
     */
    public int getProgress() {
        return progress;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param progress 进度
     */
    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > max) {
            progress = max;
        }
        this.progress = progress;
        postInvalidate();
    }

    /**
     * 原始分辨率取图像
     *
     * @param resId 资源ID
     * @return BitMap
     */
    public Bitmap getBitmap(int resId) {
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(resId);
        return drawable.getBitmap();
    }

    public Rect calLocation(int level) {
        int[] location = new int[2];
        // 获取自身在屏幕中的位置
        getLocationOnScreen(location);

        // 计算进度在控件中的位置
        int centerX = getWidth() / 2;
        float length = getHeight() - 2 * backgroundMargin;
        int offsetX = -mHighLightBitmap.getWidth() / 2;
        int offsetY = -mHighLightBitmap.getHeight() / 2;
        float progressY = (((max - level) / (float) max) * length) + backgroundMargin + offsetY;
        int progressX = centerX + offsetX;

        // 计算进度在屏幕中的位置
        int left = location[0] + progressX;
        int top = location[1] + (int) progressY;

        return new Rect(left, top, left + mHighLightBitmap.getWidth(), top + mHighLightBitmap.getHeight());
    }
}
