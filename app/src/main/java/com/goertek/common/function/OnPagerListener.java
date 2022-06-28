package com.goertek.common.function;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 文件名：OnPagerListener
 * 描述：封装OnScrollListener变为类ViewPager的OnPagerListener
 * 创建时间：2020/7/30
 * @author jochen.zhang
 */
public abstract class OnPagerListener extends RecyclerView.OnScrollListener {
    private PagerSnapHelper mPagerSnapHelper;
    private int currentPager = -1;

    public OnPagerListener(PagerSnapHelper mPagerSnapHelper) {
        this.mPagerSnapHelper = mPagerSnapHelper;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        View snapView = mPagerSnapHelper.findSnapView(layoutManager);
        if (snapView == null) {
            return;
        }
        int pager = layoutManager.getPosition(snapView);
        if (pager != currentPager) {
            currentPager = pager;
            onPager(pager);
        }
    }

    /**
     * 页面变更回调
     *
     * @param pager 页面索引
     */
    public abstract void onPager(int pager);
}
