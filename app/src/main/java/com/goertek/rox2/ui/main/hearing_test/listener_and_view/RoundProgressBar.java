package com.goertek.rox2.ui.main.hearing_test.listener_and_view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingTestActivity;

/**
 * 创建时间：2021/7/9
 *
 * @author michal.xu
 */
public class RoundProgressBar extends View {

    private int radius = 50;
    private Paint bigBackgourndPaint;
    private Paint bigProgressPaint;
    private Paint bigOverPaint;
    private Context mContext;

    private int pauseRadius = 100;
    private  int bigRadius;
    private int smallRidus =200;
    private int pauseBackgroundRadius = 36;
    private Paint mSmallBackgroundPaint = new Paint();
    private Paint msmallProcessPaint = new Paint();
    private Paint pausePaint = new Paint();
    private Paint pausePaintBackground = new Paint();
    /** 触摸中 */
    private boolean isTouching = false;
    private boolean isPause = false;
    /** 角度 */
    private int progressAngle = 0;
    int screenWidth;
    int screenHeight;
    private int bitmapLeft;
    private int bitmapTop;
    private RectF bigCircleRectF;
    private RectF smallCircleRectF;

    private static HearingTestmanuallyListener mHearingTestmanuallyListener;
    private static HearingAutoTestListener mHearingAutoTestListener;
    public RoundProgressBar(Context context) {
        this(context,null);
    }

    public RoundProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RoundProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        bigProgressPaint = new Paint();
        bigBackgourndPaint = new Paint();
//        smallRidus  = ScreenUtils.dp2px(smallRidus);
//        pauseBackgroundRadius = ScreenUtils.dp2px(pauseBackgroundRadius);

        //小圆背景画笔
        mSmallBackgroundPaint.setColor(getResources().getColor(R.color.gray));
        mSmallBackgroundPaint.setStyle(Paint.Style.STROKE);
        mSmallBackgroundPaint.setStrokeWidth(20);
        //小圆进度画笔
        msmallProcessPaint.setColor(getResources().getColor(R.color.text_color_blue));
        msmallProcessPaint.setStyle(Paint.Style.STROKE);
        msmallProcessPaint.setStrokeWidth(20);
        //暂停按钮背景画笔
        pausePaintBackground.setColor(Color.GRAY);
        pausePaintBackground.setStyle(Paint.Style.FILL);
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
        caculatePosition(getWidth(),getHeight());

        Bitmap bitmap=null;
        Canvas mcanvas = null;
        //绘制小圆背景部分
        canvas.drawArc(smallCircleRectF,-90, 360,false,mSmallBackgroundPaint);
        //绘制小圆进度部分
        canvas.drawArc(smallCircleRectF,-90, progressAngle,false,msmallProcessPaint);
        //绘制暂停按钮背景
        canvas.drawCircle(getWidth()/2,getHeight()/2,92,pausePaintBackground);
//        //绘制暂停按钮
//        Bitmap pauseBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.icon_pause);
//        canvas.drawBitmap(pauseBitmap,getWidth()/2-pauseRadius,getHeight()/2-pauseRadius,pausePaint);

//        Drawable drawable = getResources().getDrawable(R.drawable.img_test_process);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.img_test_process);
        canvas.drawBitmap(bitmap1,0,0,bigBackgourndPaint);

        Resources res= getResources();
        Drawable drawable = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = getResources().getDrawable(R.drawable.img_test_process,null);
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bm = bd.getBitmap();
        setLayerType(LAYER_TYPE_HARDWARE,null);
        bigProgressPaint.setColor(getResources().getColor(R.color.gray));
        bigProgressPaint.setStrokeWidth(100);
        bigProgressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        RectF rectF = new RectF(0, 0, 900, 900);
        canvas.drawBitmap(bm,0,0,bigProgressPaint);
        canvas.drawArc(rectF,90,30,false,bigProgressPaint);



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
        smallCircleRectF =  getCircleRectF(width / 2f, height / 2f, smallRidus);

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
                if (Math.abs(r-bigRadius)<=20 && !isPause){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    isTouching = true;
                    progressAngle = angle;
                    LogUtils.d("michal,action down accurate");
                    invalidate();
                    if (mHearingTestmanuallyListener!=null){
                        mHearingTestmanuallyListener.onProgressTouchDown();
                    }
                    return true;
                }else if (r-pauseRadius<=20){
                    isPause = !isPause;
                    mHearingTestmanuallyListener.onViewPauseClicked(isPause);
                    LogUtils.d("暂停 view isPause = "+isPause);
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
        return new RectF(x - radius, y - radius, x + radius, y + radius);
    }

    public Bitmap getBitmap(int resId) {
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(resId);
        return drawable.getBitmap();
    }

    public static void setHearingTestManuallyListener(HearingTestmanuallyListener hearingTestmanuallyListener) {
        mHearingTestmanuallyListener = hearingTestmanuallyListener;
    }
}
