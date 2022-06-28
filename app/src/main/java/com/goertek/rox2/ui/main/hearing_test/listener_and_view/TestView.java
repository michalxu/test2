package com.goertek.rox2.ui.main.hearing_test.listener_and_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 创建时间：2021/7/12
 *
 * @author michal.xu
 */
public class TestView extends View {
    public TestView(Context context) {
        this(context,null);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        paint.setColor(Color.YELLOW);
        canvas.drawRect(100,100,200,1000,paint);
        Paint paint1 = new Paint();
        paint1.setStyle(Paint.Style.FILL);
        paint1.setStrokeWidth(3);
        paint1.setColor(Color.BLUE);
        Path path = new Path();
        path.moveTo(100, 100+50);
        path.arcTo(new RectF(100, 100, 200, 100+100), -180, 180);
        path.close();
        canvas.drawPath(path,paint1);
    }
}
