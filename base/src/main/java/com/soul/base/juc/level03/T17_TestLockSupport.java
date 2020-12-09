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
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

        try {
            TimeUnit.SECONDS.sleep(8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("after 8 senconds!");

        //解除停车
        //线程阻塞一段时间后, 放行
        LockSupport.unpark(t);

    }
}
