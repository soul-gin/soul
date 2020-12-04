package com.soul.zk.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    private ZooKeeper zk;

    private MyConf myConf;

    private CountDownLatch cd = new CountDownLatch(1);;

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setMyConf(MyConf myConf) {
        this.myConf = myConf;
    }

    //DataCallback  数据查询回调
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        //数据不为空则将获取到的数据设置到 myConf (一般配置较为复杂, 需要特定处理, 封装成对象)
        if (null != data){
            String rs = new String(data);
            myConf.setConf(rs);
            cd.countDown();
        }

    }

    //StatCallback  节点状态查询回调
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (null != stat){
            //节点状态不为null, 表示节点存在, 则获取节点数据
            zk.getData("/AppConf", this, this, "udf context");
        }

    }

    //Watcher  变更事件回调
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //节点被删除(具体看业务是否能容忍节点被删除)
                //假设: 业务需要配置被删除, 程序等待新的配置设置进来
                myConf.setConf("");
                //重新赋值, 以便阻塞到配置有数据的时候
                cd = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                //节点数据变更
                zk.getData("/AppConf", this, this, "udf context");
                break;
            case NodeChildrenChanged:
                break;
            default:
                System.out.println("null type");
        }
    }


    public void existsWait(){
        //因util中的address中配置的是 /ts , 所以使用api时默认会带上 /ts 这个前缀
        zk.exists("/AppConf", this, this, "udf context");
        try {
            cd.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
