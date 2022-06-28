package com.goertek.common.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件名 FileUtils.java
 * 描述 文件处理工具类
 * @author jochen.zhang
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    public static final String APP_PATH = "TWSAPP";

    private static final String APP_FILE_DIR = getSDCard() + File.separator + APP_PATH + File.separator;
    private static final String APP_CACHE_DIR = APP_FILE_DIR + "cache/";
    private static final String APP_PHOTO_DIR = APP_FILE_DIR + "photo/";
    private static final String APP_OBJECT_DIR = APP_CACHE_DIR + "object/";
    private static final String APP_CRASH_DIR = APP_FILE_DIR + "crash/";
    private static final String DEVICE_LOG_DIR = APP_FILE_DIR + "deviceLog/";
    private static final String APP_LOG_DIR = APP_FILE_DIR + "log/";
    private static final String APP_BLUETOOTH_LOG_DIR = APP_FILE_DIR + "bluetoothLog/";
    private static final String APP_TEMPERATURE_LOG_DIR = APP_FILE_DIR + "temperatureLog/";
    private static final String APP_MONKEY_DIR = APP_FILE_DIR + "monkeyTest/";
    private static final String SD_DIR = APP_FILE_DIR + "appLog/";
    private static final String OUTPUT_DIR = getSDCard() + File.separator + "DFTOutput/";
    private static final String SENSOR_DIR = APP_FILE_DIR + "sensor/";
    public static final String DEVICE_MBB_LOG_DIR = APP_FILE_DIR + "MBBLog/";

    private static final String[] FILE_UNIT = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "BB", "NB", "DB", "CB", "XB"};
    private static final double FILE_CARRY = 1024.0;

    /**
     * 扫描path下的子文件
     *
     * @param path the specified path.
     * @return String[] Array of strings, one for each file/folder. May be null.
     */
    public static String[] fileList(String path) {
        try {
            return new File(path).list();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * 判断是文件/文件夹
     *
     * @param filePath file in the absolute path.
     * @return true, other wise is false.
     */
    public static boolean isFile(String filePath) {
        return getInputStream(filePath) != null;
    }

    /**
     * 判断文件/文件夹是否存在
     *
     * @param filePath file in the absolute path.
     * @return true, other wise is false.
     */
    public static boolean exists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 创建文件件
     *
     * @param folderPath file in the absolute path.
     * @return true, other wise is false.
     */
    public static boolean mkdirs(String folderPath) {
        File file = new File(folderPath);
        return file.exists() || file.mkdirs();
    }

    /**
     * 获取文件输入流.
     *
     * @param filePath file in the absolute path.
     * @return {@link InputStream} or null.
     */
    public static InputStream getInputStream(String filePath) {
        try {
            return new FileInputStream(new File(filePath));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * 删除文件
     *
     * @param filePath the specified path.
     * @return success/fail
     */
    public static boolean delete(String filePath) {
        try {
            // 找到文件所在的路径并删除该文件
            File file = new File(Environment.getExternalStorageDirectory(), filePath);
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除文件夹（包含子文件）
     *
     * @param dir the specified path.
     * @return success/fail
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return true;
            }
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 转换Byte为文件大小字符串
     *
     * @param fileSize 文件大小
     * @return 文件大小字符串
     */
    public static String getFileSizeFromByte(double fileSize) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(2);

        int unitIndex = 0;
        // 目前单位有14个，可以进13次位
        for (int i = 1; i < FILE_UNIT.length; i++) {
            if (fileSize < FILE_CARRY) {
                // 不需要进位
                break;
            } else {
                // 进位
                fileSize /= FILE_CARRY;
                unitIndex++;
            }
        }

        return df.format(fileSize) + " " + FILE_UNIT[unitIndex];
    }

    /**
     * 获取APP_FILE_DIR路径，若文件夹不存在则创建文件夹
     *
     * @return 路径
     */
    public static final String getAppFileDir() {
        File file = new File(APP_FILE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return APP_FILE_DIR;
    }

    /**
     * 获取APP_CACHE_DIR路径，若文件夹不存在则创建文件夹
     *
     * @return 路径
     */
    public static final String getAppCacheDir() {
        File file = new File(APP_CACHE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return APP_CACHE_DIR;
    }

    /**
     * 获取APP_PHOTO_DIR路径，若文件夹不存在则创建文件夹
     *
     * @return 路径
     */
    public static final String getAppPhotoDir() {
        File file = new File(APP_PHOTO_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return APP_PHOTO_DIR;
    }

    /**
     * 获取APP_OBJECT_DIR路径，若文件夹不存在则创建文件夹
     *
     * @return 路径
     */
    public static final String getAppObjectDir() {
        File file = new File(APP_OBJECT_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return APP_OBJECT_DIR;
    }

    /**
     * 获取app崩溃路径，若文件夹不存在则创建文件夹
     *
     * @return 路径
     */
    public static String getAppCrashDir() {
        File file = new File(APP_CRASH_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return APP_CRASH_DIR;
    }

    /**
     * 获取设备log文件路径，若文件夹不存在则创建文件夹
     *
     * @return 路径
     */
    public static String getDeviceLogDir() {
        File file = new File(DEVICE_LOG_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return DEVICE_LOG_DIR;
    }

    public static String getAppLogDir() {
        File file = new File(APP_LOG_DIR);
        if (!file.exists()) {
            boolean bFlag = file.mkdirs();
            if (bFlag) {
                LogUtils_goertek.d("getAppLogDir", "mkdirs failed path == " + APP_LOG_DIR);
            }
        }
        return APP_LOG_DIR;
    }

    @NonNull
    public static String getSDCardDir() {
        File file = new File(SD_DIR);
        if (!file.exists()) {
            boolean bFlag = file.mkdirs();
            if (!bFlag) {
                LogUtils_goertek.d("getSDCardDir", "mkdirs failed path == " + SD_DIR);
            }
        }
        return SD_DIR;
    }

    @NonNull
    public static String getLogDir() {
        boolean bSDlog = true;
        if (bSDlog) {
            return getSDCardDir();
        } else {
            return getAppLogDir();
        }
    }

    @NonNull
    public static String getSensorDir() {
        File file = new File(SENSOR_DIR);
        if (!file.exists()) {
            boolean bFlag = file.mkdirs();
            if (!bFlag) {
                LogUtils_goertek.d("getSensorDir", "mkdirs failed path == " + SENSOR_DIR);
            }
        }
        return SENSOR_DIR;
    }

    public static String getBluetoothLogDir() {
        File file = new File(APP_BLUETOOTH_LOG_DIR);
        if (!file.exists()) {
            boolean bFlag = file.mkdirs();
            if (!bFlag) {
                LogUtils_goertek.d("getBluetoothLogDir", "mkdirs failed path == " + APP_BLUETOOTH_LOG_DIR);
            }
        }
        return APP_BLUETOOTH_LOG_DIR;
    }

    public static String getTemperatureLogDir() {
        File file = new File(APP_TEMPERATURE_LOG_DIR);
        if (!file.exists()) {
            boolean bFlag = file.mkdirs();
            if (!bFlag) {
                LogUtils_goertek.d("getTemperatureLogDir", "mkdirs failed path == " + APP_TEMPERATURE_LOG_DIR);
            }
        }
        return APP_TEMPERATURE_LOG_DIR;
    }

    public static String getMonkeyTestDir() {
        if (!mkdirs(APP_MONKEY_DIR)) {
            LogUtils_goertek.d("getMonkeyTestDir", "mkdirs failed path == " + APP_MONKEY_DIR);
        }
        return APP_MONKEY_DIR;
    }

    public static String getOutputDir(String folder) {
        String path = OUTPUT_DIR + folder + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            boolean bFlag = file.mkdirs();
            if (!bFlag) {
                LogUtils_goertek.d("getOutputDir", "mkdirs failed path == " + path);
            }
        }
        return path;
    }

    public static String getMbbLogDir(String folder) {
        String path = DEVICE_MBB_LOG_DIR + folder + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            boolean bFlag = file.mkdirs();
            if (!bFlag) {
                LogUtils_goertek.d("getOutputDir", "mkdirs failed path == " + path);
            }
        }
        return path;
    }

    /**
     * 写数据到文件
     *
     * @param filePath 文件路径
     * @param data     数据
     * @param append   true 拼接在文件最后; false 从头写
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void writeDataToFile(String filePath, byte[] data, boolean append) {
        try (FileOutputStream fos = new FileOutputStream(filePath, append)) {
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param file   文件
     * @param data   将写入信息
     * @param append true 拼接在文件最后; false 从头写
     */
    public static void writeStringToFile(File file, String data, boolean append) {
        try (FileWriter fw = new FileWriter(file, append)) {
            writeStringToFile(fw, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param filePath 文件路径
     * @param data     将写入信息
     * @param append   true 拼接在文件最后; false 从头写
     */
    public static void writeStringToFile(String filePath, String data, boolean append) {
//        try (FileWriter fw = new FileWriter(filePath, append)) {
//            writeStringToFile(fw, data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 将字符串写入文件
     *
     * @param fileWriter FileWriter
     * @param data       字符串
     */
    private static void writeStringToFile(FileWriter fileWriter, String data) {
        try (BufferedWriter bw = new BufferedWriter(fileWriter)) {
            bw.append(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件读取字符串
     *
     * @param filePath 文件路径
     * @return ArrayList<String>
     */
    public static ArrayList<String> readStringFromFile(String filePath) {
        ArrayList<String> addressList = new ArrayList<>();
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists() || !new File(filePath).isFile()) {
            ToastUtils.showShortToast("无效的文件路径" + '\n');
            return addressList;
        }
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);
            String str;
            String strAdd;
            while (br.ready()) {
                // 读取一行
                str = br.readLine();
                if (!TextUtils.isEmpty(str)) {
                    str = str.trim();
                    strAdd = "";
                    if (str.length() == 12) {
                        for (int i = 0; i < 10; i += 2) {
                            strAdd += (str.substring(i, i + 2) + ":");
                        }
                        strAdd += str.substring(10, 10 + 2);
                        addressList.add(strAdd);
                    }
                }
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addressList;
    }

    /**
     * 判断是否存在SDCard
     *
     * @return 如果存在返回true
     */
    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable();
    }

    /**
     * 尝试获取SDCard的路径
     *
     * @return 如果SDCard存在返回其路径, 如果不存在返回""
     */
    @NonNull
    public static String getSDCard() {
        if (existSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return "";
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     * <code>false</code> otherwise
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            File oldFile = new File(oldPath);
            if (!oldFile.exists()) {
                LogUtils_goertek.e(TAG, "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                LogUtils_goertek.e(TAG, "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                LogUtils_goertek.e(TAG, "copyFile:  oldFile cannot read.");
                return false;
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制文件夹及其中的文件
     *
     * @param oldPath String 原文件夹路径 如：data/user/0/com.test/files
     * @param newPath String 复制后的路径 如：data/user/0/com.test/cache
     * @return <code>true</code> if and only if the directory and files were copied;
     * <code>false</code> otherwise
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    LogUtils_goertek.e(TAG, "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {
                    //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 压缩文件和文件夹
     *
     * @param srcFileString 要压缩的文件或文件夹
     * @param zipFileString 压缩完成的Zip路径
     */
    public static void zipFolder(String srcFileString, String zipFileString) throws Exception {
        //创建ZIP
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));
        //创建文件
        File file = new File(srcFileString);
        //压缩
        zipFiles(file.getParent() + File.separator, file.getName(), outZip);
        //完成和关闭
        outZip.finish();
        outZip.close();
    }

    /**
     * 压缩文件
     *
     * @param folderString   文件夹路径
     * @param fileString     文件名
     * @param zipOutputSteam Zip输出流
     */
    private static void zipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam) throws Exception {
        if (zipOutputSteam == null) {
            return;
        }
        File file = new File(folderString + fileString);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }
            zipOutputSteam.closeEntry();
        } else {
            //文件夹
            String[] fileList = file.list();
            //没有子文件和压缩
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //子文件和递归
            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderString, fileString + File.separator + fileList[i], zipOutputSteam);
            }
        }
    }

    /**
     * 打开文本文件
     *
     * @param context 上下文
     * @param path    文件路径
     */
    public static void openTxt(Context context, String path) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                // 大于7.0使用此方法
                uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileProvider", new File(path));
            } else {
                // 小于7.0就简单了
                uri = Uri.fromFile(new File(path));
            }
            intent.setDataAndType(uri, "text/plain");
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtils.showLongToastSafe("打开文档失败");
        }
    }
}
