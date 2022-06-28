package com.goertek.rox2.ui.main;

import android.util.Log;

import com.goertek.rox2.ui.main.utils.LogOutFileUtils;
import com.goertek.rox2.ui.main.utils.timeUtils;

import java.util.Locale;

public class LogUtils {
    private LogUtils(){
        throw new UnsupportedOperationException(this.getClass().getSimpleName()+"cannot be instantiated");
    }
    public static void d(String message){
        LogOutFileUtils.d(buildTag(),buildMessage(message));
        Log.d(buildTag(),buildMessage(message));
    }
    public static void w(String message){
        LogOutFileUtils.w(buildTag(),buildMessage(message));
        Log.w(buildTag(),buildMessage(message));
    }
    public static void i(String message){
        LogOutFileUtils.i(buildTag(),buildMessage(message));
        Log.i(buildTag(),buildMessage(message));
    }
    public static void e(String message){
        LogOutFileUtils.e(buildTag(),buildMessage(message));
        Log.e(buildTag(),buildMessage(message));
    }
    private static String buildTag(){
        return "Michal";
    }
    private static String buildTag(String tag){
        return tag;
    }
    private static String buildMessage(String message){
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        if (traceElements == null||traceElements.length<4){
            return message;
        }

        StackTraceElement traceElement = traceElements[4];
        return String.format(Locale.US,"%s.%s(%s:%d)"+"\n   {>>>>   %s}",
                traceElement.getClassName().substring(traceElement.getClassName().indexOf(".")+1),
                traceElement.getMethodName(),
                traceElement.getFileName(),
                traceElement.getLineNumber(),message);
    }
}
