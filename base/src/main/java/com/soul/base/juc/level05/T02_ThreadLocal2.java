package com.soul.base.juc.level05;

import java.util.concurrent.TimeUnit;

/**
 * ThreadLocal线程局部变量
 *
 * ThreadLocal是使用空间换时间，synchronized是使用时间换空间
 * 比如在hibernate中session就存在与ThreadLocal中，避免synchronized的使用
 *
 * 运行下面的程序，理解ThreadLocal
 * - set: ThreadLocal.ThreadLocalMap.set(ThreadLocal, yourValue);
 * - ThreadLocal用途
 * 声明式事务, 保证同一个Connection
 *
 */
public class T02_ThreadLocal2 {
	// ThreadLocal 设置的值是线程独有的
	// 通过 ThreadLocalMap 实现, key是ThreadLoacl对象(ThreadLoacl对象(this)最终指向的是Thread中的map), value是对应设置的值
	// 所以实际上就是在线程自己的map中, 使用 ThreadLoacl 作为key, 设置的值作为value, 存了一个键值对
	// (不同线程的值不在同一个map而是各自的map, 所以不同线程只能读取到自己的)
	static ThreadLocal<Person> tl = new ThreadLocal<>();
	
	public static void main(String[] args) {
				
		new Thread(()->{
			tl.set(new Person());
			tl.get().name = "gin";
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println(tl.get().name);
		}).start();
		
		new Thread(()->{
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//每个不同线程有自己的变量
			System.out.println(tl.get());
			tl.set(new Person());
			System.out.println(tl.get().name);
		}).start();
	}
	
	static class Person {
		String name = "zhangsan";
	}
}


