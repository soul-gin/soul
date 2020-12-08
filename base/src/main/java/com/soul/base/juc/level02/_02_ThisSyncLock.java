package com.soul.base.juc.level02;

public class _02_ThisSyncLock {

    private int count = 20;

    public void m() {
        synchronized(this) { //任何线程要执行下面的代码，必须先拿到this的锁
            count--;
            System.out.println(Thread.currentThread().getName() + " count = " + count);
        }
    }

    public synchronized void m2() { //等同于在方法的代码执行时要synchronized(this)
        count--;
        System.out.println(Thread.currentThread().getName() + " count = " + count);
    }


    public void m3() {
        //不加锁会出现重复值
        //synchronized(this) { //任何线程要执行下面的代码，必须先拿到this的锁
        count--;
        System.out.println(Thread.currentThread().getName() + " count = " + count);
        //}
    }

}

class MyTest2 {
    public static void main(String[] args) {
        _02_ThisSyncLock thisSyncLock = new _02_ThisSyncLock();
        Thread[] threads = new Thread[20];
        for (int i = 0; i < 20; i++) {
            threads[i] = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    thisSyncLock.m();
                }
            });
        }

        for (int i = 0; i < 20; i++) {
            threads[i].start();
        }


    }
}