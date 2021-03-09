package com.soul.base.juc.level07;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class T07_SingleThreadPool {
	public static void main(String[] args) {
		//允许请求的队列上限为 Integer.MAX_VALUE 可能会导致OOM
		//一个线程的线程池, 可以保障顺序执行(有队列, 且有整体管理, 比自己new Thread更优)
		ExecutorService service = Executors.newSingleThreadExecutor();
		for(int i=0; i<5; i++) {
			final int j = i;
			service.execute(()->{

				System.out.println(j + " " + Thread.currentThread().getName());
			});
		}

	}
}
