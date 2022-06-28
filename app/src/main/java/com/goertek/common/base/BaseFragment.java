package com.goertek.common.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.goertek.common.event.Event;
import com.goertek.common.utils.LocalBroadcastUtils;

import java.lang.ref.WeakReference;
/**
 * 文件名：BaseFragment
 * 描述：Fragment基类
 * 创建时间：2020/9/2
 * @author jochen.zhang
 */
public abstract class BaseFragment extends Fragment {
    private boolean isActive = true;
    private boolean isDestroy = false;
    private MyHandler mBaseHandler = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDestroy()) {
                return;
            }
            if (intent != null) {
                Event event = (Event) intent.getSerializableExtra(LocalBroadcastUtils.KEY_EVENT);
                if (event != null) {
                    onEvent(event);
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isRegisterLocalBroadcast()) {
            LocalBroadcastUtils.registerLocalBroadCast(mReceiver);
        }
        mBaseHandler = new MyHandler(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isDestroy = false;
        isActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onDestroy() {
        if (isRegisterLocalBroadcast()) {
            LocalBroadcastUtils.unregisterLocalBroadCast(mReceiver);
        }
        super.onDestroy();
        //销毁handlers
        try {
            mBaseHandler.removeCallbacksAndMessages(null);
            mBaseHandler = null;
        } catch (Exception ignored) {
        }
        isDestroy = true;
        isActive = false;
    }
    /*********************************************************************************************************************************
     * Handler 相关函数
     ********************************************************************************************************************************/
    /** 获取Handler */
    protected MyHandler getHandler() {
        return mBaseHandler;
    }

    /** 子类重写处理Handler方法 */
    protected void onMessage(Message msg) {

    }

    /** 自定义Handler */
    public static class MyHandler extends Handler {
        private final WeakReference<BaseFragment> mWeakReference;

        public MyHandler(BaseFragment reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseFragment context = mWeakReference.get();
            if (context != null) {
                if (context.isDestroy()) {
                    return;
                }
                context.onMessage(msg);
            }
        }
    }
    /*********************************************************************************************************************************
     * 其他函数
     ********************************************************************************************************************************/
    /**
     * 获取状态
     *
     * @return
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 获取是否销毁的状态
     *
     * @return
     */
    public boolean isDestroy() {
        return isDestroy;
    }

    /**
     * 是否注册事件分发
     *
     * @return true绑定LocalBroadcast事件分发，默认不绑定，子类需要绑定的话复写此方法返回true.
     */
    protected boolean isRegisterLocalBroadcast() {
        return false;
    }

    /**
     * 监听LocalBroadcast消息
     *
     * @param event 接收到的event
     */
    public void onEvent(Event event) {}
}
