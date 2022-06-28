package com.goertek.bluetooth.protocol.tws;

import com.goertek.bluetooth.protocol.tws.entity.TWSBattery;
import com.goertek.bluetooth.protocol.tws.entity.TWSEQParams;
import com.goertek.bluetooth.protocol.tws.entity.TWSResult;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.Arrays;

public class TWSProtocol {
    private static final String TAG = "TWSProtocol";
    /** OpCode SN */
    public static byte sOpCodeSn = 0;
    /** Command other length(2) Control OpCode OpCodeSn */
    public static final int COMMAND_OTHER_LENGTH = 5;
    /** 最短长度 */
    public static final int MIN_LENGTH = 9;
    /** 1000 0000 */
    public static final byte MASK_TYPE = (byte) 0x80;
    /** 0100 0000 */
    public static final byte MASK_RESPONSE = (byte) 0x40;
    /** 0011 1111 */
    public static final byte MASK_UNUSED = (byte) 0x3F;
    /** DATA MASK */
    public static final byte[] MASK_DATA = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80};

    /**
     * Payload检测
     *
     * @param data [TWSCommand.SOF Payload TWSCommand.END]格式数据
     * @return true 符合协议
     */
    public static boolean checkPayload(byte[] data) {
        if (data.length < MIN_LENGTH) {
            LogUtils_goertek.e(TAG, "checkPayload error length < MIN_LENGTH " + ProtocolUtils.bytesToHexStr(data));
            return false;
        }
        int index = TWSCommand.SOF_LENGTH;
        byte control = data[index];
        if ((MASK_UNUSED & control) != 0) {
            LogUtils_goertek.e(TAG, "checkPayload error control " + ProtocolUtils.bytesToHexStr(data));
            return false;
        }
        index += 2;
        int length = ((data[index] & 0xff) << 8) + (data[index + 1] & 0xff);
        // TWSCommand.SOF + control + opCode + length(2) + data(length) + TWSCommand.END
        if (data.length != (TWSCommand.SOF_LENGTH + 4 + length + TWSCommand.END_LENGTH)) {
            LogUtils_goertek.e(TAG, "checkPayload error length " + length + " data " + ProtocolUtils.bytesToHexStr(data));
            return false;
        }
        return true;
    }

    /**
     * 从指令中拆解出payload
     *
     * @param command 完整指令
     * @return payload
     */
    public static byte[] getPayload(byte[] command) {
        int start = TWSCommand.SOF_LENGTH;
        int length = command.length - TWSCommand.SOF_LENGTH - TWSCommand.END_LENGTH;
        byte[] payload = new byte[length];
        System.arraycopy(command, start, payload, 0, length);
        return payload;
    }

    /**
     * 获取Control字段
     *
     * @param isCommand 是发送指令
     * @param needRespond 时都需要回复
     * @return control
     */
    private static byte getControl(boolean isCommand, boolean needRespond) {
        byte control = 0;
        if (isCommand) {
            control |= MASK_TYPE;
        }
        if (needRespond) {
            control |= MASK_RESPONSE;
        }
        return control;
    }

    /**
     * 长度转byte[]
     *
     * @param length 长度
     * @return byte[]
     */
    public static byte[] getLength(int length) {
        byte high = (byte) ((length >> 8) & 0xff);
        byte low = (byte) (length & 0xff);
        return new byte[]{high, low};
    }

    /**
     * 自增的OpCode SN
     *
     * @return byte
     */
    private static byte getOpCodeSn() {
        sOpCodeSn++;
        return sOpCodeSn;
    }

    /**
     * 链路层添加
     *
     * @param payload 发送payload
     * @return SOF + payload + END
     */
    private static byte[] addLink(byte[] payload) {
        byte[] cmd = new byte[TWSCommand.SOF_LENGTH + payload.length + TWSCommand.END_LENGTH];
        System.arraycopy(TWSCommand.SOF_BYTE, 0, cmd, 0, TWSCommand.SOF_LENGTH);
        System.arraycopy(payload, 0, cmd, TWSCommand.SOF_LENGTH, payload.length);
        System.arraycopy(TWSCommand.END_BYTE, 0, cmd, TWSCommand.SOF_LENGTH + payload.length, TWSCommand.END_LENGTH);
        LogUtils.d("addLink="+ProtocolUtils.bytesToHexStr(cmd));
        return cmd;
    }

    /**
     * ATTR转byte[]
     *
     * @param masks ATTR 从0开始
     * @return byte[]
     */
    private static byte[] getMask(int ... masks) {
        int mask = 0;
        for (int attr : masks) {
            mask |= 1 << attr;
        }
        return ProtocolUtils.getBytesFromIntLittle(mask);
    }

    public interface OnAttrDataCallback {
        void onAttr(int length, int attr, byte[] data);
        void onEnd();
    }

    private static void parseAttrData(byte[] payload, int startIndex, OnAttrDataCallback callback) {
        if (startIndex >= payload.length - 1) {
            callback.onEnd();
            return;
        }
        int start = startIndex;
        while (start <= payload.length - 2) {
            int length = payload[start] & 0xff;
            start++;
            int end = start + length;
            if (length != 0) {
                if (end > payload.length) {
                    LogUtils_goertek.e("TLV长度异常，缩短返回数据长度 end " + end + " payload.length " + payload.length);
                    length = length - (end - payload.length);
                    end = payload.length;
                }
                if (length == 1) {
                    callback.onAttr(length, payload[start], new byte[]{});
                } else {
                    callback.onAttr(length, payload[start], ProtocolUtils.subByte(payload, start + 1, length - 1));
                }
            } else {
                callback.onAttr(length, -1, new byte[]{});
            }
            start = end;
        }
        callback.onEnd();
    }

    private static byte[] getLevelBytes(int level) {
        if (level < 0) {
            level = ~(-level) + 1;
        }
        level = level << (5 * 4);
        return ProtocolUtils.getBytesFromInt(level);
    }

    private static int getLevel(byte[] levelBytes) {
        boolean isNegative = (levelBytes[0] & 0x80) != 0;
        int number = ((levelBytes[0] & 0xff) << 4) + ((levelBytes[1] >> 4) & 0x0f);
        if (isNegative) {
            return number - 0x00001000;
        } else {
            return number;
        }
    }

    public static void test() {
        int testInt1 = 4;
        byte[] testByte1 = getLevelBytes(testInt1);
        int testIntFinal1 = getLevel(testByte1);
        LogUtils_goertek.d(TAG, "testInt1 " + testInt1 + " testByte1 " + ProtocolUtils.bytesToHexStr(testByte1) + " testIntFinal1 " + testIntFinal1);


        int testInt2 = -4;
        byte[] testByte2 = getLevelBytes(testInt2);
        int testIntFinal2 = getLevel(testByte2);
        LogUtils_goertek.d(TAG, "testInt2 " + testInt2 + " testByte2 " + ProtocolUtils.bytesToHexStr(testByte2) + " testIntFinal2 " + testIntFinal2);

        int testInt3 = 0;
        byte[] testByte3 = getLevelBytes(testInt3);
        int testIntFinal3 = getLevel(testByte3);
        LogUtils_goertek.d(TAG, "testInt3 " + testInt3 + " testByte3 " + ProtocolUtils.bytesToHexStr(testByte3) + " testIntFinal3 " + testIntFinal3);

        int testInt4 = -1;
        byte[] testByte4 = getLevelBytes(testInt4);
        int testIntFinal4 = getLevel(testByte4);
        LogUtils_goertek.d(TAG, "testInt4 " + testInt4 + " testByte4 " + ProtocolUtils.bytesToHexStr(testByte4) + " testIntFinal4 " + testIntFinal4);

        byte[] mask1 = getMask(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
        LogUtils_goertek.d(TAG, "getMask(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17) " + ProtocolUtils.bytesToHexStr(mask1));

        byte[] mask2 = getMask(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        LogUtils_goertek.d(TAG, "getMask(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15) " + ProtocolUtils.bytesToHexStr(mask2));
    }
    /***********************************************************************************************
     * 协议实现
     **********************************************************************************************/
    /**
     * 获取Battery
     *
     * @return 完整指令
     */
    public static byte[] getBattery() {
        // mask
        byte[] mask = getMask(TWSCommand.ATTR_GET_DEVICE_INFO_MULT_BATTERY);
        // payload
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + mask.length];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_GET_DEVICE_INFO;
        // length SN + data.length
        byte[] length = getLength(mask.length + 1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = getOpCodeSn();
        // Data
        System.arraycopy(mask, 0, payload, 5, mask.length);
        LogUtils.d("获取battery命令："+ProtocolUtils.bytesToHexStr(addLink(payload)));
        return addLink(payload);
    }

    /**
     * 获取Battery结果
     *
     * @param payload 携带数据
     * @return TWSBattery
     */
    public static TWSBattery parseGetBattery(byte[] payload) {
        TWSBattery battery = new TWSBattery();
        // control
        byte control = payload[0];
        battery.setIsCommand((control & MASK_TYPE) != 0);
        battery.setNeedResponse((control & MASK_RESPONSE) != 0);
        // OpCode
        battery.setOpCode(payload[1]);
        // Status
        battery.setSuccess(payload[4] == TWSCommand.STATUS_SUCCESS);
        // OpCodeSn
        battery.setOpCodeSn(payload[5]);
        // Data
        parseAttrData(payload, 6, new OnAttrDataCallback() {
            @Override
            public void onAttr(int length, int attr, byte[] data) {
                switch (attr) {
                    case TWSCommand.ATTR_GET_DEVICE_INFO_MULT_BATTERY:
                        if (length == 3 + 1) {
                            battery.setLeft(data[0] & 0x7f);
                            battery.setRight(data[1] & 0x7f);
                            battery.setBox(data[2] & 0x7f);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onEnd() {}
        });
        return battery;
    }

    /**
     * 设置ANC
     *
     * @param status ANC状态
     * @return 完整指令
     */
    public static byte[] setAnc(int status) {
        byte[] data = {0x02, TWSCommand.ATTR_SET_DEVICE_INFO_ANC, (byte) status};
        // payload
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + data.length];
        // control
        payload[0] = getControl(true, true);

        // OpCode
        payload[1] = TWSCommand.OP_SET_DEVICE_INFO;
        // length SN + data.length
        byte[] length = getLength(data.length + 1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = getOpCodeSn();
        // Data
        System.arraycopy(data, 0, payload, 5, data.length);
        return addLink(payload);
    }

    /**
     * 解析设置ANC结果
     *
     * @param payload 携带数据
     * @return TWSResult
     */
    public static TWSResult parseSetAnc(byte[] payload) {
        TWSResult result = new TWSResult();
        // control
        byte control = payload[0];
        result.setIsCommand((control & MASK_TYPE) != 0);
        result.setNeedResponse((control & MASK_RESPONSE) != 0);
        // OpCode
        result.setOpCode(payload[1]);
        // Status
        result.setSuccess(payload[4] == TWSCommand.STATUS_SUCCESS);
        // OpCodeSn
        result.setOpCodeSn(payload[5]);
        return result;
    }

    /**
     * 主动上报Battery结果
     *
     * @param payload 携带数据
     * @return TWSBattery
     */
    public static TWSBattery parseNotifyBattery(byte[] payload) {
        TWSBattery battery = new TWSBattery();
        // control
        byte control = payload[0];
        battery.setIsCommand((control & MASK_TYPE) != 0);
        battery.setNeedResponse((control & MASK_RESPONSE) != 0);
        // OpCode
        battery.setOpCode(payload[1]);
        // Status
        battery.setSuccess(payload[4] == TWSCommand.STATUS_SUCCESS);
        // OpCodeSn
        battery.setOpCodeSn(payload[5]);
        // Data
        parseAttrData(payload, 5, new OnAttrDataCallback() {
            @Override
            public void onAttr(int length, int attr, byte[] data) {
                switch (attr) {
                    case TWSCommand.ATTR_NOTIFY_DEVICE_STATUS_BATTERY:
                        if (length == 3 + 1) {
                            battery.setLeft(data[0] & 0x7f);
                            battery.setRight(data[1] & 0x7f);
                            battery.setBox(data[2] & 0x7f);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onEnd() {}
        });
        return battery;
    }

    /**
     * 重置EQ
     *
     * @return 完整指令
     */
    public static byte[] resetEq() {
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + 1];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_RESET_EQ;
        // length SN + data.length
        byte[] length = getLength(2);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = getOpCodeSn();
        // Data
        payload[5] = 0x01;
        return addLink(payload);
    }

    /**
     * 解析重设EQ结果
     *
     * @param payload 携带数据
     * @return TWSResult
     */
    public static TWSResult parseResetEq(byte[] payload) {
        TWSResult result = new TWSResult();
        // control
        byte control = payload[0];
        result.setIsCommand((control & MASK_TYPE) != 0);
        result.setNeedResponse((control & MASK_RESPONSE) != 0);
        // OpCode
        result.setOpCode(payload[1]);
        // Status
        result.setSuccess(payload[4] == TWSCommand.STATUS_SUCCESS);
        // OpCodeSn
        result.setOpCodeSn(payload[5]);
        return result;
    }

    /**
     * 设置EQ
     *
     * @param params EQ参数
     * @return 完整指令
     */
    public static byte[] setEq(TWSEQParams params) {
        // 获取等级数据
        int[] levels = params.getParams();
        byte[] data = new byte[levels.length * 6];
        for (int i = 0; i < levels.length; i++) {
            byte[] levelBytes = getLevelBytes(levels[i]);
            data[i * 6] = 5;
            data[i * 6 + 1] = (byte) i;
            System.arraycopy(levelBytes, 0, data, i * 6 + 2, levelBytes.length);
        }
        // 数据包
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + data.length];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_SET_EQ;
        // length SN + data.length
        byte[] length = getLength(data.length + 1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = getOpCodeSn();
        // Data
        System.arraycopy(data, 0, payload, 5, data.length);
        return addLink(payload);
    }

    /**
     * 设置EQ
     *
     * @param levels EQ参数
     * @return 完整指令
     */
    public static byte[] setEq(int[] levels) {
        // 获取等级数据
//        int[] levels = params.getParams();
        byte[] data = new byte[levels.length * 6];
        for (int i = 0; i < levels.length; i++) {
            byte[] levelBytes = getLevelBytes(levels[i]);
            LogUtils.d("levelBytes="+ProtocolUtils.bytesToHexStr(levelBytes)+"levelBytes int ="+levels[i]);
            data[i * 6] = 5;
            data[i * 6 + 1] = (byte) i;
            System.arraycopy(levelBytes, 0, data, i * 6 + 2, levelBytes.length);
            LogUtils.d("levelBytes data="+ProtocolUtils.bytesToHexStr(data));
        }
        // 数据包
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + data.length];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_SET_EQ;
        // length SN + data.length
        byte[] length = getLength(data.length + 1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = getOpCodeSn();
        // Data
        System.arraycopy(data, 0, payload, 5, data.length);
        return addLink(payload);
//        // 数据包
//        byte[] payload = new byte[COMMAND_OTHER_LENGTH + data.length];
//        // control
//        payload[0] = getControl(true, true);
//        // OpCode
//        payload[1] = TWSCommand.OP_SET_EQ;
//        // length SN + data.length
//        byte[] length = getLength(data.length + 1);
//        payload[2] = length[0];
//        payload[3] = length[1];
//        // OpCode SN
//        payload[4] = getOpCodeSn();
//        // Data
//        System.arraycopy(data, 0, payload, 5, data.length);
//        return addLink(payload);
    }
    /**
     * 设置EQ结果
     *
     * @param payload 携带数据
     * @return TWSResult
     */
    public static TWSResult parseSetEq(byte[] payload) {
        TWSResult result = new TWSResult();
        // control
        byte control = payload[0];
        result.setIsCommand((control & MASK_TYPE) != 0);
        result.setNeedResponse((control & MASK_RESPONSE) != 0);
        // OpCode
        result.setOpCode(payload[1]);
        // Status
        result.setSuccess(payload[4] == TWSCommand.STATUS_SUCCESS);
        // OpCodeSn
        result.setOpCodeSn(payload[5]);
        return result;
    }

    /**
     * 获取EQ
     *
     * @return 完整指令
     */
    public static byte[] getEq() {
        // mask
        byte[] mask = getMask(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
        // payload
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + mask.length];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_GET_EQ;
        // length SN + data.length
        byte[] length = getLength(mask.length + 1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = getOpCodeSn();
        // Data
        System.arraycopy(mask, 0, payload, 5, mask.length);
        return addLink(payload);
    }

    /**
     * 获取EQ结果
     *
     * @param payload 携带数据
     * @return TWSEQParams
     */
    public static TWSEQParams parseGetEq(byte[] payload) {
        TWSEQParams params = new TWSEQParams();
        // control
        byte control = payload[0];
        params.setIsCommand((control & MASK_TYPE) != 0);
        params.setNeedResponse((control & MASK_RESPONSE) != 0);
        // OpCode
        params.setOpCode(payload[1]);
        // Status
        params.setSuccess(payload[4] == TWSCommand.STATUS_SUCCESS);
        // OpCodeSn
        params.setOpCodeSn(payload[5]);
        // Data
        parseAttrData(payload, 6, new OnAttrDataCallback() {
            @Override
            public void onAttr(int length, int attr, byte[] data) {
                params.setParam(attr, getLevel(data));
            }

            @Override
            public void onEnd() {}
        });
        return params;
    }


    public static byte[] setWdrcData(int packageCount, byte[] wdrcData,int totalCount, int currentCount){
        // 获取模式数据
        // 数据包
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + wdrcData.length+3];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_SET_WDRC;
        payload[2] = (byte) packageCount;
        payload[3] = (byte) totalCount;
        payload[4] = (byte) currentCount;
        // length SN + data.length
        byte[] length = getLength(wdrcData.length + 1);
        payload[5] = length[0];
        payload[6] = length[1];
        // OpCode SN
        payload[7] = TWSCommand.OP_SN_WDRC;
        // Data
        System.arraycopy(wdrcData, 0, payload, 8, wdrcData.length);
        return addLink(payload);
    }



    public static byte[] setNoiceControl(byte[] data){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH+data.length];
        payload[0] = getControl(true,true);
        payload[1] = TWSCommand.OP_SET_NOICE_CONTROL;
        byte[] length = getLength(data.length+1);
        //length.SN+data.length
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_NOICE_CONTROL;
        //data
        System.arraycopy(data,0,payload,5,data.length);
        return addLink(payload);
    }

    public static byte[] getNoiceControl(){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH];
        //control
        payload[0] = getControl(false,true);
        //opCode
        payload[1] = TWSCommand.OP_GET_NOICE_CONTROL;
        //length
        byte[] length = getLength(1);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = getOpCodeSn();
        return addLink(payload);
    }
    /**
     *
     //     * @param  true: measure heart rate
     *             false:stop measure heart rate
     * @return
     */
    public static byte[] setHeartRateOnMeasure(){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_SET_MEASURE_HEART_RATE;
        // length SN + data.length
        byte[] length = getLength(1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = TWSCommand.OP_SN_SET_MEASURE_HEART_RATE;
        return addLink(payload);
    }

    public static byte[] setSettings(byte[] data){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH+data.length];
        //control
        payload[0] = getControl(true,true);
        //opCode
        payload[1] = TWSCommand.OP_SET_SETTING;
        //length
        byte[] length = getLength(data.length+1);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_SET_SETTING;
        //data
        System.arraycopy(data,0,payload,5,data.length);
//        addLink(payload);
        return addLink(payload);
    }

    public static byte[] getSettings(byte data){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH+1];
        payload[0] = getControl(false,true);
        payload[1] = TWSCommand.OP_GET_SETTING;
        //length
        byte[] length = getLength(2);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_GET_SETTING;
        payload[5] = data;
//        addLink(payload);
//        LogUtils.d("addLink="+ProtocolUtils.bytesToHexStr(payload));
        return addLink(payload);
    }

    public static byte[] getWearState(){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH];
        payload[0] = getControl(false,true);
        payload[1] = TWSCommand.OP_GET_WEAR_STATE;
        //length
        byte[] length = getLength(1);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_WEAR_STATE;
//        addLink(payload);
        return addLink(payload);
    }

    public static byte[] setHeartRateDetectAuto(boolean isOn){
        byte data ;
        if (isOn){
            data = 0x01;
        }else {
            data = 0x00;
        }
        byte[] payload = new byte[COMMAND_OTHER_LENGTH+1];
        payload[0] = getControl(true,true);
        payload[1] = TWSCommand.OP_SET_HEART_RATE_DETECT_AUTO;
        //length
        byte[] length = getLength(2);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_HEART_RATE_DETECT_AUTO;
        payload[5] = data;
//        addLink(payload);
        return addLink(payload);
    }

    public static byte[] setHeartRateDetectAccurately(boolean isOn){
        byte data ;
        if (isOn){
            data = 0x01;
        }else {
            data = 0x00;
        }
        byte[] payload = new byte[COMMAND_OTHER_LENGTH+1];
        payload[0] = getControl(true,true);
        payload[1] = TWSCommand.OP_SET_HEART_RATE_ACCURATELY;
        //length
        byte[] length = getLength(2);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_HEART_RATE_ACCURATELY;
        payload[5] = data;
//        addLink(payload);
        return addLink(payload);
    }

    public static byte[] getHeartRateDetectAutoStatus(){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH];
        //control
        payload[0] = getControl(false,true);
        //opCode
        payload[1] = TWSCommand.OP_GET_HEART_RATE_DETECT_AUTO;
        //length
        byte[] length = getLength(1);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_GET_HEART_RATE_DETECT_AUTO;
        return addLink(payload);
    }

    public static byte[] getHeartRateDetectAccurately(){
        byte[] payload = new byte[COMMAND_OTHER_LENGTH];
        //control
        payload[0] = getControl(false,true);
        //opCode
        payload[1] = TWSCommand.OP_GET_HEART_RATE_ACCURATELY;
        //length
        byte[] length = getLength(1);
        payload[2] = length[0];
        payload[3] = length[1];
        //opCode SN
        payload[4] = TWSCommand.OP_SN_HEART_RATE_ACCURATELY;
        return addLink(payload);
    }

    public static byte[] setAudition(byte[] data) {
        // 数据包
        byte[] payload = new byte[COMMAND_OTHER_LENGTH + data.length];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_SET_EQ;
        // length SN + data.length
        byte[] length = getLength(data.length + 1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = getOpCodeSn();
        // Data
        System.arraycopy(data, 0, payload, 5, data.length);
        return addLink(payload);
    }

    public static byte[] updateFirmWare(){
        // 数据包
        byte[] payload = new byte[COMMAND_OTHER_LENGTH];
        // control
        payload[0] = getControl(true, true);
        // OpCode
        payload[1] = TWSCommand.OP_UPDATE_FIRM_WARE;
        // length SN + data.length
        byte[] length = getLength(1);
        payload[2] = length[0];
        payload[3] = length[1];
        // OpCode SN
        payload[4] = TWSCommand.OP_SN_UPDATE_FIRM_WARE;
        return addLink(payload);
    }
}
