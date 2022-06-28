package com.goertek.rox2.ui.main.hearing_test.listener_and_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.utils.MPPointD;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.List;

/**
 * 创建时间：2021/7/1
 *
 * @author michal.xu
 */
public class MyLineChart extends LineChart {

    private Paint paint1 = new Paint();
    private Paint paint2 = new Paint();
    private Paint paint3 = new Paint();
    private Paint paint4 = new Paint();
    private Paint GridPaint = new Paint();
    public boolean enableDrawGridLine = false;
    List<MPPointD> pointList1;
    List<MPPointD> pointList2;
    private float xOffest;
    private float yOffest;
    public float gridWidth;
    public float gridHeight;
    public MyLineChart(Context context) {
        this(context,null);
    }

    public MyLineChart(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint1.setStyle(Paint.Style.FILL);
        paint1.setColor(getResources().getColor(R.color.michal_color2));
        paint1.setAlpha(100);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(getResources().getColor(R.color.michal_color3));
        paint2.setAlpha(100);
        paint3.setStyle(Paint.Style.FILL);
        paint3.setColor(getResources().getColor(R.color.michal_color4));
        paint3.setAlpha(100);
        paint4.setStyle(Paint.Style.FILL);
        paint4.setColor(getResources().getColor(R.color.michal_color5));
        paint4.setAlpha(100);
        mGridBackgroundPaint.setStyle(Paint.Style.FILL);
        mGridBackgroundPaint.setColor(getResources().getColor(R.color.michal_color1));
        mGridBackgroundPaint.setAlpha(100);
//        mGridBackgroundPaint.setStyle(Paint.Style.FILL);
//        // mGridBackgroundPaint.setColor(Color.WHITE);
//        mGridBackgroundPaint.setColor(Color.rgb(240, 240, 240)); // light

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    protected void drawGridBackground(Canvas c) {
//        super.drawGridBackground(c);
        float left = mViewPortHandler.getContentRect().left;
        float right = mViewPortHandler.getContentRect().right;
        float top = mViewPortHandler.getContentRect().top;
        float bottom = mViewPortHandler.getContentRect().bottom;
//        mViewPortHandler.getContentRect().left
        LogUtils.d("left = "+left+",right="+right+",top="+top+",bottom="+bottom);
        LogUtils.d("left1 = "+mViewPortHandler.getContentRect().left+",right1="+mViewPortHandler.getContentRect().right+",top1="+mViewPortHandler.getContentRect().top+",bottom1="+mViewPortHandler.getContentRect().bottom);
        gridHeight = (mViewPortHandler.getContentRect().bottom-mViewPortHandler.getContentRect().top)/5;
        gridWidth = (mViewPortHandler.getContentRect().right-mViewPortHandler.getContentRect().left)/6;
        LogUtils.d("getTop() = "+getTop()+";height="+gridHeight+";width = "+getWidth()+";height=="+getHeight());
        c.drawRect(mViewPortHandler.offsetLeft(),top, mViewPortHandler.contentRight(),top+gridHeight,mGridBackgroundPaint);
        c.drawRect(mViewPortHandler.offsetLeft(),top+gridHeight, mViewPortHandler.contentRight(),top+2*gridHeight,paint1);
        c.drawRect(mViewPortHandler.offsetLeft(),top+2*gridHeight, mViewPortHandler.contentRight(),top+3*gridHeight,paint2);
        c.drawRect(mViewPortHandler.offsetLeft(),top+3*gridHeight, mViewPortHandler.contentRight(),top+4*gridHeight,paint3);
        c.drawRect(mViewPortHandler.offsetLeft(),top+4*gridHeight, mViewPortHandler.contentRight(),top+5*gridHeight,paint4);
        if (enableDrawGridLine){
            drawGridLine(c,pointList1,pointList2);
        }
    }
    public void drawGridLine(Canvas canvas, List<MPPointD> pointList1, List<MPPointD> pointList2){
        LogUtils.d("画网格");
        for (int i=0;i<pointList1.size();i++){
            float x1 = (float) pointList1.get(i).x*gridWidth+mViewPortHandler.getContentRect().left;
            float x2 = (float)pointList2.get(i).x*gridWidth+mViewPortHandler.getContentRect().left;
            float y1 = (float)(100-pointList1.get(i).y)*gridHeight/20+mViewPortHandler.getContentRect().top;
            float y2 = (float)(100-pointList2.get(i).y)*gridHeight/20+mViewPortHandler.getContentRect().top;
            LogUtils.d("x1 = "+x1+";y1="+y1+";x2="+x2+";y2="+y2);
            canvas.drawLine(x1,y1,x2,y2,GridPaint);
        }
    }
    public void setPoints(List<MPPointD> pointList1, List<MPPointD> pointList2,float xOffset,float yOffset){
        this.pointList1 = pointList1;
        this.pointList2 = pointList2;
        this.xOffest = xOffset;
        this.yOffest = yOffset;
        LogUtils.d("xffest = "+xOffset+"yoffset="+yOffset);

    }
    public void setGridPaint(int color){
        GridPaint.setColor(color);
        GridPaint.setStyle(Paint.Style.STROKE);
//        GridPaint.setStrokeWidth(20);
    }

}
