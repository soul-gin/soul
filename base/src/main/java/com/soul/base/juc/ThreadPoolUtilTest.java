package com.soul.base.juc;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author gin
 * @date 2021/3/9
 */
public class ThreadPoolUtilTest {

    public static void main(String[] args) {
        //getCore();
        testPool();
        //testPoolError();
        //testPoolError2();
    }

    private static void getCore() {
        System.out.println(Runtime.getRuntime()
                .availableProcessors());
    }

    private static void testPool() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
                    System.out.println("test");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testPoolError() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("test");
                int a = 0;
                int b = 1/a;
            }
        });
    }

    private static void testPoolError2() {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("test2");
                int a = 0;
                int b = 1/a;
            }
        });
    }

}
