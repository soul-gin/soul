package com.soul.base.juc.level04;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * 面试题：写一个固定容量同步容器，拥有put和get方法，以及getCount方法，
 * 能够支持2个生产者线程以及10个消费者线程的阻塞调用
 *
 * 使用wait和notify/notifyAll来实现
 *
 */
public class T09_MyContainer1<T> {
	final private LinkedList<T> lists = new LinkedList<>();
	final private int MAX = 10; //最多10个元素
	static int count = 0;
	
	
	public synchronized void put(T t) {
		//为什么用while而不是用if
		//后续操作是 notifyAll, 必须保证每个线程都判断下, 防止醒了(notifyAll)后直接执行后续业务
		while(lists.size() == MAX) {
			try {
				this.wait(); //effective java
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		lists.add(t);
		++count;
		//通知消费者线程进行消费
		//实际叫醒是所有的线程
		this.notifyAll();
	}
	
	public synchronized T get() {
		T t = null;
		while(lists.size() == 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		t = lists.removeFirst();
		count --;
		//通知生产者进行生产
		//实际叫醒是所有的线程
		this.notifyAll();
		return t;
	}
	
	public static void main(String[] args) {
		T09_MyContainer1<String> c = new T09_MyContainer1<>();
		//启动消费者线程
		for(int i=0; i<10; i++) {
			new Thread(()->{
				//每个消费者消费5个
				for(int j=0; j<5; j++) System.out.println(c.get());
			}, "c" + i).start();
		}
		
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//启动生产者线程
		for(int i=0; i<2; i++) {
			new Thread(()->{
				//每个生产者生产25个
				for(int j=0; j<25; j++) c.put(Thread.currentThread().getName() + " " + j + " ,middle count=" + count);
			}, "p" + i).start();
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("count end=" + count);
	}
}
