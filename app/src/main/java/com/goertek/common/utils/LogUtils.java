package com.goertek.common.utils;

import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件名 LogUtils.java
 * 描述 日志工具类
 * @author jochen.zhang
 */
public class LogUtils {
  /** 日志TAG */
  private static final String TAG = LogUtils.class.getSimpleName();

  /** 如果是调试模式（true）, 强制开启日志 */
 // private static boolean DEBUG = BuildConfig.DEBUG;

  private static boolean DEBUG = true;

  /** 日志文件的输出流对象 */
  private static BufferedWriter bw = null;

  /** 日志线程对象 */
  private static LogThread logThread = null;

  /** 允许缓存的最大日志条数(如sd卡未准备好) */
  private static final int MAX_CACHE_SIZE = 128;

  /** log文件允许的最大大小 , 当前为3M */
  private static final int LOG_FILE_MAX_SIZE = 3 * 1024 * 1024;

  /** 待记录到文件的日志队列, 需要支持同步 */
  private static Queue<String> queLogs = new ConcurrentLinkedQueue<String>();

  /** 日志级别与其对应的字符标签 */
  private static SparseArray<String> degreeLabel = new SparseArray<String>();

  static String logFile;

  /** 是否需要记录日志到文件 */
  private static boolean IS_NEED_FILELOG = true;

  /** 是否需要过滤字符串 */
  //private static boolean IS_NEED_FILTERLOG = !BuildConfig.DEBUG;

  private static boolean IS_NEED_FILTERLOG = false;

  /** 版本号过滤 */
  private static Pattern sVersionPattern = Pattern.compile("V[0-9]{5}");

  private static OutputStreamWriter outputStreamWriter;

  private static FileOutputStream fileOutputStream;

  public static boolean isFIJI = false;

  private static final String[] PATTERN_LIST = {
    "\"sn\":\\\"([^\\\"]*)\\\"|\"devId\":\\\"([^\\\"]*)\\\"|\"latitude\":([^\\\"]*)|\"longitude\":([^\\\"]*)|\"altitude\":([^\\\"]*)|\"lat\":([^\\\"]*)|\"lng\":([^\\\"]*)|\"smsInfo\": \\{([\\s\\S]*?)\\}\\n|\"mailInfo\": \\{([\\s\\S]*?)\\}\\n|Nickname:([^\\\"]*)|\"name\":([^,]*)|Name\":([^,}]*)|addreines=([^\\]]*)|\"countryCode\":([^,]*)",
    "(?i)(Track|huawei.com|login|监控|hide|登录|private information|personal|non-public|Privacy|compile|tracking|SNMP|SNMPV1|SNMPV2|SSH1|SSHv1|SSL|SSL2.0|SSL3.0|TLS|TLSv1|Telnet)",
    "(?i)(Call history|development plan|Customer name|GPS|Phone|SMS|Location|address|communication content|contacts|customer data|customer information|email|latitude|longitude|altitude|online data|online user scale)",
    "(?i)(manager|master|wwwrun|gaussdba|token|authenticationcode|private_key|privatekey|secret_key|secretkey|sharecode|sharekey|verifycode|guest|key_|_key|keyword)[ ]{0,10}[=:]",
    "(?i)(ftp://|Https://|Http://|www\\.)",
    "\\b((25[0-5]|2[0-4][0-9]|1\\d{2}|1\\d|[2-9]\\d|\\d)(\\.(25[0-5]|2[0-4][0-9]|1\\d{2}|1\\d|[2-9]\\d|\\d)){3})\\b",
    "\\b(?<![0-9\\.])(1[01][0-9]|12[0-6]|1[1-9]|[2-9][0-9]|[1-9])(\\.(25[0-5]|2[0-4][0-9]|1\\d{2}|1\\d|[2-9]\\d|\\d)){3}(?![0-9\\.])\\b",
    "1[3|4|5|7|8][0-9]{9}|14[5|7][0-9]{8}|17[0|7][0-9]{8}",
    "[A-Za-z\\d]{8}(-[A-Za-z\\d]{4}){3}-[A-Za-z\\d]{12}",
    "(?i)((administrator|commonuser|hardcoding|passwd|pswd|passcode|root|master|visitor|admin|anonymous|appuser|analyzer|account|commonuser|user|encode|operator)[ ]{0,10}[=:])",
    "(([a-zA-Z]00[1-9][0-9]{5})|([a-zA-Z](WX|wx)[1-9][0-9]{5})|([a-zA-Z]*kf[1-9][0-9]{4,5})|\\b([a-zA-Z][1-9][0-9]{4})\\b)",
    "([a-fA-F0-9][a-fA-F0-9](:|-)){5}[a-fA-F0-9][a-fA-F0-9]",
    "(职务|公司|姓名|客户名称|客户信息|工号|手机|邮编|地址|单位|法人代表|电话|住址|电话号码|传真)",
    "([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)"
  };

    /** 初始化日志级别与其对应的字符标签 */
  static {
    degreeLabel.put(Log.VERBOSE, "V");
    degreeLabel.put(Log.DEBUG, "D");
    degreeLabel.put(Log.INFO, "I");
    degreeLabel.put(Log.WARN, "W");
    degreeLabel.put(Log.ERROR, "E");
  }

  private static boolean needPrint() {
    return (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) || IS_NEED_FILELOG;
  }
  /**
   * 记录VERBOSE级别日志 在记录VERBOSE级别日志时调用, 如果日志配置为不记录日志或日志级别高于VERBOSE, 不记录日志。
   *
   * @param tag 日志tag
   * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
   */
  public static void v(String tag, String... msg) {
    if (needPrint()) {
      String msgStr = combineLogMsg(msg);
      if (IS_NEED_FILTERLOG) {
        tag = filterString(tag);
        msgStr = filterString(msgStr);
      }
      if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
        // 控制台日志
        Log.v(TAG, "[" + tag + "]" + msgStr);
      }
      if (IS_NEED_FILELOG) {
        // 打印文件到手机里面
        writeLogToFile(Log.VERBOSE, tag, msgStr, null);
      }
    }
  }

  /**
   * 记录DEBUG级别日志 在记录DEBUG级别日志时调用, 如果日志配置为不记录日志或日志级别高于DEBUG, 不记录日志。
   *
   * @param tag 日志tag
   * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
   */
  public static void d(String tag, String... msg) {
    if (needPrint()) {
      String msgStr = combineLogMsg(msg);
      if (IS_NEED_FILTERLOG) {
        tag = filterString(tag);
        msgStr = filterString(msgStr);
      }
      if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
        // 控制台日志
        Log.d(TAG, "[" + tag + "]" + msgStr);
      }
      if (IS_NEED_FILELOG) {
        // 打印文件到手机里面
        writeLogToFile(Log.DEBUG, tag, msgStr, null);
      }
    }
  }

  /**
   * 记录INFO级别日志 在记录INFO级别日志时调用, 如果日志配置为不记录日志或日志级别高于INFO, 不记录日志。
   *
   * @param tag 日志tag
   * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
   */
  public static void i(String tag, String... msg) {
    if (needPrint()) {
      String msgStr = combineLogMsg(msg);
      if (IS_NEED_FILTERLOG) {
        tag = filterString(tag);
        msgStr = filterString(msgStr);
      }
      if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
        // 控制台日志
        Log.i(TAG, "[" + tag + "]" + msgStr);
      }
      if (IS_NEED_FILELOG) {
        // 打印文件到手机里面
        writeLogToFile(Log.INFO, tag, msgStr, null);
      }
    }
  }

  /**
   * 记录WARN级别日志 在记录W级别日志时调用, 如果日志配置为不记录日志或日志级别高于WARN, 不记录日志。
   *
   * @param tag 日志tag
   * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
   */
  public static void w(String tag, String... msg) {
    if (needPrint()) {
      String msgStr = combineLogMsg(msg);
      if (IS_NEED_FILTERLOG) {
        tag = filterString(tag);
        msgStr = filterString(msgStr);
      }
      if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
        // 控制台日志
        Log.w(TAG, "[" + tag + "]" + msgStr);
      }
      if (IS_NEED_FILELOG) {
        // 打印文件到手机里面
        writeLogToFile(Log.WARN, tag, msgStr, null);
      }
    }
  }

  /**
   * 记录ERROR级别日志 在记录ERROR级别日志时调用, 如果日志配置为不记录日志或日志级别高于ERROR, 不记录日志。
   *
   * @param tag 日志tag
   * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
   */
  public static void e(String tag, String... msg) {
    if (needPrint()) {
      String msgStr = combineLogMsg(msg);
      if (IS_NEED_FILTERLOG) {
        tag = filterString(tag);
        msgStr = filterString(msgStr);
      }
      if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
        // 控制台日志
        Log.e(TAG, "[" + tag + "]" + msgStr);
      }
      if (IS_NEED_FILELOG) {
        // 打印文件到手机里面
        writeLogToFile(Log.ERROR, tag, msgStr, null);
      }
    }
  }

  /**
   * 记录ERROR级别日志 在记录ERROR级别日志时调用, 如果日志配置为不记录日志或日志级别高于ERROR, 不记录日志。
   *
   * @param tag 日志tag
   * @param tr 异常对象
   * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
   */
  public static void e(String tag, Throwable tr, String... msg) {
    // 控制台日志
    if (needPrint()) {
      String msgStr = combineLogMsg(msg);
      if (IS_NEED_FILTERLOG) {
        tag = filterString(tag);
        msgStr = filterString(msgStr);
      }
      if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.e(
            TAG,
            "[" + tag + "]" + msgStr + "[Throwable]" + (tr == null ? " null" : tr.getMessage()));
      }
      // 文件日志
      if (IS_NEED_FILELOG) {
        writeLogToFile(Log.ERROR, tag, msgStr, tr);
      }
    }
  }

  public static void e(String tag, String msg, Throwable tr) {
    // 控制台日志
    if (needPrint()) {
      String msgStr = combineLogMsg(msg);
      if (IS_NEED_FILTERLOG) {
        tag = filterString(tag);
        msgStr = filterString(msgStr);
      }
      if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.e(TAG, "[" + tag + "]" + msgStr + "[Throwable]" + tr.getMessage());
      }
      // 文件日志
      if (IS_NEED_FILELOG) {
        writeLogToFile(Log.ERROR, tag, msgStr, tr);
      }
    }
  }

  private static String filterString(String str) {
//    if (TextUtils.isEmpty(str)) {
//      return "";
//    }
//    long time = System.currentTimeMillis();
//    for (String pattern : PATTERN_LIST) {
//      str = str.replaceAll(pattern, "");
//    }
//    Log.d(TAG, "filterString time == " + (System.currentTimeMillis() - time));
    return str;
  }

  /**
   * 组装动态传参的字符串，将动态参数的字符串拼接成一个字符串。
   *
   * @param msg 动态参数
   * @return 拼接后的字符串
   */
  private static String combineLogMsg(String... msg) {
    if (null == msg) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (String s : msg) {
      sb.append(s);
    }
    return sb.toString();
  }

    public static void updateLogFile(){
      LogThread.updateLogFile();
    }

  /** 日志线程类 用于记录日志到文件的线程类 */
  private static final byte[] INIT_WRITER_LOCK = new byte[0];

  private static class LogThread extends Thread {

    @Override
    public void start() {
      // 启动日志线程时初始化文件输出流
      synchronized (INIT_WRITER_LOCK) {
        logFile =
            FileUtils.getLogDir() + DateUtils.getCurrentDate("yyyy-MM-dd-HH-mm-ss") + ".log";
        initWriter();
      }
      deleteOldLogFile();
      super.start();
    }

    private void deleteOldLogFile(){
        File[] cacheFileList = new File(Utils.getContext().getCacheDir()+ File.separator).listFiles();
        if(cacheFileList == null){
            return;
        }
        for(File file : cacheFileList){
            Matcher m = sVersionPattern.matcher(file.getName());
            if(m.find() && file.getName().length() > 11){
                int result = 0;
                if(result < 0){
                    if(!FileUtils.deleteDir(file)){
                        LogUtils.d(TAG,"delete old log file failed! path == " + file.getPath() + File.separator + file.getName());
                    }
                }else if(result == 0){
                    File[] logFileList = new File(FileUtils.getAppLogDir()).listFiles();
                    if(logFileList == null){
                        return;
                    }
                    SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                    String fromDate = DateUtils.getCurrentDate("yyyy-MM-dd-HH-mm-ss");
                    long from;
                    try {
                        from = simpleFormat.parse(fromDate).getTime();
                        for(File logFile : logFileList){
                            String toDate = logFile.getName().substring(0, logFile.getName().lastIndexOf("."));
                            long to = simpleFormat.parse(toDate).getTime();
                            int days = (int) ((to - from)/(1000 * 60 * 60 * 24));
                            if(days < -1){
                                if(!logFile.delete()){
                                    LogUtils.d(TAG, "delete log file failed! path == " + logFile.getPath() + File.separator + logFile.getName());
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private static synchronized void updateLogFile(){
        logFile =
                FileUtils.getLogDir() + DateUtils.getCurrentDate("yyyy-MM-dd-HH-mm-ss") + ".log";
    }

    @Override
    public synchronized void run() {
      while (true) {
        // APP输出日志超过3M后，weshare.log复制为weshare_old.log后重新开始输出日志
        File file = new File(logFile);
        long length = file.length();
        // 当log文件达到最大大小时，将其重命名为备份文件：LOG_FILE_BACKUP；重新开始在LOG_FILE上打印
        if (length >= LOG_FILE_MAX_SIZE) {
          Log.d(TAG, "new log file!");
          logFile =
              FileUtils.getLogDir() + DateUtils.getCurrentDate("yyyy-MM-dd-HH-mm-ss") + ".log";
          initWriter();
        } else if (0 == length) {
          // 需要需要重新初始化writer,否则当用户删除log文件后，必须强行停止一次Hilink才能继续输出log
          initWriter();
        }

        String log;
        // 如果bw为null，没有必要重复调用initWriter检测，
        // 因为已经在start的时候检测过了，这样可以缓解耗电问题。
        if (null != bw) {
          // 循环从日志队列中取出日志, 记录到文件中
          while (null != (log = queLogs.poll())) {
            try {
              bw.write(log);
              bw.newLine();
              bw.flush();
            } catch (IOException e) {
              Log.e(TAG, e.getMessage());
              // 如果发生异常, 可能是sd卡不可用, 并尝试重新初始化输出流
              initWriter();
            }
            // 当手机内存为0时，测试手机出现ErrnoException,由于无法捕获ErrnoException，因此直接捕获Exception
            catch (Exception exception) {
              Log.d(TAG, exception.getMessage());
            }
          }
        }
        try {
          this.wait();
        } catch (InterruptedException e) {
          Log.e(TAG, e.getMessage());
        }
      }
    }

    @Override
    @Deprecated
    public void destroy() {
      // 如果输出流初始化过, 需要先关闭输出流, 释放资源
      try {
        if (bw != null) {
          bw.close();
        }
        if (null != outputStreamWriter) {
          outputStreamWriter.close();
        }
        if (true == fileOutputStream.getFD().valid()) {
          fileOutputStream.close();
        }
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }
      bw = null;
      super.destroy();
    }
  }

  /**
   * 记录日志到文件 如果配置成需要记录日志到文件, 需要将日志数据拼接成一条日志记录, 首先放入到待记录的日志队列中, 再由异步线程记录到文件中。 日志格式定义为：yyyy-MM-dd
   * HH:mm:ss.SSS, <D>degree, <T>tag, <M>message
   *
   * @param degree 日志级别
   * @param tag 日志标签
   * @param msg 日志信息
   * @param tr 异常
   */
  private static void writeLogToFile(int degree, String tag, String msg, Throwable tr) {
    if (IS_NEED_FILELOG) {
      // 拼接时间、日志级别、标签、信息
      StringBuffer sb = new StringBuffer();
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
      sb.append(df.format(Calendar.getInstance().getTime()))
          .append(", <D>")
          .append(degreeLabel.get(degree))
          .append(", <T>")
          .append(tag)
          .append(", <M>")
          .append(msg);

      // 如果有异常信息, 需要拼接异常信息, 拼接所有的堆栈信息
      if (null != tr) {
        StackTraceElement[] stacks = tr.getStackTrace();

        sb.append(", <E>").append(tr.getMessage()).append("\r\n");
        for (int i = 0; i < stacks.length; i++) {
          sb.append("\t\tat ")
              .append(stacks[i].getClassName())
              .append(".")
              .append(stacks[i].getMethodName())
              .append("(")
              .append(stacks[i].getClassName())
              .append(".java")
              .append(" ")
              .append(stacks[i].getLineNumber())
              .append(")")
              .append("\r\n");
        }
      }

      // 将日志信息增加到队列中
      queLogs.add(sb.toString());

      // 如果日志线程没有初始化, 需要初始化并启动
      if (null == logThread) {
        logThread = new LogThread();
        logThread.start();
      } else {
        synchronized (logThread) {
          if (null != logThread) {
            logThread.notifyAll();
          }
        }
      }
    }
  }

  /** 初始化文件输出流对象 如果文件输出流已经初始化过, 需要先关闭输出流, 再创建新的输出流对象; 否则直接创建 */
  private static synchronized void initWriter() {
    // 只缓存允许的日志数目
    if (queLogs.size() > MAX_CACHE_SIZE) {
      queLogs.clear();
    }

    // 如果输出流初始化过, 需要先关闭输出流, 释放资源
    if (bw != null) {
      try {
        bw.close();
        //  可能存在未释放的系统资源
        bw = null;
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }
    }

    try {
      boolean externalStorageWriteable = false;
      String state = Environment.getExternalStorageState();
      if (Environment.MEDIA_MOUNTED.equals(state)) {
        externalStorageWriteable = true;
      }

      if (externalStorageWriteable) {
        // 如果日志文件对应的文件夹不存在, 需要先创建文件夹
        String logFile = Normalizer.normalize(LogUtils.logFile, Normalizer.Form.NFKC);
        File dir = new File(logFile.substring(0, logFile.lastIndexOf("/")));
        if (!dir.exists()) {
          if (!dir.mkdirs()) {
            Log.e(TAG, "create log directory failed!");
            return;
          }
        }
        dir.setReadable(true);
        dir.setWritable(true);
        // 创建文件输出流
        fileOutputStream = new FileOutputStream(LogUtils.logFile, true);
        outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        bw = new BufferedWriter(outputStreamWriter);
      }
    } catch (FileNotFoundException e) {
      closeSteam(e);
    }
  }

  private static void closeSteam(Exception e) {
    LogUtils.e(TAG, e, e.toString());
    try {
      if (bw != null) {
        bw.close();
      }
      if (null != outputStreamWriter) {
        outputStreamWriter.close();
      }
      if (null != fileOutputStream) {
        if (fileOutputStream.getFD().valid()) {
          fileOutputStream.close();
        }
      }
    } catch (IOException e1) {
      LogUtils.e(TAG, "IOException e :" + e.getMessage());
    }
  }
}
