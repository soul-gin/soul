
package com.soul.base.juc.level02;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * synchronized优化
 * 同步代码块中的语句越少越好
 * 比较m1和m2
 *
 * 粗粒度锁(Fine)还是细粒度锁(Coarse)
 */
public class T06_FineCoarseLock {
	
	int count = 0;

	//整个方法加锁, 粗粒度锁(Fine)
	synchronized void m1() {
		//do sth need not sync
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//业务逻辑中只有下面这句需要sync，这时不应该给整个方法上锁
		count ++;
		
		//do sth need not sync
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	void m2() {
		//do sth need not sync
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//业务逻辑中只有下面这句需要sync，这时不应该给整个方法上锁
		//采用细粒度的锁，可以使线程争用时间变短，从而提高效率
		//细粒度锁(Coarse)
		synchronized(this) {
			count ++;
		}
		//do sth need not sync
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		T06_FineCoarseLock t = new T06_FineCoarseLock();

		List<Thread> threads1 = new ArrayList<Thread>();
		//多线程并发修改, volatile只能保障可见, 不保证操作原子性
		for(int i=0; i<2; i++) {
			threads1.add(new Thread(t::m1, "thread-"+i));
		}
		threads1.forEach((o)->o.start());
		threads1.forEach((o)->{
			try {
				o.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println(t.count);

		List<Thread> threads2 = new ArrayList<Thread>();
		//多线程并发修改, volatile只能保障可见, 不保证操作原子性
		for(int i=0; i<3; i++) {
			threads2.add(new Thread(t::m2, "thread-"+i));
		}
		threads2.forEach((o)->o.start());
		threads2.forEach((o)->{
			try {
				o.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		System.out.println(t.count);

	}

}
