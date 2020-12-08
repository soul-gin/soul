
package com.soul.base.juc.level03;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock还可以指定为公平锁
 * 保证每个线程都能拿到锁, 队列FIFO是一个完美的解决方案，也就是先进先出，
 * ReenTrantLock 也就是用队列实现的公平锁和非公平锁
 *
 */
public class T09_ReentrantLock5 extends Thread {

    //参数为true表示为公平锁，请对比输出结果
	private static ReentrantLock lock=new ReentrantLock(true);
    public void run() {
        for(int i=0; i<100; i++) {
            lock.lock();
            try{
                System.out.println(Thread.currentThread().getName()+"获得锁");
            }finally{
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        T09_ReentrantLock5 rl=new T09_ReentrantLock5();
        Thread th1= new Thread(rl);
        Thread th2= new Thread(rl);
        th1.start();
        th2.start();
    }
}
