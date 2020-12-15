package com.soul.base.juc.level07;

import java.util.concurrent.Executor;

/**
 *
 * Executor -> 执行提交的任务的对象
 * ExecutorService -> 在Executor基础上扩展了submit 等方法
 * Callable = Runnable + result (可以返回处理结果的Runnable)
 * Executors -> Factory and utility methods for Executor
 * ThreadPool
 * Future -> 存储将来执行后产生的结果
 * FutureTask -> Future + Runnable
 *
 * fixed cached single scheduled workstealing forkjoin
 *
 * ThreadpoolExecutor
 *
 * PStreamAPI
 */
public class T01_MyExecutor implements Executor {

    public static void main(String[] args) {
        new T01_MyExecutor().execute(() -> System.out.println("hello executor"));
    }

    @Override
    public void execute(Runnable command) {
        //new Thread(command).run();
        command.run();

    }

}

