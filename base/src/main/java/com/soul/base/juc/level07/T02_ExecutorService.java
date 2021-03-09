package com.soul.base.juc.level07;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 认识ExecutorService,阅读API文档
 * 认识submit方法，扩展了execute方法，具有一个返回值
 *
 * concurrent vs parallel
 * concurrent, 并发, 指任务的提交
 * parallel, 并行, 指任务执行(在多个cpu上执行)
 * 并行是并发的子集
 */
public class T02_ExecutorService  {
    public static void main(String[] args) {
        ExecutorService e = Executors.newCachedThreadPool();
        e.submit(() -> System.out.println("hello ExecutorService"));
        //停止
        e.shutdown();
    }
}
