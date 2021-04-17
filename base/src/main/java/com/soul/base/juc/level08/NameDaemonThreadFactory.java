package com.soul.base.juc.level08;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
* @author gin
* @date 2021/4/17
*/
public class NameDaemonThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    /**
     * 是否守护线程
     */
    private boolean isDaemon = true;

    NameDaemonThreadFactory(String name, Boolean daemon) {
        if (null != daemon) {
            isDaemon = daemon;
        }
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = name + "pool-" +
                POOL_NUMBER.getAndIncrement() +
                "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        //守护线程
        if (!t.isDaemon()) {
            if (isDaemon) {
                // 原线程为非守护则设置为守护
                t.setDaemon(true);
            }
        } else if (!isDaemon) {
            // 原线程为守护则还原为非守护
            t.setDaemon(false);
        }
        //优先级
        if (Thread.NORM_PRIORITY != t.getPriority()) {
            // 标准优先级
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}