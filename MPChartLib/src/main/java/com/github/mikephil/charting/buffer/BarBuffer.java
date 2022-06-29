
package com.github.mikephil.charting.buffer;

import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

public class BarBuffer extends AbstractBuffer<IBarDataSet> {

    protected int mDataSetIndex = 0;
    protected int mDataSetCount = 1;
    protected boolean mContainsStacks = false;
    protected boolean mInverted = false;

    /** width of the bar on the x-axis, in values (not pixels) */
    protected float mBarWidth = 1f;

    public BarBuffer(int size, int dataSetCount, boolean containsStacks) {
        super(size);
        this.mDataSetCount = dataSetCount;
        this.mContainsStacks = containsStacks;
    }

    public void setBarWidth(float barWidth) {
        this.mBarWidth = barWidth;
    }

    public void setDataSet(int index) {
        this.mDataSetIndex = index;
    }

    public void setInverted(boolean inverted) {
        this.mInverted = inverted;
    }

    protected void addBar(float left, float top, float right, float bottom) {
        Log.e("MichalTest,addBar","left="+left+";top="+top+";right="+right+";bottom="+bottom);
        buffer[index++] = left;
        buffer[index++] = top;
        buffer[index++] = right;
        buffer[index++] = bottom;
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
            Object o = e.getData();
            if (o != null){
                z = (float)o;
                Log.e("MichalTest","z="+z+";y="+y);
            }
            float[] vals = e.getYVals();

            if (!mContainsStacks || vals == null) {

                float left = x - barWidthHalf;
                float right = x + barWidthHalf;
                float bottom, top;

                if (mInverted) {
                    bottom = y >= 0 ? y : 0;
                    top = z <= 0 ? z : 0;
                } else {
                    bottom = y >= 0 ? y : 0;
//                    bottom = y <= 0 ? y : 0;
                    top = z >= 0 ? z : 0;
                }

                // multiply the height of the rect with the phase
                if (top > 0)
                    top *= phaseY;
                else
                    bottom *= phaseY;
                Log.e("MichalTest","left="+left+";top="+top+";right="+right+";bottom="+bottom);
                addBar(left, top, right, bottom);
                Log.e("MichalTestbce","test1");
            } else {

                float posY = 0f;
                float negY = -e.getNegativeSum();
                float yStart = 0f;
                Log.e("MichalTestbce","test2");
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
                        Log.e("MichalTest","bottom="+bottom+";top="+top+";yStart="+yStart);
                    } else {
                        top = y >= yStart ? y : yStart;
                        bottom = y <= yStart ? y : yStart;
                        Log.e("MichalTest","bottom="+bottom+";top="+top+";yStart="+yStart);

                    }

                    // multiply the height of the rect with the phase
                    top *= phaseY;
                    bottom *= phaseY;

                    addBar(left, top, right, bottom);
                }
            }
        }

        reset();
    }
}
