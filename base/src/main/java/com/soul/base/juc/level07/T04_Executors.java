package com.soul.base.juc.level07;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 认识Executors, 线程池的工厂
 */
public class T04_Executors {
	public static void main(String[] args) {
		//Executors
		//必须通过 ThreadPoolExecutor 来创建线程池, 规避下面一系列问题
		//线程数上限为 Integer.MAX_VALUE 可能会产生大量线程, 导致OOM
		ExecutorService executorService1 = Executors.newCachedThreadPool();

		//允许请求的队列上限为 Integer.MAX_VALUE 可能会导致OOM
		ExecutorService executorService2 = Executors.newSingleThreadExecutor();
		ExecutorService executorService3 = Executors.newFixedThreadPool(10);


	}
}
