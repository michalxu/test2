package com.goertek.common.utils;

import android.content.Context;

import com.goertek.bluetooth.protocol.tws.entity.TWSEQParams;
import com.goertek.rox2.common.entity.EQConfig;
import com.goertek.rox2.common.entity.EQModel;

import java.util.List;
/**
 * 文件名：EQUtils
 * 描述：EQ工具类
 * 创建时间：2020/9/7
 * @author jochen.zhang
 */
public class EQUtils {
    private static final String KEY_XML_EQ = "KEY_XML_EQ";
    /** 应用EQ配置 */
    private EQConfig mConfig;
    /** 设备当前Params */
    private TWSEQParams mParams;

    /** 从本地加载EQConfig，若不存在则新建 */
    public synchronized EQConfig loadEQConfig(Context context) {
        if (mConfig == null) {
            mConfig = XmlUtils.loadXmlClass(context, KEY_XML_EQ, EQConfig.class);
        }
        if (mConfig == null) {
            mConfig = new EQConfig();
        }
        return mConfig;
    }

    /** 内存Config缓存至文件中 */
    public synchronized void saveEQConfig(Context context) {
        if (mConfig != null) {
            XmlUtils.saveXmlClass(context, KEY_XML_EQ, mConfig);
        }
    }

    /** return true 有同名EQ */
    public boolean containName(Context context, String name) {
        List<EQModel> list = loadEQConfig(context).getUserList();
        if (list == null) {
            return false;
        }
        for (EQModel model : list) {
            if (name.equals(model.getName())) {
                return true;
            }
        }
        return false;
    }

    /** 清空设备当前参数(设备断连时) */
    public void removeParams() {
        mParams = null;
    }

    /** 获取设备当前参数 */
    public TWSEQParams getParams() {
        return mParams;
    }

    /**
     * 设置设备当前参数
     * 1、耳机连接后主动查询
     * 2、APP设置EQ成功后
     */
    public void setParams(TWSEQParams params) {
        if (params == null) {
            return;
        }
        if (mParams != params) {
            mParams = params;
        }
    }

    /***********************************************************************************************
     * 单例化
     **********************************************************************************************/
    private EQUtils() {
        // 私有化构造函数
    }

    public static EQUtils getInstance() {
        return Singleton.SINGLETON.getSingleTon();
    }

    public enum Singleton {
        /** 枚举本身序列化之后返回的实例 */
        SINGLETON;
        private EQUtils singleton;

        /** JVM保证只实例一次 */
        Singleton() {
            singleton = new EQUtils();
        }

        /** 公布对外方法 */
        public EQUtils getSingleTon() {
            return singleton;
        }
    }
}
