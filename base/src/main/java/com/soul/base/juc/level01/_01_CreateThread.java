package com.soul.base.juc.level01;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * *进程与线程**
 * - 进程: 资源分配的最小单元, 系统的不同执行路径
 * - 线程: 资源调度的最小单元, 是进程中的最小执行单元
 * <p>
 * *线程的三种方式**
 * - 继承 Thread
 * - 实现 Runnable
 * - 线程池 Executors.newCachedThreadPool
 * - 特殊: 语法糖 lambda
 */
public class _01_CreateThread {

    public static void main(String[] args) {
        //创建线程并调用start方法(start会调用run方法)
        new MyThread1().start();
        //主线程,线程交替执行打印
        for (int i = 0; i < 3; i++) {
            try {
                TimeUnit.MICROSECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("main");
        }


        //实现 Runnable
        new Thread(new MyThread2()).run();

        // lambda  类似匿名内部类, Thread接收Runnable
        new Thread(() -> {
            System.out.println("******");
            System.out.println("3 lambda run method");
            System.out.println("******");
        }).start();


        // 通过线程池来启动, 一般不建议使用 newCachedThreadPool 这种在Integer.MAX_VALUE范围内无界线程池; 最好自定义
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
        newCachedThreadPool.execute(new Thread(() -> {
            System.out.println("#######");
            System.out.println("4 newCachedThreadPool&Thread run method");
            System.out.println("#######");
        }));

        newCachedThreadPool.execute(() -> {
            System.out.println("%%%");
            System.out.println("5 newCachedThreadPool&Runnable run method");
            System.out.println("%%%");
        });

        //线程池销毁,以便退出main方法
        newCachedThreadPool.shutdown();
    }


    private static class MyThread1 extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    TimeUnit.MICROSECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("1 Thread run method");
            }
        }
    }

    private static class MyThread2 implements Runnable {
        @Override
        public void run() {
            System.out.println("--------");
            System.out.println("2 Runnable run method");
            System.out.println("--------");
        }
    }


}
