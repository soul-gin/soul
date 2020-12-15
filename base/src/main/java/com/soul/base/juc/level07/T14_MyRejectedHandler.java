package com.soul.base.juc.level07;

import java.util.concurrent.*;

public class T14_MyRejectedHandler {
    public static void main(String[] args) {
        ExecutorService service = new ThreadPoolExecutor(4, 4,
                0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(6),
                Executors.defaultThreadFactory(),
                new MyHandler());
    }

    //自定义线程池拒绝策略的方法
    static class MyHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //记录日志
            //log("r rejected")
            //任务数据存储到kafka redis mysql等等
            //save r kafka mysql redis
            //或者多次尝试
            //try 3 times
            if(executor.getQueue().size() < 10000) {
                //队列小于1w, 尝试3次
                //try put again();
            }
        }
    }
}
