package com.goertek.bluetooth.protocol.tws;

public class TWSCommand {
    public static final String SOF = "FEDCBA";
    public static final String END = "EF";

    public static final byte[] SOF_BYTE = {(byte) 0xFE, (byte) 0xDC, (byte) 0xBA};
    public static final byte[] END_BYTE = {(byte) 0xEF};

    public static final int SOF_LENGTH = SOF_BYTE.length;
    public static final int END_LENGTH = END_BYTE.length;

    public static final byte OP_GET_DEVICE_INFO = 0x02;
    public static final byte ATTR_GET_DEVICE_INFO_MULT_BATTERY = 0x07;

    public static final byte OP_SET_DEVICE_INFO = 0x08;
    public static final byte ATTR_SET_DEVICE_INFO_ANC = 0x04;

    public static final byte OP_NOTIFY_DEVICE_STATUS = 0x0E;
    public static final byte ATTR_NOTIFY_DEVICE_STATUS_BATTERY = 0x00;

    public static final byte OP_RESET_EQ = 0x11;
    public static final byte OP_SET_EQ = 0x12;
    public static final byte OP_GET_EQ = 0x13;
    public static final byte OP_SET_WDRC = 0x15;
    public static final byte OP_SN_WDRC = 0x01;
    public static final byte OP_GET_NOICE_CONTROL = 0x16;
    public static final byte OP_SET_NOICE_CONTROL = 0x17;
    public static final byte OP_SN_NOICE_CONTROL = 0x03;
    public static final byte OP_SET_MEASURE_HEART_RATE = 0x19;
    public static final byte OP_SN_SET_MEASURE_HEART_RATE = 0x05;
    public static final byte OP_SET_SETTING = 0x20;
    public static final byte OP_SN_SET_SETTING = 0x06;
    public static final byte OP_GET_SETTING = 0x21;
    public static final byte OP_SN_GET_SETTING = 0x07;
    public static final byte OP_GET_WEAR_STATE = 0x24;
    public static final byte OP_SN_WEAR_STATE = 0x010;
    public static final byte OP_SET_HEART_RATE_DETECT_AUTO = 0x22;
    public static final byte OP_SN_HEART_RATE_DETECT_AUTO = 0x08;
    public static final byte OP_SET_HEART_RATE_ACCURATELY = 0x23;
    public static final byte OP_SN_HEART_RATE_ACCURATELY = 0x09;
    public static final byte OP_GET_HEART_RATE_DETECT_AUTO = 0x35;
    public static final byte OP_SN_GET_HEART_RATE_DETECT_AUTO = 0x05;
    public static final byte OP_GET_HEART_RATE_ACCURATELY = 0x36;
    public static final byte OP_SN_GET_HEART_RATE_ACCURATELY = 0x06;

    public static final byte OP_UPDATE_FIRM_WARE = 0x41;
    public static final byte OP_SN_UPDATE_FIRM_WARE = 0x01;

    public static final byte OP_SN_MEASURE_HEART_RATE = 0x05;

    public static final byte STATUS_SUCCESS = 0;
    public static final byte STATUS_FAIL = 1;
    public static final byte STATUS_UNKOWN = 2;
    public static final byte STATUS_BUSY = 3;
    public static final byte STATUS_NO_RESPONSE = 4;
    public static final byte STATUS_CRC_ERROR = 5;
    public static final byte STATUS_ALL_DATA_CRC_ERROR = 6;
    public static final byte STATUS_PARAM_ERROR = 7;
    public static final byte STATUS_RESPONSE_DATA_OVER_LIMIT = 8;
    public static final byte STATUS_NOT_SUPPORT = 9;
    public static final byte STATUS_PARTIAL_OPERATION_FAILED = 10;
    public static final byte STATUS_UNREACHABLE = 11;
}
