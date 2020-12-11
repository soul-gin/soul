package com.soul.base.juc.level03;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class T17_TestLockSupport {
    public static void main(String[] args) {
        Thread t = new Thread(()->{
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
                if(i == 5) {
                    //停车
                    //让线程阻塞住, 除非触发了或触发过 unpark()
                    LockSupport.park();
                } else if(i == 8) {
                    //第二次停车
                    //让线程阻塞住, 除非再次触发或触发过两次 unpark()
                    LockSupport.park();
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

        //如果注释掉当前线程的等待时间, 那么t线程park不会阻塞
        //这表示: unpark可以先与park执行
        try {
            TimeUnit.SECONDS.sleep(8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("after 8 senconds!");

        //解除停车
        //线程阻塞一段时间后, 放行指定线程(unpark可以叫醒指定线程, 优于notify只能叫醒队列中第一个线程)
        //实现是通过计数维护
        LockSupport.unpark(t);

    }
}
