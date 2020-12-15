package com.soul.base.juc.level05;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class T28_SynchronusQueue {
	//同步队列
	//类似 Exchanger 的功能, 两个线程间传递数据
	//容量为0 !!! 不允许向队列中装元素
	//只能向队列中put元素(add会直接报错)
	public static void main(String[] args) throws InterruptedException {
		BlockingQueue<String> strs = new SynchronousQueue<>();

		new Thread(()->{
			try {
				//必须
				System.out.println(strs.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		//阻塞等待消费者消费
		strs.put("aaa");
		//等待消费
		strs.put("bbb");
		//该队列容量为0, add会抛异常: Queue full
		//strs.add("aaa");
		System.out.println(strs.size());
	}
}
