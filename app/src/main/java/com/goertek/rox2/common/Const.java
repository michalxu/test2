package com.goertek.rox2.common;
/**
 * 文件名：Const
 * 描述：全局常量
 * 创建时间：2020/8/26
 * @author jochen.zhang
 */
public class Const {
    /** 低电阈值 */
    public static final int LOW_POWER = 20;

    /** 测试频点 */
    public static final int[] LIST_FREQUENCY = new int[]{
//            1000, 63, 2000, 125, 4000, 250, 8000, 500, 12500
//            500, 1000, 2000, 4000
            63,125,250,500,1000,2000,4000,8000,12500
    };
    /** 测试频点数 */
    public static final int MAX_FREQUENCY = LIST_FREQUENCY.length;

    /** 每频点增益 */
    public static final int[][] LIST_LEVEL = {
            {0, 1, 2, 3,4,5,6,7,8},
            {0, 1, 2, 3,4,5,6,7,8},
            {0, 1, 2, 3,4,5,6,7,8},
            {0, 1, 2, 3,4,5,6,7,8},
//            {0, 1, 3, 3},
//            {0, 1, 2, 3},
//            {0, 1, 3, 3},
//            {0, 1, 2, 3},
//            {0, 1, 3, 3},
//            {0, 1, 2, 3},
//            {0, 1, 3, 3},
//            {0, 1, 2, 3},
//            {0, 1, 3, 3}
    };
    /** 每频点Level */
    public static final int MAX_LEVEL = LIST_LEVEL[0].length;

    /** 默认EQ Level */
    public static final int MAX_DEFAULT_LEVEL = 9;//6

    /** 默认EQ：1 */
    public static final int[] DEFAULT_EQ_1 = new int[]{
            0, 0, 0, 4, 0, 2, 2, 0, 0
    };
    /** 默认EQ：2 */
    public static final int[] DEFAULT_EQ_2 = new int[]{
            0, 0, 0, 2, 1, 1, 1, 0, 0
    };
    /** 默认EQ：3 */
    public static final int[] DEFAULT_EQ_3 = new int[]{
            0, 0, 0, 0, 3, 0, 3, 0, 0
    };

    /** EventBus Code，自己定义所需的Event事件 */
    public static final class EventCode {
        // Bluetooth
        public static final int BluetoothDisconnectedCode  = 0x0000;// 连接断开 携带设备mac地址
        public static final int BluetoothConnectedCode     = 0x0001;// 连接成功 携带设备mac地址
        // SPP
        public static final int ConnectStateCode           = 0x1000;// 连接状态 携带连接的ConnectState
        public static final int BatteryStateCode           = 0x1001;// 电量变化 携带Battery
        // EQ
        public static final int EQRefreshStateCode         = 0x2000;// EQ状态变化 携带 true 强制变更索引
        public static final int NoiceControlStateCode      = 0x3000;//NoiceControl状态变化
        public static final int SettingsStateCode          = 0x4000;//设置中的所有开关的状态
        public static final int HeartRateDataCode          = 0x5000;//收到了心率数据
    }
}
