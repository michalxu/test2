package com.goertek.bluetooth.protocol;

/**
 * 文件名：ErrorCode
 * 描述：
 * 创建时间：2018/5/17.
 * @author jochen.zhang
 */
public class ErrorCode {
    /***********************************************************************************************
     *自定义错误
     **********************************************************************************************/
    public static final int ERROR_CODE_PARSE = 0;
    public static final int ERROR_REACH_MAX_RESEND = 1;
    public static final int ERROR_CLEAR_SEND_QUEUE = 2;

    public static String toErrorString(int errorCode) {
        switch (errorCode) {
            case ERROR_CODE_PARSE:
                return "设备指令解析错误(" + errorCode + ")";
            case ERROR_REACH_MAX_RESEND:
                return "指令达到最大重发次数(" + errorCode + ")";
            case ERROR_CLEAR_SEND_QUEUE:
                return "发送队列被清空(" + errorCode + ")";
            default:
                return "其他错误(" + errorCode + ")";
        }
    }
}
