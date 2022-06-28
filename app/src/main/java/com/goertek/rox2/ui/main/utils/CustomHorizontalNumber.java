package com.goertek.rox2.ui.main.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;


import com.goertek.common.utils.ScreenUtils;
import com.goertek.rox2.ui.main.LogUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2021/7/13
 *
 * @author michal.xu
 */
public class CustomHorizontalNumber extends View {
    float mTouchX;
    private float lastXposition;
    private int screenWidth;
    private int screenHight;
    private int lineNumber = 25;
    private float lineSpace = 20f;
    private int longLineLength = 64;
    private int shortLineLength = 36;
//    private float lineXPosition[] = new float[25];
    private List lineXPosition = new ArrayList<>();
    private int textPosition[] = new int[2];
    private Paint paint;
    private Paint selectedLinePaint;
    private Paint textPaint;
    private int textSize = 30;
    private int textWidth;
    private int textHight;
    private int text = 2020;  //年龄
    private float lineMarginLeft = 23.5f;
    private float longLineMarginTop = 12f;
    private float shortLineMarginTop = 26f;
    private int textMargin = 20;
    private float longLineBottomPosition;
    private float longLineTopPosition;
    private float shortLineBottomPosition;
    private float shortLineTopPosition;
    private float moveLength;
    private float moveTempLength;
    private float lineStartPosition;
    private float lineEndPosition;
    private float lineTempPosition;

    public CustomHorizontalNumber(Context context) {
        this(context,null);
    }

    public CustomHorizontalNumber(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomHorizontalNumber(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        longLineLength = ScreenUtils.dp2px(longLineLength);
        shortLineLength = ScreenUtils.dp2px(shortLineLength);
        textSize =  ScreenUtils.dp2px(textSize);
        shortLineMarginTop = ScreenUtils.dp2px(shortLineMarginTop);
        longLineMarginTop = ScreenUtils.dp2px(longLineMarginTop);
        lineMarginLeft = ScreenUtils.dp2px(lineMarginLeft);
        textMargin = ScreenUtils.dp2px(textMargin);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(ScreenUtils.dp2px(1));
        paint.setColor(Color.parseColor("#1A091931"));

        selectedLinePaint = new Paint();
        selectedLinePaint.setStrokeWidth(ScreenUtils.dp2px(1));
        selectedLinePaint.setColor(Color.parseColor("#2378F1"));
        selectedLinePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#2378F1"));
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);

    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(text+"",textPosition[0],textPosition[1],textPaint);
        LogUtils.d("lineXPosition.size="+lineXPosition.size());
        for (int i=0; i<lineXPosition.size(); i++){
            int number = text - (lineNumber+1)/2 + i;
            if (number ==text){
                if (number%5!=0){
                    //不是5的倍数，画短线，蓝色
                    canvas.drawLine((float)lineXPosition.get(i),shortLineTopPosition,(float)lineXPosition.get(i),shortLineBottomPosition,selectedLinePaint);
                }else {
                    canvas.drawLine((float)lineXPosition.get(i),longLineTopPosition,(float)lineXPosition.get(i),longLineBottomPosition,selectedLinePaint);
                }
            }else {
                if (number%5!=0){
                    canvas.drawLine((float)lineXPosition.get(i),shortLineTopPosition,(float)lineXPosition.get(i),shortLineBottomPosition,paint);
                }else {
                    canvas.drawLine((float)lineXPosition.get(i),longLineTopPosition,(float)lineXPosition.get(i),longLineBottomPosition,paint);
                }
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHight = MeasureSpec.getSize(heightMeasureSpec);
        setText();
        lineSpace = (screenWidth - 2*lineMarginLeft)/lineNumber; //求每根线之间的间隔
        if (lineXPosition.size()==0){
            for (int i=0; i<lineNumber; i++){
                lineXPosition.add(lineSpace*i+lineMarginLeft);
//            lineXPosition[i] = lineSpace*i+lineMarginLeft;
            }
            lastXposition = (float) lineXPosition.get(lineXPosition.size()-1);
            LogUtils.d("lineXPosition.get(0)="+lineXPosition.get(0));
            LogUtils.d("lineXPosition.get(24)="+lineXPosition.get(lineXPosition.size()-1));
        }
        LogUtils.d("lineXPosition.size="+lineXPosition.size());
        LogUtils.d("textHight="+textHight+";textMargin="+textMargin+";longLineMarginTop="+longLineMarginTop);
        longLineTopPosition = textHight + textMargin+longLineMarginTop;
        longLineBottomPosition = longLineTopPosition + longLineLength;
        shortLineTopPosition = textHight + textMargin+shortLineMarginTop;
        shortLineBottomPosition = shortLineTopPosition + shortLineLength;
        LogUtils.d("longLineTopPosition="+longLineTopPosition+";shortLineMarginTop="+shortLineMarginTop);
    }
    private void setText() {
        int textTop = textMargin; //字体离控件距离为50
        Rect rect = new Rect();
        paint.getTextBounds("2021", 0, "2021".length(), rect);
        textWidth = rect.width();
        textHight = rect.height();
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float dy = (fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        int baseLine = (int)(textMargin+textHight/2+dy);  //基线是字体中心y坐标加上偏移dy
        textPosition[0] = screenWidth/2 - textWidth*4+20;//字体绘制的起始位置的x坐标
        textPosition[1] = baseLine;   //字体绘制的baseLine坐标
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtils.d("event.getX()="+event.getX());
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lineEndPosition = event.getX();
                lineStartPosition = lineEndPosition;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (lineStartPosition!=0&&lineEndPosition!=0) {
                    float dx = event.getX() - lineEndPosition;
//                    lineEndPosition = event.getX();
                    LogUtils.d("dx==="+dx);
                    boolean isMoveLeft;
                    if (dx<0){
                        isMoveLeft = true; // 线向左移
                        dx = -dx;
                    }else {
                        isMoveLeft = false; // 线向右移
                    }
                    if (dx>8){
                        int dxx = (int)dx/8;
                        if (dxx*8>dx){
                            dxx++;
                        }
                        LogUtils.d("dxx="+dxx);
                        for (int i=0; i<dxx;i++){
                            float dxxx = dx - 8*(i+1);
                            LogUtils.d("dxxx="+dxxx+"i="+i);
                            if (dxxx<=8&&dxxx>0){
                                handleMove(dxxx,isMoveLeft);
                            }else if (dxxx>8){
                                handleMove(8,isMoveLeft);
                            }
                        }
                    }else {
                        handleMove(dx,isMoveLeft);
                        adjust(isMoveLeft);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                LogUtils.d("action up");
                adjustUp();
                lineStartPosition = 0;
                lineEndPosition = 0;
//                handleUp();
                break;
        }
        return true;
    }

    private void adjustUp() {
        float dx = (float)lineXPosition.get(0)-lineMarginLeft;
        for (int i=0;i<lineXPosition.size();i++){
            float x = (float)lineXPosition.get(i)-dx;
            lineXPosition.set(i,x);
        }
    }

    public int getAge(){
        return text;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                lineEndPosition = event.getX();
//                return true;
//            case MotionEvent.ACTION_MOVE:
//                float dx = event.getX() - lineEndPosition;
//                lineEndPosition = event.getX();
//                handleMove(dx);
//                invalidate();
//                adjust();
//                break;
//           case MotionEvent.ACTION_UP:
//                LogUtils.d("action up");
////                handleUp();
//                break;
//        }
//        return super.onTouchEvent(event);
//
//    }

    private void adjust(boolean isMoveLeft) {
        if ((Math.abs(lineEndPosition - lineStartPosition)>lineSpace)&&isMoveLeft){
            lineXPosition.remove(0);
            adjustMove(isMoveLeft);
            text++;
            lineXPosition.add(lastXposition);
            invalidate();
            LogUtils.d("lineXPosition.get(0)="+lineXPosition.get(0));
            LogUtils.d("==lineXPosition.get("+lineXPosition.size()+")="+lineXPosition.get(lineXPosition.size()-1));
            lineStartPosition = lineEndPosition;
        }else if ((lineEndPosition - lineStartPosition>lineSpace)&& !isMoveLeft){
            adjustMove(isMoveLeft);
            lineXPosition.remove(lineXPosition.size()-1);
            text--;
            lineXPosition.add(0,lineMarginLeft-lineSpace);
            invalidate();
            LogUtils.d("lineXPosition.get(0)="+lineXPosition.get(0));
            LogUtils.d("lineXPosition.get(24)="+lineXPosition.get(lineXPosition.size()-1));
            lineStartPosition = lineEndPosition;
        }
        LogUtils.d("==lineXPosition.get("+lineXPosition.size()+")="+lineXPosition.get(lineXPosition.size()-1));
//        adjustMove(isMoveLeft);
    }

    private void adjustMove(boolean isMoveLeft) {
        if (isMoveLeft){
            LogUtils.d("=34=lineXPosition.get("+lineXPosition.size()+")="+lineXPosition.get(lineXPosition.size()-1)+";;lastXposition="+lastXposition);
            float dx = (float)lineXPosition.get(0)-lineMarginLeft;
            for (int i=0;i<lineXPosition.size();i++){
                float x = (float)lineXPosition.get(i)-dx;
                lineXPosition.set(i,x);
            }
        }else {
            float dx = (float)lineXPosition.get(lineXPosition.size()-1)-lastXposition;
            for (int i=0;i<lineXPosition.size();i++){
                float x = (float)lineXPosition.get(i)-dx;
                lineXPosition.set(i,x);
            }
        }
    }

    private void handleMove(float dx,boolean isMoveLeft) {
        if (isMoveLeft){
            dx = -dx;
        }
        LogUtils.d("dx=-="+dx);
        lineEndPosition = lineEndPosition+dx;
        for (int i=0; i<lineXPosition.size(); i++){
            float x = (float)lineXPosition.get(i)+dx;
            lineXPosition.set(i,x);
        }
        invalidate();
        adjust(isMoveLeft);
    }
}
