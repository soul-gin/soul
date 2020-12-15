package com.soul.base.juc.level05;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class T27_DelayQueue {

	//按时间进行任务调度
	//DelayQueue 按时间等待阻塞队列
	static BlockingQueue<MyTask> tasks = new DelayQueue<>();

	static Random r = new Random();

	static class MyTask implements Delayed {
		String name;
		//等待时间
		long runningTime;

		MyTask(String name, long rt) {
			this.name = name;
			this.runningTime = rt;
		}

		@Override
		public int compareTo(Delayed o) {
			//需要实现 compareTo 接口, 根据时间进行排序
			if(this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS))
				return -1;
			else if(this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS))
				return 1;
			else
				return 0;
		}

		@Override
		public long getDelay(TimeUnit unit) {

			return unit.convert(runningTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}


		@Override
		public String toString() {
			return name + " " + runningTime;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		long now = System.currentTimeMillis();
		MyTask t1 = new MyTask("t1", now + 1000);
		MyTask t2 = new MyTask("t2", now + 2000);
		MyTask t3 = new MyTask("t3", now + 1500);
		//t4需要5.5秒后才可从队列中获取
		MyTask t4 = new MyTask("t4", now + 5500);
		MyTask t5 = new MyTask("t5", now + 500);

		//添加任务
		tasks.put(t1);
		tasks.put(t2);
		tasks.put(t3);
		tasks.put(t4);
		tasks.put(t5);

		System.out.println(tasks);

		for(int i=0; i<5; i++) {
			//阻塞获取任务
			System.out.println(tasks.take());
		}
	}
}
