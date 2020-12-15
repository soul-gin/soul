package com.soul.base.juc.level05;

import java.util.ArrayList;
import java.util.List;

/**
 * 有N张火车票，每张票都有一个编号
 * 同时有10个窗口对外售票
 * 请写一个模拟程序
 *
 *
 * 分析下面的程序可能会产生哪些问题？
 * 超卖(最后一张票多个线程会卖出, 导致remove空)
 * 可能会抛处 java.lang.ArrayIndexOutOfBoundsException
 * 问题:1.容器本身非同步 2.判断和移除逻辑需要同步(这两个操作需要保证原子性)
 */
public class T17_TicketSeller1 {
	static List<String> tickets = new ArrayList<>();

	static {
		for(int i=0; i<10000; i++) tickets.add("票编号：" + i);
	}



	public static void main(String[] args) {
		for(int i=0; i<10; i++) {
			new Thread(()->{
				while(tickets.size() > 0) {
					System.out.println("销售了--" + tickets.remove(0));
				}
			}).start();
		}
	}
}
