package com.goertek.common.widget;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.goertek.common.utils.LogUtils_goertek;

/**
 * Created by Ansen on 2015/5/14 23:30.
 *
 * @E-mail: ansen360@126.com
 * @Blog: http://blog.csdn.net/qq_25804863
 * @Github: https://github.com/ansen360
 * @PROJECT_NAME: FrameAnimation
 * @PACKAGE_NAME: com.ansen.frameanimation.sample
 * @Description: TODO
 */
public class FrameAnimation {
    private static final String TAG = "FrameAnimation";
    private boolean mIsRepeat;
    private AnimationListener mAnimationListener;
    private ImageView mImageView;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int[] mFrameRess;

    /**
     * 每帧动画的播放间隔
     */
    private int mDuration;

    private int mLastFrame;

    private boolean mPause;

    private int mCurrentFrame;

    private long startTime;


    /**
     * @param iv       播放动画的控件
     * @param frameRes 播放的图片数组
     * @param duration 每帧动画的播放间隔(毫秒)
     * @param isRepeat 是否循环播放
     */
    public FrameAnimation(ImageView iv, int[] frameRes, int duration, boolean isRepeat) {
        this.mImageView = iv;
        this.mFrameRess = frameRes;
        this.mDuration = duration;
        this.mLastFrame = frameRes.length - 1;
        this.mIsRepeat = isRepeat;
    }

    public void playNext(int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPause) {
                    return;
                }

                if (mCurrentFrame == 0) {
                    if (mAnimationListener != null) {
                        mAnimationListener.onAnimationStart();
                    }
                } else if (mLastFrame == mCurrentFrame) {
                    if (mIsRepeat) {
                        if (mAnimationListener != null) {
                            mAnimationListener.onAnimationRepeat();
                        }
                        startTime = System.currentTimeMillis();
                        mCurrentFrame = 0;
                        mImageView.setBackgroundResource(mFrameRess[0]);
                        playNext(mDuration);
                        return;
                    } else {
                        if (mAnimationListener != null) {
                            mAnimationListener.onAnimationEnd();
                        }
                        return;
                    }
                }

                long time = System.currentTimeMillis();
                int frame = (int) (time - startTime) / mDuration;
                if (frame != mCurrentFrame) {
                    if (frame > mLastFrame) {
                        frame = mLastFrame;
                    }
                    if (frame != mCurrentFrame + 1) {
                        LogUtils_goertek.w(TAG, "丢失 " + (frame - mCurrentFrame - 1) + "帧");
                    }
                    mCurrentFrame = frame;
                    mImageView.setBackgroundResource(mFrameRess[frame]);
                }
                long nextTime = System.currentTimeMillis();
                long timeOffset = nextTime - startTime;
                int nextFrame = (int) (timeOffset) / mDuration;
                if (nextFrame == frame) {
                    int delay = mDuration - (int) (timeOffset) % mDuration;
                    playNext(delay);
                } else {
                    playNext(0);
                }
            }
        }, delay);
    }

    public interface AnimationListener {

        /**
         * <p>Notifies the start of the animation.</p>
         */
        void onAnimationStart();

        /**
         * <p>Notifies the end of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         */
        void onAnimationEnd();

        /**
         * <p>Notifies the repetition of the animation.</p>
         */
        void onAnimationRepeat();
    }

    /**
     * <p>Binds an animation listener to this animation. The animation listener
     * is notified of animation events such as the end of the animation or the
     * repetition of the animation.</p>
     *
     * @param listener the animation listener to be notified
     */
    public void setAnimationListener(AnimationListener listener) {
        this.mAnimationListener = listener;
    }

    public void start() {
        mPause = false;
        mHandler.removeCallbacksAndMessages(null);
        startTime = System.currentTimeMillis();
        mCurrentFrame = 0;
        mImageView.setBackgroundResource(mFrameRess[0]);
        playNext(mDuration);
    }

    public void release() {
        pauseAnimation();
        mHandler.removeCallbacksAndMessages(null);
        mCurrentFrame = 0;
        mImageView.setBackgroundResource(mFrameRess[0]);
    }

    public void pauseAnimation() {
        this.mPause = true;
    }

    public boolean isPause() {
        return this.mPause;
    }
}
