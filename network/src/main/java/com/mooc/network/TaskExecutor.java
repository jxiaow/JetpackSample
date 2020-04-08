package com.mooc.network;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecutor {
    private final ExecutorService mDiskIO;
    private volatile Handler mHandler;
    private final Object lock = new Object();

    private static final class Holder {
        private static final TaskExecutor sInstance = new TaskExecutor();
    }

    private TaskExecutor() {
        mDiskIO = Executors.newFixedThreadPool(2, threadFactory);
    }

    public static TaskExecutor get() {
        return Holder.sInstance;
    }

    public void executeOnDiskIO(Runnable runnable) {
        mDiskIO.execute(runnable);
    }

    public void postToMain(Runnable runnable) {
        if (mHandler == null) {
            synchronized (lock) {
                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        mHandler.post(runnable);
    }

    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private ThreadFactory threadFactory = new ThreadFactory() {
        private static final String THREAD_NAME_STEM = "arch_disk_io_%d";

        private final AtomicInteger mThreadId = new AtomicInteger(0);

        @SuppressLint("DefaultLocale")
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r);
            t.setName(String.format(THREAD_NAME_STEM, mThreadId.getAndIncrement()));
            return t;
        }
    };
}
