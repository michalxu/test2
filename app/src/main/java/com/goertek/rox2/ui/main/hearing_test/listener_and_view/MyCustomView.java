package com.goertek.rox2.ui.main.hearing_test.listener_and_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.goertek.common.utils.ScreenUtils;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingTestActivity;

/**
 * 创建时间：2021/6/29
 *
 * @author michal.xu
 */
public class MyCustomView extends View {

    private int pauseRadius = 100;
    private  int bigRadius;
    private int smallRidus;
    private Paint mBigBackgroundPaint = new Paint();
    private Paint mBigProcessPaint = new Paint();
    private Paint mSmallBackgroundPaint = new Paint();
    private Paint msmallProcessPaint = new Paint();
    private Paint pausePaint = new Paint();
    private Paint pausePaintBackground = new Paint();
    /** 触摸中 */
    private boolean isTouching = false;
    private boolean isPause = false;
    /** 角度 */
    private int progressAngle = 0;
    /** 坐标 */
    int screenWidth;
    int screenHeight;
    private int bitmapLeft;
    private int bitmapTop;
    private RectF bigCircleRectF;
    private RectF smallCircleRectF;
    private static HearingTestmanuallyListener mHearingTestmanuallyListener;
    private static HearingAutoTestListener mHearingAutoTestListener;
    public MyCustomView(Context context) {
        this(context,null);
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalProgress);
        bigRadius = (int)typedArray.getDimension(R.styleable.VerticalProgress_progressSize, ScreenUtils.dp2px(150));
        smallRidus = 77;
        smallRidus = ScreenUtils.dp2px(smallRidus);
//        PathEffect effect = new DashPathEffect(new float[] { 10, 10}, 1);
        //油表的位置方框
        int width = bigRadius*2;
        int height = bigRadius*2;
        LogUtils.d("getWidth()"+width+";getHeight()"+height);
        RectF rectF =new RectF(0, 0, width, height);
        Path mPath = new Path();
        mPath.reset();
        //在油表路径中增加一个从起始弧度
        mPath.addArc(rectF, 0, 360);
        //计算路径的长度
        PathMeasure pathMeasure = new PathMeasure(mPath, false);
        float length = pathMeasure.getLength();
        float step = length / 480;
        LogUtils.d("length="+length+";step="+step);
        PathEffect effect = new DashPathEffect(new float[]{step,step*2}, 0);
        //进度画笔设置
        mBigProcessPaint.setColor(getResources().getColor(R.color.light_blue));
        mBigProcessPaint.setStyle(Paint.Style.STROKE);
        mBigProcessPaint.setStrokeWidth(135);
        mBigProcessPaint.setPathEffect(effect);
        //大圆背景画笔
        mBigBackgroundPaint.setColor(getResources().getColor(R.color.light_gray));
        mBigBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBigBackgroundPaint.setStrokeWidth(135);
        mBigBackgroundPaint.setPathEffect(effect);
        //小圆背景画笔
        mSmallBackgroundPaint.setColor(getResources().getColor(R.color.light_gray));
        mSmallBackgroundPaint.setStyle(Paint.Style.STROKE);
        mSmallBackgroundPaint.setStrokeWidth(10);
        //小圆进度画笔
        msmallProcessPaint.setColor(getResources().getColor(R.color.colorDialogButtonBlue));
        msmallProcessPaint.setStyle(Paint.Style.STROKE);
        msmallProcessPaint.setStrokeWidth(10);

        pausePaintBackground.setColor(Color.WHITE);
        pausePaintBackground.setStyle(Paint.Style.FILL);
        pausePaintBackground.setShadowLayer(50, 2, 2, getResources().getColor(R.color.light_gray));

        SweepGradient radialGradient = new SweepGradient(width/2, height/2, Color.RED,Color.GREEN);

       mHearingAutoTestListener = new HearingAutoTestListener() {
            @Override
            public void ondBChange(int dbValue) {
                progressAngle = dbValue*4;
                if (progressAngle>359){
                    progressAngle = 359;
                }
                LogUtils.d("监听到自动增加为");
                invalidate();
            }

            @Override
            public void onNextTestClicked() {
                progressAngle = 0;
                LogUtils.d("michal,action down accurate");
                invalidate();
            }

        };
        LogUtils.d("View 设置mHearingAutoTestListener");
        HearingTestActivity.setHearingAutoTestListener(mHearingAutoTestListener);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        LogUtils.d("width"+width+",height"+height);
        bitmapLeft = width/2 - 54;
        bitmapTop = height/2 - 54;
        //计算控件的位置，并确定其RectF值
        caculatePosition(width,height);
        //绘制大圆背景部分
        canvas.drawArc(bigCircleRectF,-90,359,false,mBigBackgroundPaint);
        //绘制大圆进度部分
        LogUtils.d("即将绘制大圆，progressAngle = "+progressAngle);
        canvas.drawArc(bigCircleRectF,-90, progressAngle,false,mBigProcessPaint);
        //绘制小圆背景部分
        canvas.drawArc(smallCircleRectF,-90, 360,false,mSmallBackgroundPaint);
        //绘制小圆进度部分
        canvas.drawArc(smallCircleRectF,-90, progressAngle,false,msmallProcessPaint);
        //绘制暂停按钮
        canvas.drawCircle(getWidth()/2,getHeight()/2,120,pausePaintBackground);
        if (isPause){
            canvas.drawBitmap(getBitmap(R.drawable.icon_play),bitmapLeft,bitmapTop,pausePaint);
        }else {
            canvas.drawBitmap(getBitmap(R.drawable.icon_pause),bitmapLeft,bitmapTop,pausePaint);
        }
        LogUtils.d("bitmapLeft="+bitmapLeft+"bitmapTop"+bitmapTop);
    }

    private void caculatePosition(int width, int height) {
        if (width == screenWidth && height == screenHeight){
            //已经计算过坐标，不需要重新计算
            return;
        }
        screenWidth = width;
        screenHeight = height;
        LogUtils.d("width="+width+",height="+height+"bigRadius="+bigRadius);
        bigCircleRectF =  getCircleRectF(width / 2f, height / 2f, bigRadius);
        smallCircleRectF =  new RectF(width/2f - smallRidus, height/2f - smallRidus, width/2f + smallRidus, height/2f + smallRidus);


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 坐标中心移动到圆心
        float x = event.getX() - getWidth() / 2f;
        float y = event.getY() - getHeight() / 2f;
        // 计算角度
        int angle = (int) Math.floor((Math.atan2(-x, y) * 180 / Math.PI) + 180);
        LogUtils.d("andle ==="+angle);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                //判断是否点击在圆弧附近
                double r = Math.sqrt(x * x + y * y);
                if (angle > 360) {
                    angle = 359;
                }
                if (Math.abs(r-bigRadius)<=100 && !isPause){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    isTouching = true;
                    progressAngle = angle;
                    LogUtils.d("michal,action down accurate");
                    invalidate();
                    if (mHearingTestmanuallyListener!=null){
                        mHearingTestmanuallyListener.onProgressTouchDown();
                    }
                    return true;
                }else if (r-pauseRadius<=50){
                    isPause = !isPause;
                    mHearingTestmanuallyListener.onViewPauseClicked(isPause);
                    LogUtils.d("暂停 view isPause = "+isPause);
                    invalidate();
                    return true;
                }

            case MotionEvent.ACTION_MOVE:
                if (isTouching&&(Math.abs(angle-progressAngle)>=5)&&angle<=360){
                    LogUtils.d("michal,action move angle"+angle+"progressAngle="+progressAngle);
                    progressAngle = angle;
                    if (mHearingTestmanuallyListener!=null){
                        mHearingTestmanuallyListener.ondBChange(progressAngle);
                    }
                    invalidate();
                }else if (isTouching&&(360-angle<0.5)&&angle<=360){
                    progressAngle = 359;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                isTouching = false;
                if (mHearingTestmanuallyListener!=null && !isPause){
                    mHearingTestmanuallyListener.onProgressTouchUp();
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }


    private RectF getCircleRectF(float x, float y, int radius) {
        return new RectF(x - radius+70, y - radius+70, x + radius-70, y + radius-70);
    }

    public Bitmap getBitmap(int resId) {
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(resId);
        return drawable.getBitmap();
    }

    public static void setHearingTestManuallyListener(HearingTestmanuallyListener hearingTestmanuallyListener) {
        mHearingTestmanuallyListener = hearingTestmanuallyListener;
    }



}
