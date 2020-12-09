package com.soul.base.juc.level03;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class T15_TestSemaphore {
    public static void main(String[] args) {

        //信号量
        //允许两个线程同时执行
        //Semaphore s = new Semaphore(2);
        //允许两个线程同时执行, 公平
        Semaphore s = new Semaphore(2, true);
        //允许一个线程同时执行
        //Semaphore s = new Semaphore(1);

        new Thread(()->{
            try {
                s.acquire();

                System.out.println("T1 running...");
                Thread.sleep(2000);
                System.out.println("T1 end...");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                s.release();
            }
        }).start();

        new Thread(()->{
            try {
                s.acquire();

                System.out.println("T2 running...");
                Thread.sleep(2000);
                System.out.println("T2 end...");

                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            try {
                s.acquire();

                System.out.println("T3 running...");
                Thread.sleep(2000);
                System.out.println("T3 end...");

                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
