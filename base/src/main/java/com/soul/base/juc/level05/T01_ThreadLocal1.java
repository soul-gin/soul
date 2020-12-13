package com.soul.base.juc.level05;

import java.util.concurrent.TimeUnit;

/**
 * ThreadLocal线程局部变量
 */
public class T01_ThreadLocal1 {

	volatile static Person p = new Person();
	
	public static void main(String[] args) {
				
		new Thread(()->{
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//在被其他线程1秒时修改, 2秒后打印值
			System.out.println(p.name);
		}).start();
		
		new Thread(()->{
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//修改会影响其他线程, 如果想每个线程独占, 那么需要使用 ThreadLocal
			p.name = "lisi";
		}).start();
	}
}

class Person {
	String name = "zhangsan";
}
