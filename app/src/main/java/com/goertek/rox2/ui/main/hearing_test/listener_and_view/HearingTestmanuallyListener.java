package com.goertek.rox2.ui.main.hearing_test.listener_and_view;

/**
 * 创建时间：2021/6/30
 *
 * @author michal.xu
 */
public interface HearingTestmanuallyListener {
    void ondBChange(int angle);
    void onProgressTouchDown();
    void onProgressTouchUp();
    void onViewPauseClicked(boolean pause);
}
