package com.goertek.rox2.ui.main.utils.chart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.BarEntry;
import com.goertek.common.utils.ScreenUtils;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2021/7/22
 *
 * @author michal.xu
 */
public class BarChart extends View {

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
    private Context mContext;
    private float barWidth = 20;
    private float barHeight = 1000;
    private Paint barPaint;
    private Paint circlePaint;
    private int barColor = Color.parseColor("#FF364356");

    private float databottom;
    private float dataTop;
    private float dataLeft;
    private float dataRight;
    private float yToPx;
    private int maxY = 192;
    public int getyLeftOffset() {
        return yLeftOffset;
    }

    public void setyLeftOffset(int yLeftOffset) {
        this.yLeftOffset = yLeftOffset;
    }

    public int getyRightOffset() {
        return yRightOffset;
    }

    public void setyRightOffset(int yRightOffset) {
        this.yRightOffset = yRightOffset;
    }

    public int getxBottomOffset() {
        return xBottomOffset;
    }

    public void setxBottomOffset(int xBottomOffset) {
        this.xBottomOffset = xBottomOffset;
    }

    public int getxTopOffset() {
        return xTopOffset;
    }

    public void setxTopOffset(int xTopOffset) {
        this.xTopOffset = xTopOffset;
    }

    public BarChart(Context context) {
        this(context,null);
    }

    public BarChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BarChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setPaint();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //已经由dp转换成px了
        float screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        float screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        left = yLeftOffset;                   //左y轴x坐标
        right = screenWidth - yRightOffset;   //右y轴x坐标
        top = xTopOffset;                     //顶部x轴y坐标
        bottom = screenHeight - xBottomOffset;//底部x轴y坐标
        xSpace = (right-left)/(xLabelCount-1);    //x坐标的间距
        ySpace = (bottom - top)/(yLabelCount-1);  //y坐标的间距
        chartHeight = bottom - top;
        chartWidth = right - left;
        yToPx = chartHeight/maxY;
        LogUtils.d("yLeftOffset="+yLeftOffset+"xTopOffset"+xTopOffset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        LogUtils.d("barchart Draw");
        //画网格和坐标轴
        DrawGridAndLabel(canvas);
//        List<BarEntry> data = new ArrayList<BarEntry>(){};
//        for (int i =0; i<4; i++){
//            BarEntry entry = new BarEntry(i,70f,2f);
//            data.add(entry);
//        }
//        DrawData(canvas,data);
    }

    private void DrawGridAndLabel(Canvas canvas){
        //画y轴
        canvas.drawLine(left,bottom,left,top,yLabelPaint);
        canvas.drawLine(right,bottom,right,top,yLabelPaint);
        //画x轴
        canvas.drawLine(left,bottom,right,bottom,xLabelPaint);
        canvas.drawLine(left,top,right,top,xLabelPaint);
        //画竖线网格
        for (int i=1;i<xLabelCount-1;i++){
            canvas.drawLine(left+i*xSpace,bottom,left+i*xSpace,top,xGridPaint);
        }
        for (int i=1; i<yLabelCount-1; i++){
            canvas.drawLine(left,top+i*ySpace,right,top+i*ySpace,yGridPaint);
        }

    }

    private void setPaint(){
        xLabelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        xLabelPaint.setStrokeWidth(ScreenUtils.dp2px(1));
        xLabelPaint.setColor(xLabelColor);
        yLabelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        yLabelPaint.setStrokeWidth(ScreenUtils.dp2px(1));
        yLabelPaint.setColor(xLabelColor);

        xGridPaint = new Paint();
        PathEffect dashEffect = new DashPathEffect(new float[]{10,10},1);
        xGridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        xGridPaint.setStrokeWidth(ScreenUtils.dp2px(1));
        xGridPaint.setColor(xLabelColor);
        xGridPaint.setPathEffect(dashEffect);

        yGridPaint = new Paint();
        yGridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        yGridPaint.setStrokeWidth(ScreenUtils.dp2px(1));
        yGridPaint.setColor(xLabelColor);
    }
    private void dpToPx(){
        xTopOffset = ScreenUtils.dp2px(xTopOffset);
        xBottomOffset = ScreenUtils.dp2px(xBottomOffset);
        yLeftOffset = ScreenUtils.dp2px(yLeftOffset);
        yRightOffset = ScreenUtils.dp2px(yRightOffset);
    }

    private void DrawData(Canvas canvas, List<BarEntry> data){
        for (int i=0; i<data.size(); i++){
            float x = data.get(i).getX();  //x坐标
            float lowY = data.get(i).getY();
            float highY = (float) data.get(i).getData();
            setDataPosition(x,barWidth,lowY,highY);
            barPaint = new Paint();
            barPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            barPaint.setStrokeWidth(barWidth);
            barPaint.setColor(barColor);
            circlePaint = new Paint();
            circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            circlePaint.setColor(barColor);
            barHeight = databottom-dataTop;
            canvas.drawLine(dataLeft+barWidth/2,dataTop-barWidth/2,dataLeft+barWidth/2,databottom-barWidth/2,barPaint);
            drawLeftUp(canvas,dataLeft,dataTop,dataRight,dataTop-barWidth/2);
            drawBottomCircle(canvas,dataLeft,databottom-barWidth,dataRight,databottom);
        }

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

    private void setDataPosition(float x, float barWidth, float lowY, float highY){
        dataLeft = x*xSpace - barWidth/2+left;
        dataRight = x*xSpace + barWidth/2+left;
        dataTop = chartHeight - lowY*yToPx;
        databottom = chartHeight - highY*yToPx;
    }

    public void setOffest(int yLeftOffset, int xTopOffset, int yRightOffset, int xBottomOffset){
        this.yLeftOffset = yLeftOffset;
        this.xTopOffset = xTopOffset;
        this.yLeftOffset = yLeftOffset;
        this.xBottomOffset = xBottomOffset;
    }

}
