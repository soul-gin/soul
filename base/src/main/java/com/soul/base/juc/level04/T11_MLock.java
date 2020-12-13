package com.soul.base.juc.level04;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 通过AQS实现自定义锁，目前仅实现了lock和unlock
 */
public class T11_MLock implements Lock {

    private T13_Sync t13Sync = new T13_Sync();

    @Override
    public void lock() {
        t13Sync.acquire(1);
    }

    @Override
    public void unlock() {
        t13Sync.release(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
