package com.goertek.common.utils;

import android.content.Context;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class XmlUtils {
    /**
     * 解析data文件夹下文件
     */
    public static <T> T loadXmlClass(Context context, String name, Class<T> clz) {
        InputStream inputStream = null;
        try {
            inputStream = context.getApplicationContext().openFileInput(name);
            XStream xStream = new XStream();
            xStream.processAnnotations(clz);
            Object result = xStream.fromXML(inputStream);
            return result == null ? null : (T) result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 保存data文件夹下文件
     */
    public static <T> void saveXmlClass(Context context, String name, T config) {
        XStream xStream = new XStream();
        xStream.processAnnotations(config.getClass());
        String xml = xStream.toXML(config);
        FileOutputStream outputStream = null;
        try {
            outputStream = context.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(xml.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
