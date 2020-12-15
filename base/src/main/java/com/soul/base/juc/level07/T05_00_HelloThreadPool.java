package com.soul.base.juc.level07;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class T05_00_HelloThreadPool {

    static class Task implements Runnable {
        private int i;

        public Task(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " Task " + i);
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "Task{" +
                    "i=" + i +
                    '}';
        }
    }

    public static void main(String[] args) {
        //corePoolSize -> 核心线程数, 初始线程为0, 来了任务会判断worker数量是否小于核心线程数, 小于则创建新线程
        //maximumPoolSize -> 最大线程
        //keepAliveTime -> 归还线程时间(线程没活干了)
        //unit -> 归还时间的单位
        //workQueue -> 等待队列, 可以使用不同的队列
        //threadFactory -> 创建线程的工厂(注意指定线程组的名称, 便于jstack回溯哪个线程组的问题)
        //handler -> 拒绝策略
        //注意, 任务超过核心线程数, 先将任务加入队列中, 如果队列也满了, 再启动新的线程, 直到达到最大线程数, 再有任务过来就执行拒绝策略
        //拒绝策略jdk提供了4种: Abort 抛异常; Discard 扔掉,不抛异常;
        // DiscardOldest 扔掉排队时间最久的; CallerRuns 调用者处理任务(提交任务线程自己执行)
        //拒绝策略支持自定义(一般生产使用自定义策略, 接收不了的任务可以存缓存或者消息中间件)
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 4,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4),
                Executors.defaultThreadFactory(),
                //new ThreadPoolExecutor.AbortPolicy());
                //new ThreadPoolExecutor.DiscardPolicy());
                //new ThreadPoolExecutor.DiscardOldestPolicy());
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 8; i++) {
            tpe.execute(new Task(i));
        }

        System.out.println(tpe.getQueue());

        tpe.execute(new Task(100));

        System.out.println(tpe.getQueue());

        tpe.shutdown();
    }
}
