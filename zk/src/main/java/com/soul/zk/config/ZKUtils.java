package com.soul.zk.config;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKUtils {

    private static ZooKeeper zk;

    //直接连接zk的根目录(/)
    //private static String address = "192.168.25.106:2181,192.168.25.107:2181,192.168.25.108:2181";

    //指定 /ts 目录进行连接, 需要确保ts目录存在(手动创建: create /ts "")
    private static String address = "192.168.25.106:2181,192.168.25.107:2181,192.168.25.108:2181/ts";

    //默认的自定义watch
    private static DefaultWatch watch = new DefaultWatch();

    //等待zk创建成功
    private static CountDownLatch init = new CountDownLatch(1);

    public static ZooKeeper getZK(){
        try {
            watch.setCt(init);
            zk = new ZooKeeper(address, 3000, watch);
            init.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zk;
    }
}
