package com.soul.base.juc.level04;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 面试题
 * 实现一个容器，提供两个方法，add，size
 * 写两个线程，线程1添加10个元素到容器中，线程2实现监控元素的个数，当个数到5个时，线程2给出提示并结束
 *
 * 给lists添加volatile之后，t2能够接到通知，但是，t2线程的死循环很浪费cpu，如果不用死循环，
 * 而且，如果在if 和 break之间被别的线程打断，得到的结果也不精确，
 * 该怎么做呢？
 *
 * 注意: volatile尽量不要修饰引用值(引用本身的修改其他线程可见, 引用里面的属性修改其他线程不可见)
 *
 */
public class T02_WithVolatile {

	//单纯添加volatile，能够感知到数据变化, 但是size方法不是同步方法, 如果执行过快, 读取到的可能还是旧值
	// volatile 仅能感知基本数据类型
	//volatile List lists = new LinkedList();

	//还需要创建一个同步的集合
	volatile List lists = Collections.synchronizedList(new LinkedList<>());

	public void add(Object o) {
		lists.add(o);
	}

	public int size() {
		return lists.size();
	}

	public static void main(String[] args) {

		T02_WithVolatile c = new T02_WithVolatile();
		new Thread(() -> {
			for(int i=0; i<10; i++) {
				c.add(new Object());
				System.out.println("add " + i);

				//不再等待, t2可能读取不到最新的值就添加完成了
				/*try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
		}, "t1").start();
		
		new Thread(() -> {
			while(true) {
				//添加了 volatile, 且size方法为同步方法, 数据准确可见
				if(c.size() == 5) {
					break;
				}
			}
			System.out.println("t2 结束");
		}, "t2").start();
	}
}
