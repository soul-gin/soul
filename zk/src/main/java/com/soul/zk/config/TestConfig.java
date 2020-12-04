package com.soul.zk.config;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestConfig {

    private ZooKeeper zk;

    @Before
    public void conn(){
        zk = ZKUtils.getZK();
    }


    @Test
    public void getConf(){

        //用于zk调用时需要的watch及callkack的抽象
        WatchCallBack watchCallBack = new WatchCallBack();
        watchCallBack.setZk(zk);

        //zk获取的信息存放对象
        MyConf myConf = new MyConf();
        watchCallBack.setMyConf(myConf);


        watchCallBack.existsWait();

        //create /ts ""
        //测试数据读取
        //create /ts/AppConf "old"
        //测试配置变更是否能读取到最新数据, 发现打印新的数据
        //set /ts/AppConf "new"
        //测试删除, 发现打印等待
        //delete /ts/AppConf
        //测试重新建立
        //create /ts/AppConf "new2"
        while (true){
            //发现数据被清除
            if ("".equals(myConf.getConf())){
                System.out.println("数据被删除了...");
                watchCallBack.existsWait();
            }

            //获取数据
            System.out.println("myConf=" + myConf.getConf());
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
