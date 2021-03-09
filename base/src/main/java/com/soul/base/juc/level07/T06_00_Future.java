package com.soul.base.juc.level07;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 认识future, 未来结果的获取
 * 异步
 */
public class T06_00_Future {
	public static void main(String[] args) throws InterruptedException, ExecutionException {

		FutureTask<Integer> task = new FutureTask<>(()->{
			System.out.println("task begin...");
			TimeUnit.MILLISECONDS.sleep(2000);
			return 1000;
		}); //new Callable () { Integer call();}

		new Thread(task).start();

		System.out.println("task end=" + task.get()); //阻塞


	}
}
