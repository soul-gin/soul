package com.soul.base.juc.level05;

import java.util.Hashtable;
import java.util.UUID;

/*
 * 同步容器类
 *
 *  1：Vector Hashtable ：早期使用synchronized实现
 *  2：ArrayList HashSet ：未考虑多线程安全（未实现同步）
 *  3：HashSet vs Hashtable StringBuilder vs StringBuffer
 *  4：Collections.synchronized***工厂方法使用的也是synchronized
 *
 *  使用早期的同步容器以及Collections.synchronized***方法的不足之处，请阅读：
 *  http://blog.csdn.net/itm_hadf/article/details/7506529
 *
 *  使用新的并发容器
 *  http://xuganggogo.iteye.com/blog/321630
 */

/**
 * 绝大多数方法使用 synchronized 加锁
 * 实际上很多任务单线程操作, 不需要锁
 * 目前 Hashtable 使用的会比较少
 */
public class T10_TestHashtable {

    static Hashtable<UUID, UUID> m = new Hashtable<>();

    static int count = T09_Constants.COUNT;
    static UUID[] keys = new UUID[count];
    static UUID[] values = new UUID[count];
    static final int THREAD_COUNT = T09_Constants.THREAD_COUNT;

    static {
        for (int i = 0; i < count; i++) {
            keys[i] = UUID.randomUUID();
            values[i] = UUID.randomUUID();
        }
    }

    static class MyThread extends Thread {
        int start;
        int gap = count/THREAD_COUNT;

        public MyThread(int start) {
            this.start = start;
        }

        @Override
        public void run() {
            for(int i=start; i<start+gap; i++) {
                m.put(keys[i], values[i]);
            }
        }
    }

    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        Thread[] threads = new Thread[THREAD_COUNT];

        //指定线程, 每个线程装一定数量的数据
        for(int i=0; i<threads.length; i++) {
            threads[i] =
            new MyThread(i * (count/THREAD_COUNT));
        }

        for(Thread t : threads) {
            t.start();
        }

        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        //计算耗时
        System.out.println(end - start);

        //个数应该是和 count 保持一致(目前设置的是100w)
        System.out.println(m.size());

        //-----------------------------------

        start = System.currentTimeMillis();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 10000000; j++) {
                    m.get(keys[10]);
                }
            });
        }

        for(Thread t : threads) {
            t.start();
        }

        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
