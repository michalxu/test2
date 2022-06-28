package com.goertek.rox2.ui.main.utils.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.BarEntry;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2021/7/22
 *
 * @author michal.xu
 */
public class BarChartLayout extends ViewGroup {
    private final Context mContext;
    float screenWidth;
    float screenHeight;
    private float chartWidth;
    private float chartHeight;
    private float x;
    private float y;
    private float left;
    private float right;
    private float top;
    private float bottom;
    private int yLeftOffset = 0;//y轴距左边的距离
    private int yRightOffset = 0;//y轴距右边的距离
    private int xBottomOffset = 0;//x轴距顶边的距离
    private int xTopOffset = 0;//x轴距底边的距离
    private int xLabelCount = 5;//xlabel的个数，即需要绘制的数据的x的个数
    private int yLabelCount = 4;
    private Paint yLabelPaint = new Paint();
    private Paint xLabelPaint = new Paint();
    private Paint xGridPaint;
    private Paint yGridPaint;
    private int xLabelColor = Color.parseColor("#FFD6D8DA");
    private int xLabelWidth = 1;
    private float xSpace;
    private float ySpace;
    private float barWidth = 20;
    private float barHeight = 1000;
    private Paint barPaint;
    private Paint circlePaint;
    private int barColor = Color.parseColor("#FF364356");

    private float[] databottom = new float[4];
    private float[] dataTop = new float[4];
    private float[] dataLeft = new float[4];
    private float[] dataRight = new float[4];
    private float yToPx;
    private int maxY = 192;
    BarChart barChart;
    List<BarEntry> data;
    public BarChartLayout(Context context) {
        this(context,null);
    }

    public BarChartLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BarChartLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext =context;
        data = new ArrayList<BarEntry>(){};
        for (int i =0; i<4; i++){
            BarEntry entry = new BarEntry(i,70f,2f);
            data.add(entry);
        }
//        setData(data);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        barChart.draw(canvas);
        LogUtils.d("darw");
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        LogUtils.d("on lay");
        View child = getChildAt(0);
        left = yLeftOffset;
        top = xTopOffset;
        right = (int) screenWidth;
        bottom = (int) screenHeight;
        child.layout(left,top,right,bottom);
        int childCount = getChildCount();
        LogUtils.d("childCount="+childCount);
        for (int j=0;j<childCount-1;j++){
            View childBar = getChildAt(j+1);
            LogUtils.d("dataLeft[j]="+dataLeft[j]+"dataTop[j]="+dataTop[j]+"dataRight[j]="+dataRight[j]+"databottom[j]="+databottom[j]);
            childBar.layout((int)dataLeft[j],(int)dataTop[j],(int)dataRight[j],(int)databottom[j]);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //已经由dp转换成px了
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        left = yLeftOffset;                   //左y轴x坐标
        right = screenWidth - yRightOffset;   //右y轴x坐标
        top = xTopOffset;                     //顶部x轴y坐标
        bottom = screenHeight - xBottomOffset;//底部x轴y坐标
        xSpace = (right-left)/(xLabelCount-1);    //x坐标的间距
        ySpace = (bottom - top)/(yLabelCount-1);  //y坐标的间距
        chartHeight = bottom - top;
        chartWidth = right - left;
        yToPx = chartHeight/maxY;
        LogUtils.d("xSpace="+xSpace+"ySpace="+ySpace+"chartHeight="+chartHeight);
        setData(data);
    }

    public void setData(List<BarEntry> data){
        for (int i=0; i<data.size(); i++){
            float x = data.get(i).getX();  //x坐标
            float lowY = data.get(i).getY();
            float highY = (float) data.get(i).getData();
            LogUtils.d("x="+x+"barWidth="+barWidth+"lowY="+lowY+"highY="+highY+"left="+left);
            setDataPosition(i,x,barWidth,lowY,highY);
            barHeight = databottom[i]-dataTop[i];
            setBarSize(barWidth,barHeight);
        }
    }
    private void setDataPosition(int i, float x, float barWidth, float lowY, float highY){
        dataLeft[i] = x*xSpace - barWidth/2+left;
        dataRight[i] = x*xSpace + barWidth/2+left;
        dataTop[i] = chartHeight - lowY*yToPx;
        databottom[i] = chartHeight - highY*yToPx;
    }

    public void setDate(List<BarEntry> data){
        this.data = data;
    }

    public void setOffest(int yLeftOffset, int xTopOffset, int yRightOffset, int xBottomOffset){
        LogUtils.d("setOffest");
        this.yLeftOffset = yLeftOffset;
        this.xTopOffset = xTopOffset;
        this.yRightOffset = yRightOffset;
        this.xBottomOffset = xBottomOffset;
    }

    public void setBarSize(float barWidth, float barHeight){
        this.barHeight = barHeight;
        this.barWidth = barWidth;

    }
}
