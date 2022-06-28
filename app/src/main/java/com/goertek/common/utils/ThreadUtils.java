package com.goertek.common.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 文件名 ThreadUtils.java
 * 描述 线程管理工具类
 * @author jochen.zhang
 */
public final class ThreadUtils {
    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
    /** 线程池 */
    private ExecutorService threadPool;
    /** 单例实例 */
    private static ThreadUtils Instance;

    /** 构造函数 */
    private ThreadUtils() {
        if (threadPool == null) {
            threadPool = new ThreadPoolExecutor(ThreadUtils.CPU_NUM, ThreadUtils.CPU_NUM, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        }
    }

    /** 构造函数 */
    private ThreadUtils(int cpuNum) {
        if (threadPool == null) {
            threadPool = new ThreadPoolExecutor(cpuNum, cpuNum, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        }
    }

    /**
     * 自定义初始化线程熟练
     * 必须在getInstance之前调用
     */
    public static void init(int cpuNum) {
        if (Instance == null) {
            synchronized (ThreadUtils.class) {
                if (Instance == null) {
                    Instance = new ThreadUtils(cpuNum);
                }
            }
        } else {
            throw new UnsupportedOperationException("init func must before first getInstance！");
        }
    }

    /**
     * 获取单例实例
     *
     * @return ThreadUtils
     */
    public static ThreadUtils getInstance() {
        if (Instance == null) {
            synchronized (ThreadUtils.class) {
                if (Instance == null) {
                    Instance = new ThreadUtils();
                }
            }
        }
        return Instance;
    }

    /**
     * 获取线程池
     *
     * @return ExecutorService
     */
    public static ExecutorService getThreadPool() {
        if (Instance == null) {
            synchronized (ThreadUtils.class) {
                if (Instance == null) {
                    Instance = new ThreadUtils();
                }
            }
        }
        return Instance.threadPool;
    }

    /**
     * 线程池执行操作
     *
     * @param runnable 可运行块
     */
    public void run(@NonNull Runnable runnable) {
        threadPool.execute(runnable);
    }

    public <T> Future<T> submit(@NonNull Callable<T> call) {
        return threadPool.submit(call);
    }

    /**
     * 关闭线程池,这将导致改线程池立即停止接受新的线程请求,但已经存在的任务仍然会执行,直到完成。
     */
    public void shutDown() {
        if (Instance == null) {
            return;
        }
        synchronized (this) {
            if (Instance != null) {
                threadPool.shutdownNow();
            }
            Instance = null;
        }
    }
}
