package com.soul.base.juc.level04;

import java.util.concurrent.locks.Lock;

public class T12_Main {
    public static int m = 0;
    public static Lock lock = new T11_MLock();

    public static void main(String[] args) throws Exception {
        Thread[] threads = new Thread[100];

        for(int i=0; i<threads.length; i++) {
            threads[i] = new Thread(()->{
                try {
                    lock.lock();
                    for (int j = 0; j < 100; j++) m++;
                } finally {
                    lock.unlock();
                }
            });
        }

        for(Thread t : threads) t.start();

        for (Thread t : threads) t.join();

        System.out.println(m);
    }
}