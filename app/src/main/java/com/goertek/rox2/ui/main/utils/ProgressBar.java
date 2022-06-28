package com.goertek.rox2.ui.main.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.goertek.common.utils.ScreenUtils;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.logging.LogRecord;

/**
 * 创建时间：2021/7/9
 *
 * @author michal.xu
 */
public class ProgressBar extends View {

    public int[] SWEEP_GRADIENT_COLORS = new int[]{0,0,0,0,0,0,0,0,0,0};
    private Path mPath = new Path();
    private Paint backGroundPaint;
    private RectF rectF;

    private int width;
    private int height;
    public ProgressBar(Context context) {
        this(context,null);
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        LogUtils.d("getWidth1()"+width+";getHeight1()"+height);
        //油表的位置方框
        LogUtils.d("getWidth()"+getWidth()+";getHeight()"+getHeight());
        rectF =new RectF(0, 0, width, height);
        mPath.reset();
        //在油表路径中增加一个从起始弧度
        mPath.addArc(rectF, 0, 360);
        //计算路径的长度
        PathMeasure pathMeasure = new PathMeasure(mPath, false);
        float length = pathMeasure.getLength();
        float step = length / 72;
        LogUtils.d("length="+length+";step="+step);
        PathEffect effect = new DashPathEffect(new float[]{step,step*2}, 0);
        float radius = width/2;
        SweepGradient mColorShader = new SweepGradient(radius, radius,SWEEP_GRADIENT_COLORS,null);
        backGroundPaint = new Paint();
//        backGroundPaint.setColor(getResources().getColor(R.color.gray));
        backGroundPaint.setStyle(Paint.Style.STROKE);
        backGroundPaint.setStrokeWidth(15);
        backGroundPaint.setPathEffect(effect);
        backGroundPaint.setShader(mColorShader);
    }
    public ProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SWEEP_GRADIENT_COLORS[0]=context.getResources().getColor(R.color.progress_gradient_color1);
        SWEEP_GRADIENT_COLORS[1]=context.getResources().getColor(R.color.progress_gradient_color2);
        SWEEP_GRADIENT_COLORS[2]=context.getResources().getColor(R.color.progress_gradient_color3);
        SWEEP_GRADIENT_COLORS[3]=context.getResources().getColor(R.color.progress_gradient_color4);
        SWEEP_GRADIENT_COLORS[4]=context.getResources().getColor(R.color.progress_gradient_color5);
        SWEEP_GRADIENT_COLORS[5]=context.getResources().getColor(R.color.progress_gradient_color6);
        SWEEP_GRADIENT_COLORS[6]=context.getResources().getColor(R.color.progress_gradient_color7);
        SWEEP_GRADIENT_COLORS[7]=context.getResources().getColor(R.color.progress_gradient_color8);
        SWEEP_GRADIENT_COLORS[8]=context.getResources().getColor(R.color.progress_gradient_color9);
        SWEEP_GRADIENT_COLORS[9]=context.getResources().getColor(R.color.progress_gradient_color10);


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(width/2,height/2,width/2-10,backGroundPaint);
//        canvas.drawArc(rectF,0,360,false,backGroundPaint);
    }
}
