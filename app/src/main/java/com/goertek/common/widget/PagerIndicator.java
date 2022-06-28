package com.goertek.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.goertek.rox2.R;

/**
 * 文件名：PagerIndicator
 * 描述：页面指示器
 * 创建时间：2020/7/30
 * @author jochen.zhang
 */
public class PagerIndicator extends LinearLayout {
    private int selectPosition = -1;
    private int indicatorDrawableRes = 0;
    private int indicatorMargin = 15;

    public PagerIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.CENTER_VERTICAL);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PagerIndicator);
            indicatorMargin = a.getDimensionPixelOffset(R.styleable.PagerIndicator_indicatorMargin, indicatorMargin);
            indicatorDrawableRes = a.getResourceId(R.styleable.PagerIndicator_indicatorDrawableRes, indicatorDrawableRes);
            a.recycle();
        }
    }

    public void setIndicatorCount(int count) {
        removeAllViews();
        selectPosition = -1;
        if (count <= 0)  {
            return;
        }
        for (int i = 0; i < count; i++) {
            ImageView iv = new ImageView(getContext());
            iv.setImageResource(indicatorDrawableRes);
            if (i == 0) {
                iv.setSelected(true);
                this.selectPosition = i;
            }
            this.addView(iv, params(i));
        }
    }

    protected LayoutParams params(int position) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (position > 0) {
            if (getOrientation() == HORIZONTAL) {
                params.leftMargin = indicatorMargin;
            } else {
                params.topMargin = indicatorMargin;
            }
        }
        return params;
    }

    /**
     * 设置当前指示器位置
     */
    public void setCurrentIndicator(int position) {
        if (position == selectPosition || position < 0 || position >= getChildCount()) {
            return;
        }
        // 取出变换View
        View lastView = getChildAt(selectPosition);
        View currentView = getChildAt(position);
        // 上次选中与此次选中Indicator宽高互换
        ViewGroup.LayoutParams lastParams = lastView.getLayoutParams();
        ViewGroup.LayoutParams currentParams = currentView.getLayoutParams();
        int lastWidth = lastParams.width;
        int lastHeight = lastParams.height;
        lastParams.width = currentParams.width;
        lastParams.height = currentParams.height;
        currentParams.width = lastWidth;
        currentParams.height = lastHeight;
        lastView.setLayoutParams(lastParams);
        currentView.setLayoutParams(currentParams);
        // 更新选中状态
        lastView.setSelected(false);
        currentView.setSelected(true);
        this.selectPosition = position;
    }
}
