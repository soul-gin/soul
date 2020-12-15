package com.soul.base.juc.level05;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class T26_ArrayBlockingQueue {

	//Queue 对线程友好的API, offer peek poll
	//BlockQueue 阻塞队列, 增加了 put take
	//阻塞有界队列(先进先出, FIFO)
	static volatile BlockingQueue<String> strs = new ArrayBlockingQueue<>(10);

	static Random r = new Random();

	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			strs.put("a" + i);
		}

		new Thread(() -> {
			try {
				//等待2秒后消费
				Thread.sleep(2000);
				//消费两条数据
				//尝试1秒消费(1s内处于阻塞状态)
				strs.poll(1, TimeUnit.SECONDS);
				//阻塞消费(等待至有数据可以被消费)
				strs.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		//三种设置值的方式
		//put满了就会等待，程序阻塞
		strs.put("aaa");
		//add满了就会抛出异常
		//strs.add("aaa");
		//offer会阻塞1秒, 尝试添加, 如不能添加则解释阻塞
		strs.offer("bbb", 1, TimeUnit.SECONDS);
		strs.offer("ccc", 1, TimeUnit.SECONDS);

		System.out.println(strs);
	}
}
