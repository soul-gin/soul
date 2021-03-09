package com.soul.base.juc.level07;

import java.util.concurrent.*;

/**
 * 认识Callable，对Runnable进行了扩展
 * 对Callable的调用，可以有返回值
 */
public class T03_Callable {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<String> c = new Callable() {
            @Override
            public String call() throws Exception {
                return "Hello Callable";
            }
        };

        ExecutorService service = Executors.newCachedThreadPool();
        //异步
        Future<String> future = service.submit(c);
        //阻塞, 中途可以处理一些业务逻辑
        System.out.println(future.get());

        service.shutdown();
    }

}
