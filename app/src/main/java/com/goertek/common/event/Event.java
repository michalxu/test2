package com.goertek.common.event;

import java.io.Serializable;

/**
 * 文件名：Event
 * 描述：EventBus Event基类只有继承该类才能通过重写BaseActivity的
 * public void onEvent(Event event)方法监听到
 * <p>
 * 粘性事件允许Event发送在注册监听Event之前
 * 监听粘性事件需要在函数前加上注解@Subscribe(sticky = true)
 * 粘性事件是使用Map存储的，每种Class类型只能存一个事件，旧事件会被新事件覆盖
 */

public class Event<T> implements Serializable {
    //Event标识码
    private int code;
    //Event携带数据
    private T data;

    public Event(int code) {
        this.code = code;
    }

    public Event(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
