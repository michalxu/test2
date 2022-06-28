package com.goertek.rox2.ui.main.utils.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.BarEntry;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.List;

/**
 * 创建时间：2021/7/22
 *
 * @author michal.xu
 */
public class Bars extends View {

    private float barWidth = 200;
    private float barHeight = 1000;
    private int barColor = Color.parseColor("#FF364356");
    private Paint barPaint;
    private Paint circlePaint;
    public Bars(Context context) {
        this(context,null);
    }

    public Bars(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Bars(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        barPaint = new Paint();
        barPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        barPaint.setStrokeWidth(barWidth);
        barPaint.setColor(barColor);

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint.setColor(barColor);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        barPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(barWidth/2,barWidth/2,barWidth/2,barHeight-barWidth/2,barPaint);
//        drawLeftUp(canvas,0,0,barWidth,barWidth);
//        drawBottomCircle(canvas,0,barHeight-barWidth,barWidth,barHeight);
    }

    /**
     * 左上角的弧度
     * @param canvas
     */
    private void drawLeftUp(Canvas canvas,float left, float top,float right,float bottom) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FF364356"));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        float barRadius = (right-left)/2;
        LogUtils.d("radius="+barRadius+";left="+left+";right="+right+";bottom="+bottom+";top="+top);

        Path path = new Path();
        path.moveTo(left, bottom);
        path.addArc(new RectF(left, top, right, bottom), -180, 180);
        path.close();
        canvas.drawPath(path,paint);
    }
    /**
     * 左上角的弧度
     * @param canvas
     */
    private void drawBottomCircle(Canvas canvas,float left, float top,float right,float bottom) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FF364356"));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        float barRadius = (right-left)/2;
        LogUtils.d("radius="+barRadius+";left="+left+";right="+right+";bottom="+bottom+";top="+top);

        Path path = new Path();
        path.moveTo(left, top);
        path.addArc(new RectF(left, top, right, bottom), 180, -180);
        path.close();
        canvas.drawPath(path,paint);
    }
    public void setData(float barWidth, float barHeight){
        this.barWidth = barWidth;
        this.barHeight = barHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //已经由dp转换成px了
        float screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        float screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        LogUtils.d("screenWidth="+screenWidth+"screenHeight="+screenHeight);
    }
}
