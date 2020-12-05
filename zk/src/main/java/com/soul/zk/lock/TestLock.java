package com.soul.zk.lock;

import com.soul.zk.config.ZKUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLock {

    private ZooKeeper zk;

    @Before
    public void conn(){
        //指定 /lockTs 目录进行连接, 需要确保 lockTs 目录存在(手动创建: create /lockTs "")
        ZKUtils.setAddress("192.168.25.106:2181,192.168.25.107:2181,192.168.25.108:2181/lockTs");
        zk = ZKUtils.getZK();
    }


    @Test
    public void testLock(){

        for (int i = 0; i < 10; i++) {

            new Thread(){
                @Override
                public void run(){
                    WatchCallBack watchCallBack = new WatchCallBack();
                    watchCallBack.setZk(zk);

                    String threadName = Thread.currentThread().getName();
                    watchCallBack.setThreadName(threadName);
                    //每一个线程
                    //抢锁
                    watchCallBack.tryLock();

                    //干活
                    System.out.println("do something");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //释放锁
                    watchCallBack.unLock();

                }
            }.start();
        }


        while (true){

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    @After
    public void close(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
