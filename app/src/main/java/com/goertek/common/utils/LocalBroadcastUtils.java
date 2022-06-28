package com.goertek.common.utils;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.goertek.common.event.Event;

/**
 * 文件名 LocalBroadcastUtils.java
 * 描述 使用LocalBroadcast代替EventBus类使用，尽可能保持原有接口不变
 * 与BaseActivity、BaseFragment、BaseService配合使用
 */
public class LocalBroadcastUtils {
    public static final String KEY_ACTION = "KEY_ACTION";
    public static final String KEY_EVENT = "KEY_EVENT";

    public static void registerLocalBroadCast(BroadcastReceiver localBroadcastReceiver) {
        IntentFilter filter = new IntentFilter(LocalBroadcastUtils.KEY_ACTION);
        LocalBroadcastManager.getInstance(Utils.getContext()).registerReceiver(localBroadcastReceiver, filter);
    }

    public static void unregisterLocalBroadCast(BroadcastReceiver localBroadcastReceiver) {
        LocalBroadcastManager.getInstance(Utils.getContext()).unregisterReceiver(localBroadcastReceiver);
    }

    public static void post(Event event) {
        LogUtils.d("post event = "+event.getCode()+"event data="+event.getData());
        Intent intent = new Intent(KEY_ACTION);
        intent.putExtra(KEY_EVENT, event);
        LocalBroadcastManager.getInstance(Utils.getContext()).sendBroadcast(intent);
    }
}
