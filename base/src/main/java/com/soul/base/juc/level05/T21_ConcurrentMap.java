package com.soul.base.juc.level05;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * http://blog.csdn.net/sunxianghuang/article/details/52221913
 * http://www.educity.cn/java/498061.html
 * 阅读concurrentskiplistmap
 *
 * 总结：
 * 1：对于map/set的选择使用
 * HashMap
 * TreeMap
 * LinkedHashMap
 *
 * Hashtable
 * Collections.sychronizedXXX
 *
 * ConcurrentHashMap
 * ConcurrentSkipListMap
 *
 * 2：队列
 * ArrayList
 * LinkedList
 * Collections.synchronizedXXX
 * CopyOnWriteList
 * Queue
 * 	CocurrentLinkedQueue //concurrentArrayQueue
 * 	BlockingQueue
 * 		LinkedBQ
 * 		ArrayBQ
 * 		TransferQueue
 * 		SynchronusQueue
 * 	DelayQueue执行定时任务
 */
public class T21_ConcurrentMap {
	public static void main(String[] args) {
		//高并发不排序
		Map<String, String> map = new ConcurrentHashMap<>();
		//高并发并且排序(跳表实现cas要比TreeMap难度低很多, 所以目前没有默认的并发TreeMap)
		//Map<String, String> map = new ConcurrentSkipListMap<>();

		//Map<String, String> map = new Hashtable<>();
		//Map<String, String> map = new HashMap<>(); //Collections.synchronizedXXX
		//TreeMap
		Random r = new Random();
		Thread[] ths = new Thread[100];
		CountDownLatch latch = new CountDownLatch(ths.length);
		long start = System.currentTimeMillis();
		for(int i=0; i<ths.length; i++) {
			ths[i] = new Thread(()->{
				for(int j=0; j<10000; j++) map.put("a" + r.nextInt(100000), "a" + r.nextInt(100000));
				latch.countDown();
			});
		}

		Arrays.asList(ths).forEach(t->t.start());
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		System.out.println(end - start);
		System.out.println(map.size());

	}
}
