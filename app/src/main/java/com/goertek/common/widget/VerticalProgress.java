package com.goertek.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.goertek.common.utils.ScreenUtils;
import com.goertek.rox2.R;
import com.goertek.rox2.common.Const;

/**
 * 文件名：VerticalProgress
 * 描述：竖直进度条
 * 创建时间：2020/8/13
 * @author jochen.zhang
 */
public class VerticalProgress extends View {
    private static final String TAG = "EQProgress";
    /** 画笔 */
    private Paint mPaint;
    /** 最大进度 */
    private int max;
    /** 当前进度 */
    private int progress;
    /** 进度条背景Size */
    private float backgroundSize;
    /** 进度条背景Color */
    private int backgroundColor;
    /** 进度条背景Radius */
    private boolean backgroundRadius;
    /** 进度条进度Size */
    private float progressSize;
    /** 进度条进度Color */
    private int progressColor;
    /** 进度条进度Radius */
    private boolean progressRadius;

    final private int backgroundEnableColor;
    final private int backgroundDisableColor;
    final private int progressEnableColor;
    final private int progressDisableColor;

    public VerticalProgress(Context context) {
        this(context, null);
    }

    public VerticalProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        backgroundEnableColor = context.getResources().getColor(R.color.color_vp_background);
        backgroundDisableColor = context.getResources().getColor(R.color.color_vp_disable_background);
        progressEnableColor = context.getResources().getColor(R.color.color_vp_progress);
        progressDisableColor = context.getResources().getColor(R.color.color_vp_disable_progress);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalProgress);
            try {
                // 获取自定义属性和默认值，第一个参数是从用户属性中得到的设置，如果用户没有设置，那么就用默认的属性，即：第二个参数
                // 进度条背景Size
                backgroundSize = typedArray.getDimension(R.styleable.VerticalProgress_backgroundSize, ScreenUtils.dp2px(2));
                // 进度条背景Color
                backgroundColor = typedArray.getColor(R.styleable.VerticalProgress_backgroundColor, Color.GRAY);
                // 进度条背景Radius
                backgroundRadius = typedArray.getBoolean(R.styleable.VerticalProgress_backgroundRadius, true);
                // 进度条进度Size
                progressSize = typedArray.getDimension(R.styleable.VerticalProgress_progressSize, ScreenUtils.dp2px(2));
                // 进度条进度Color
                progressColor = typedArray.getColor(R.styleable.VerticalProgress_progressColor, context.getResources().getColor(R.color.colorPrimary));
                // 进度条进度Radius
                progressRadius = typedArray.getBoolean(R.styleable.VerticalProgress_progressRadius, true);
                // 最大进度
                max = typedArray.getInteger(R.styleable.VerticalProgress_max, 100);
                // 进度
                progress = typedArray.getInteger(R.styleable.VerticalProgress_progress, 0);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                typedArray.recycle();
            }
        } else {
            // 进度条背景Size
            backgroundSize = context.getResources().getDimension(R.dimen.vp_background_size);
            // 进度条背景Color
            backgroundColor = isEnabled() ? backgroundEnableColor : backgroundDisableColor;
            // 进度条背景Radius
            backgroundRadius = true;
            // 进度条进度Size
            progressSize = context.getResources().getDimension(R.dimen.vp_progress_size);
            // 进度条进度Color
            progressColor = isEnabled() ? progressEnableColor : progressDisableColor;
            // 进度条进度Radius
            progressRadius = true;
            // 最大进度
            max = Const.MAX_LEVEL - 1;
            // 进度
            progress = 0;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getWidth() / 2;

        // 绘制背景
        mPaint.setColor(backgroundColor);
        mPaint.setStrokeWidth(backgroundSize);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(backgroundRadius ? Paint.Cap.ROUND : Paint.Cap.SQUARE);

        float backgroundLineCap = backgroundSize / 2;
        canvas.drawLine(centerX, backgroundLineCap, centerX, getHeight() - backgroundLineCap, mPaint);

        if (progress != 0) {
            // 绘制进度条
            mPaint.setColor(progressColor);
            mPaint.setStrokeWidth(progressSize);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeCap(progressRadius ? Paint.Cap.ROUND : Paint.Cap.SQUARE);

            float progressLineCap = progressSize / 2;

            float progressStartY = (((max - progress) / (float) max) * getHeight()) + progressLineCap;
            float progressEndY = getHeight() - progressLineCap;
            if (progressStartY > progressEndY) {
                progressStartY = progressEndY;
            }
            canvas.drawLine(centerX, progressStartY, centerX, progressEndY, mPaint);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        // 进度条背景Color
        backgroundColor = enabled ? backgroundEnableColor : backgroundDisableColor;
        // 进度条进度Color
        progressColor = enabled ? progressEnableColor : progressDisableColor;
        super.setEnabled(enabled);
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
}
