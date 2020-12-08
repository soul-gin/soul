package com.soul.base.juc.level02;

public class _01_ObjectSyncLock {

    private int count = 20;
    private Object o = new Object();

    public void m() {
        synchronized(o) { //任何线程要执行下面的代码，必须先拿到o的锁
            count--;
            System.out.println(Thread.currentThread().getName() + " count = " + count);
        }
    }

    public void m2() {
        //不加锁会出现重复值
        //synchronized(o) { //任何线程要执行下面的代码，必须先拿到o的锁
            count--;
            System.out.println(Thread.currentThread().getName() + " count = " + count);
        //}
    }

}

class MyTest1 {
    public static void main(String[] args) {
        _01_ObjectSyncLock objectSyncLock = new _01_ObjectSyncLock();
        Thread[] threads = new Thread[20];
        for (int i = 0; i < 20; i++) {
            threads[i] = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    objectSyncLock.m();
                }
            });
        }

        for (int i = 0; i < 20; i++) {
            threads[i].start();
        }


    }
}