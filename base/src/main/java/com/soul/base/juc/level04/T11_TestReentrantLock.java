package com.soul.base.juc.level04;

import java.util.concurrent.locks.ReentrantLock;

/**
 * AQS ( AbstractQueuedSynchronizer ), 主要涉及
 * tryAcquire tryRelease tryAcquireShared addWaiter
 * 涉及到的设计模式:
 * Template Method
 * Callback Function
 * 父类默认实现
 * 子类具体实现
 * 注意: jdk1.9之后, 通过 VarHandle 实现node的原子操作(VarHandle cas方式c c++实现, 直接操作二进制码, 效率优于反射(需要校验))
 *
 * 公平锁: 新的线程来了, 先查看等待队列里面是否有线程在排队, 有则排队的先执行, 自己添加至队尾
 * 非公平锁: 新的线程来了, 不查看等待队列, 直接进行锁的争抢(争抢对象是队列头节点),
 * 如新来节点未抢到则排至队列末端, 如抢到则新来的线程先执行(是否公平针对的是新来的线程, 如无新来线程每次从队列头节点获取线程获得锁)
 *
 * AQS volatile state(AQS使用CAS和volatile实现: state和队列新增节点的操作均通过CAS保证一致性,并未使用锁)
 * (不同子类根据state可以有不同实现: 可重入锁 0->1表示加了锁, 1->2表示重入加锁, 2->1重入锁释放, 1->0锁释放)
 */
public class T11_TestReentrantLock {

    private static volatile int i = 0;

    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        // lock实际调用是 AbstractQueuedSynchronizer.tryAcquire() 的父类方法
        // 直接看子类是抛异常
        lock.lock();
        //synchronized (T11_TestReentrantLock.class) {
            i++;
        //}

        lock.unlock();

        //synchronized 程序员的丽春院 JUC
    }
}
