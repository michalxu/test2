package com.goertek.rox2.ui.main.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName ： LogOutFileUtils
 * @Description ： log信息保存到外部存储log.txt
 * @Author ： Lani.wang
 * @Date ：2021/1/13 13:39
 */
public class LogOutFileUtils {

    //File file = null;
    private static String filePath = Environment.getExternalStorageDirectory() + "/" + "darwinlog/";
    static String fileName = "log";
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMdd-HH:mm:ss.SSS");

    public static void e(String mark, String info) {
        writeTxtToFile(new StringBuilder(dateFormat.format(new Date())).append(":error").append(":").append(mark).append(":").append(info).toString());
    }

    public static void e(String info) {
        writeTxtToFile(new StringBuilder(dateFormat.format(new Date())).append(":error").append(":").append(":").append(info).toString());
    }

    public static void i(String mark, String info) {
        writeTxtToFile(new StringBuilder(dateFormat.format(new Date())).append(":info").append(":").append(mark).append(":").append(info).toString());
    }

    public static void d(String mark, String info) {
        writeTxtToFile(new StringBuilder(dateFormat.format(new Date())).append(":debug").append(":").append(mark).append(":").append(info).toString());
    }

    public static void d(String info) {
        writeTxtToFile(new StringBuilder(dateFormat.format(new Date())).append(":debug").append(":").append(":").append(info).toString());
    }

    public static void w(String mark, String info) {
    }

    public static void w(String info) {
        writeTxtToFile(new StringBuilder(dateFormat.format(new Date())).append(":warn").append(":").append(info).toString());
    }

    // 将字符串写入到文本文件中
    private static void writeTxtToFile(String strcontent) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String sim = dateFormat.format(new Date());
            File fileName = new File(filePath + LogOutFileUtils.fileName + sim + ".txt");
            if (!fileName.exists()) {
                try {
                    fileName.createNewFile();
                } catch (IOException e) {
                    Log.e("TestFile", "Error on create File:" + e);
                }
            }
            FileInputStream fis = new FileInputStream(fileName);
            long size = fis.available();
            fis.close();
            /**
             * 当文件大小大于10MByte时，主动删除
             */
            if (size >= 10000000) {
                fileName.delete();
                return;
            }
            FileOutputStream outStream = new FileOutputStream(fileName, true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream, "utf-8");
            writer.write(strcontent);
            writer.write("\n");
            writer.flush();
            writer.close();// 记得关闭
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LogOutFile", e.toString());
        }
    }

}
