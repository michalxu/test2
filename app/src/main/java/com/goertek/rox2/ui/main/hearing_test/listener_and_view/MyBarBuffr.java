package com.goertek.rox2.ui.main.hearing_test.listener_and_view;

import android.util.Log;

import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.goertek.rox2.ui.main.LogUtils;

/**
 * 创建时间：2021/7/13
 *
 * @author michal.xu
 */
public class MyBarBuffr extends BarBuffer {
    public MyBarBuffr(int size, int dataSetCount, boolean containsStacks) {
        super(size, dataSetCount, containsStacks);
    }
    @Override
    public void feed(IBarDataSet data) {

        float size = data.getEntryCount() * phaseX;
        float barWidthHalf = mBarWidth / 2f;

        for (int i = 0; i < size; i++) {

            BarEntry e = data.getEntryForIndex(i);

            if(e == null)
                continue;

            float x = e.getX();
            float y = e.getY();
            float z = 0f;
            Object object = e.getData();
            if (object!=null){
                z = (float)object;
            }
            float[] vals = e.getYVals();

            if (!mContainsStacks || vals == null) {

                float left = x - barWidthHalf;
                float right = x + barWidthHalf;
                float bottom, top;
                if (mInverted) {
                    bottom = y >= 0 ? y : 0;
                    top = y <= 0 ? y : 0;
                } else {
                    top = y >= 0 ? y : 0;
                    bottom = z >= 0 ? z : 0;
                }

                // multiply the height of the rect with the phase
                if (top > 0){
                    hasData = true;
                    Log.d("michal","hasData="+hasData);
                    top *= phaseY;
                }else if (bottom>0){
                    hasData = true;
//                    Log.d("michal","hasData="+hasData);
                    bottom *= phaseY;
                }
//                LogUtils.d("adBar");
                addBar(left, top, right, bottom);

            } else {

                float posY = 0f;
                float negY = -e.getNegativeSum();
                float yStart = 0f;

                // fill the stack
                for (int k = 0; k < vals.length; k++) {

                    float value = vals[k];

                    if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
                        // Take care of the situation of a 0.0 value, which overlaps a non-zero bar
                        y = value;
                        yStart = y;
                    } else if (value >= 0.0f) {
                        y = posY;
                        yStart = posY + value;
                        posY = yStart;
                    } else {
                        y = negY;
                        yStart = negY + Math.abs(value);
                        negY += Math.abs(value);
                    }

                    float left = x - barWidthHalf;
                    float right = x + barWidthHalf;
                    float bottom, top;

                    if (mInverted) {
                        bottom = y >= yStart ? y : yStart;
                        top = y <= yStart ? y : yStart;
                    } else {
                        top = y >= yStart ? y : yStart;
                        bottom = y <= yStart ? y : yStart;
                    }

                    // multiply the height of the rect with the phase
                    top *= phaseY;
                    bottom *= phaseY;
                    Log.d("michal","adBar");
                    addBar(left, top, right, bottom);
                }
            }
        }

        reset();
    }
}
