
package com.soul.base.juc.level02;

/**
 * 同步和非同步方法是否可以同时调用？
 */

public class _04_ThreadConcurrent {

	public synchronized void m1() { 
		System.out.println(Thread.currentThread().getName() + " m1 start...");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + " m1 end");
	}
	
	public void m2() {
		System.out.println(Thread.currentThread().getName() + " m2 start...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + " m2 end");
	}
	
	public static void main(String[] args) {
		_04_ThreadConcurrent thread = new _04_ThreadConcurrent();
		
		/*new Thread(()->thread.m1(), "t1").start();
		new Thread(()->thread.m2(), "t2").start();*/
		
		new Thread(thread::m1, "t1").start();
		new Thread(thread::m2, "t2").start();
		
		/*
		//1.8之前的写法
		new Thread(new Runnable() {

			@Override
			public void run() {
				thread.m1();
			}
			
		});
		*/
		
	}
	
}
