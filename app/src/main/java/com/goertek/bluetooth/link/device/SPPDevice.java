package com.goertek.bluetooth.link.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.text.TextUtils;

import com.goertek.bluetooth.link.function.ConnectState;
import com.goertek.bluetooth.link.function.UUIDConfig;
import com.goertek.common.utils.BluetoothUtils;
import com.goertek.common.utils.LogUtils_goertek;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.rox2.ui.main.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件名：SPPDevice
 * 描述：封装了SPP链路的连接、通信方法
 * 构造只需要传入系统的BluetoothDevice模型
 * SPPDevice sppDevice = new SPPDevice(mDevice);
 * 创建人：jochen.zhang
 * 创建时间：2019/8/2
 */
public class SPPDevice extends BaseDevice {

    @Override
    public synchronized boolean connect(String address, UUIDConfig uuidConfig) {
        if (TextUtils.isEmpty(address)) {
            LogUtils_goertek.e("connect失败 address == null");
            return false;
        }
        if (uuidConfig == null) {
            LogUtils_goertek.e("connect失败 uuidConfig == null");
            return false;
        }
        if (address.equals(mac) && connectionState != ConnectState.STATE_DISCONNECTED) {
            LogUtils_goertek.w("connect失败 已存在连接");
            return false;
        }
        BluetoothDevice device = BluetoothUtils.getBluetoothAdapter().getRemoteDevice(address);
        if (device == null) {
            LogUtils_goertek.e("connect失败 device == null");
            return false;
        }
        disconnect();
        mac = address;
        mUUIDConfig = uuidConfig;
        if (connectionState == ConnectState.STATE_DISCONNECTED) {
            // Start the thread to connect with the given mDevice
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            return true;
        }
        return false;
    }

    @Override
    public synchronized void disconnect() {
        LogUtils_goertek.d("[" + mac + "] disconnect");
        clearConnection();
    }

    @Override
    public boolean write(byte[] data) {
        ConnectedThread r;
        //同步获取通信线程
        synchronized (this) {
            if (connectionState < ConnectState.STATE_CONNECTED) {
                return false;
            }
            r = mConnectedThread;
        }
        r.write(data);
        return true;
    }

    @Override
    public String toString() {
        return "[" + mac + "] SPP";
    }

    /***********************************************************************************************
     * SPP 连接实现
     **********************************************************************************************/
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    /**
     * 连接成功后开启通信线程
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        LogUtils_goertek.i("[" + mac + "] connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    /**
     * 清空连接相关线程，设置状态
     */
    private synchronized void clearConnection() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setConnectState(ConnectState.STATE_DISCONNECTED);
    }

    /**
     * 连接失败
     */
    private synchronized void connectionFailed() {
        LogUtils_goertek.d("[" + mac + "] connectionFailed");
        clearConnection();
    }

    /**
     * 连接丢失
     */
    private synchronized void connectionLost() {
        LogUtils_goertek.d("[" + mac + "] connectionLost");
        clearConnection();
    }


    /**
     * 连接线程
     * 尝试与设备建立SPP连接
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                LogUtils.d("SPP连接获取socekt");
                tmp = device.createRfcommSocketToServiceRecord(mUUIDConfig.getRfcommUUID());
            } catch (IOException e) {
                LogUtils_goertek.e("[" + mac + "] rfcomm create() failed", e);
            }
            mmSocket = tmp;
            setConnectState(ConnectState.STATE_CONNECTING);
        }

        @Override
        public void run() {
            LogUtils.d("SPP连接开始");
            LogUtils_goertek.i("[" + mac + "] BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            BluetoothUtils.getBluetoothAdapter().cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                LogUtils.d("socket连接");
                mmSocket.connect();
            } catch (Exception e) {
                // Close the socket
                LogUtils_goertek.e("[" + mac + "] unable to connect() socket during connection failure", e);
                try {
                    if (mmSocket != null) {
                        mmSocket.close();
                    }
                } catch (IOException e2) {
                    LogUtils_goertek.e("[" + mac + "] unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (SPPDevice.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                LogUtils_goertek.e("[" + mac + "] close() of connect socket failed", e);
            }
        }
    }

    /**
     * 通信线程
     * 在SPP连接成功后，开启通信线程
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            LogUtils_goertek.d("[" + mac + "] create ConnectedThread");
            setConnectState(ConnectState.STATE_CONNECTED);
            LogUtils.d("创建SPP连接完成后的通信线程");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                LogUtils_goertek.e("[" + mac + "] temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            setConnectState(ConnectState.STATE_DATA_READY);
        }

        @Override
        public synchronized void run() {
            LogUtils_goertek.i("[" + mac + "] BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int len;
            int available;

            // Keep listening to the InputStream while connected
            while (connectionState >= ConnectState.STATE_DATA_READY) {
                try {
                    // Read from the InputStream
                    len = mmInStream.read(buffer);
                    available = mmInStream.available();
                    if (len == buffer.length && available > 0) {
                        byte[] availableBytes = new byte[available];
                        mmInStream.read(availableBytes);
                        byte[] receiveBytes = ProtocolUtils.appen(buffer, availableBytes);

                        //log receive data
                        LogUtils_goertek.d("[" + mac + "] 接收 len: " + len + " available: " + available + " 数据: " + ProtocolUtils.bytesToHexStr(receiveBytes));
                        receive(receiveBytes);
                    } else if (len > 0) {
                        byte[] receiveBytes = new byte[len];
                        System.arraycopy(buffer, 0, receiveBytes, 0, len);

                        //log receive data
                        LogUtils_goertek.d("[" + mac + "] 接收 len: " + len + " 数据: " + ProtocolUtils.bytesToHexStr(receiveBytes));
                        receive(receiveBytes);
                    }
                } catch (IOException e) {
                    LogUtils_goertek.e("[" + mac + "] disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * 发送数据
         *
         * @param buffer 待发送数据
         */
        void write(byte[] buffer) {
            try {
                LogUtils.d("[" + mac + "] 发送 长度: " + buffer.length + " 数据: " + ProtocolUtils.bytesToHexStr(buffer));
                mmOutStream.write(buffer);
            } catch (Exception e) {
                LogUtils.e("[" + mac + "] Exception during write");
            }
        }

        void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                LogUtils_goertek.e("[" + mac + "] close() of connect socket failed", e);
            }
        }
    }

    /***********************************************************************************************
     * 获取Default使用
     **********************************************************************************************/
    public static SPPDevice getDefault() {
        return Singleton.SINGLETON.getSingleTon();
    }

    public enum Singleton {
        /** 枚举本身序列化之后返回的实例 */
        SINGLETON;
        private SPPDevice singleton;

        /** JVM保证只实例一次 */
        Singleton() {
            singleton = new SPPDevice();
        }

        /** 公布对外方法 */
        public SPPDevice getSingleTon() {
            return singleton;
        }
    }
}
