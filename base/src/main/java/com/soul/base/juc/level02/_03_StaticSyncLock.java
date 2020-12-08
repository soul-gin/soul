package com.soul.base.juc.level02;

public class _03_StaticSyncLock {

    //注意 volatile 只是保证可见, 不保证原子性
    private static int /*volatile*/ count = 20;

    public synchronized static void m() { //这里等同于synchronized(FineCoarseLock.class)
        count--;
        System.out.println(Thread.currentThread().getName() + " count = " + count);
    }

    public static void m2() {
        synchronized(_03_StaticSyncLock.class) { //考虑一下这里写synchronized(this)是否可以？
            count --;
            System.out.println(Thread.currentThread().getName() + " count = " + count);
        }
    }


    public static void m3() {
        //不加锁会出现重复值
        count--;
        System.out.println(Thread.currentThread().getName() + " count = " + count);
    }

}

class MyTest3 {
    public static void main(String[] args) {
        _03_StaticSyncLock staticSyncLock = new _03_StaticSyncLock();
        Thread[] threads = new Thread[20];
        for (int i = 0; i < 20; i++) {
            threads[i] = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    staticSyncLock.m();
                }
            });
        }

        for (int i = 0; i < 20; i++) {
            threads[i].start();
        }


    }
}