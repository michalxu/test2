package com.goertek.rox2.ui.main.utils;

import com.goertek.bluetooth.protocol.ProtocolAPI;
import com.goertek.bluetooth.protocol.function.IResultListener;
import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.model.SendDataEvent;
import com.goertek.bluetooth.protocol.tws.TWSCommand;
import com.goertek.bluetooth.protocol.tws.TWSProtocol;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.ArrayList;
import java.util.List;

import static com.goertek.bluetooth.protocol.tws.TWSProtocol.getLength;

/**
 * 创建时间：2021/8/13
 *
 * @author michal.xu
 */
public class SendWdrcData {

    private int currentCount = 0;
    private int dataCount;
    private final int DATA_SIZE = 128;
    private int packageCount = 0;

    public SendWdrcData(byte[] data){
        this.data = data;
    }
    byte[] data;
    private final ProtocolAPI protocolAPI = ProtocolAPI.getDefault();

    public void sendData(WdrcDataListener listener){
        if (data!=null&&data.length==1084*4*2){
            byte[] buf = new byte[1084];
            dataCount = buf.length/DATA_SIZE +1;    //判断需要分帧多少帧
//            for (int i=0; i<8; i++){
//                System.arraycopy(data,i*1084,buf,0,1084);
//
//                currentCount = 0;
//                LogUtils.d("michal for i ="+i);
//
//            }
            System.arraycopy(data,packageCount*1084,buf,0,1084);
            send(packageCount, listener,buf);
        }

    }

    private void send(int i, WdrcDataListener listener,byte[] buf) {
        LogUtils.d("i  = "+i+"currentCount=="+currentCount);
        byte[] sendData;
        if (currentCount<dataCount-1){
            sendData = new byte[DATA_SIZE];
            System.arraycopy(buf,currentCount*DATA_SIZE,sendData,0,DATA_SIZE);
        }else{
            sendData = new byte[buf.length-DATA_SIZE*currentCount];
            System.arraycopy(buf,currentCount*DATA_SIZE,sendData,0,buf.length-DATA_SIZE*currentCount);
        }
        byte[] temData = TWSProtocol.setWdrcData(i, sendData,dataCount, currentCount);
        LogUtils.d("temData="+ProtocolUtils.bytesToHexStr(temData));
        LogUtils.d("参数  i="+(byte) temData[5]+"参数  dataCount="+(byte) temData[6]+"参数  currentCount="+(byte) temData[7]+"senddata.length="+sendData.length);
        protocolAPI.sendData(temData, 2000, new IResultListener() {
            @Override
            public void onSuccess(byte[] payload) {
                LogUtils.d("payload="+ProtocolUtils.bytesToHexStr(payload));
                if (payload[4]==0x00&&currentCount<dataCount-1){
                    currentCount++;
                    send(packageCount, listener, buf);
                }else if (payload[4]==0x00&&currentCount==dataCount-1&&i<8){
                    currentCount = 0;
                    packageCount++;
                    System.arraycopy(data,i*1084,buf,0,1084);
                    send(packageCount, listener, buf);
                }else {
                    listener.onFinish();
                }
            }

            @Override
            public void onFailed(int errorCode) {

            }
        });
    }
    private void waitForReceive(boolean wait){
        while (true);
    }

}
