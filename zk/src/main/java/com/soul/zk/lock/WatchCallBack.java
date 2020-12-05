package com.soul.zk.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallBack implements Watcher,
        AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {


    private ZooKeeper zk;

    private String threadName;

    private String pathName;

    CountDownLatch cd = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public void tryLock(){

        try {
            System.out.println("threadName=" + threadName + " create...");
            //这里也可以获取符目录里的信息, 查看是否和当前节点的名称相同, 相同则获得锁(这就是重入锁)
            zk.create("/lock", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL, this, "lock context");

            cd.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock(){
        try {
            //-1 表示不对版本做判定
            zk.delete(pathName, -1);
            System.out.println(threadName + " unLock...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void process(WatchedEvent event) {

        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //节点被删除
                //更新节点信息
                zk.getChildren("/", false, this, "children context");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            default:
                System.out.println("null type");

        }

    }

    //StringCallback 创建节点回调
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (null != name){
            System.out.println(threadName + "create node=" + name);
            pathName = name;
            zk.getChildren("/", false, this, "children context");
        }

    }

    //Children2Callback 查询子节点信息的回调
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        //创建完成自己的节点后, 必然能看到前面的节点信息
        System.out.println(threadName + " look locks ");
        //发现节点是乱序的
/*        for (String child : children) {
            System.out.println("child=" + child);
        }*/
        //应先排序
        Collections.sort(children);
        //pathName带有斜杠(/lock0000000011), 而children不带, 需要截取
        int index = children.indexOf(pathName.substring(1));
        if (index == 0){
            //是第一个(最小)的临时顺序节点
            System.out.println(threadName + " is current first...");
            try {
                //在对应临时序列锁的父节点上设置数据, 可以用于处理锁重入问题
                //另外避免执行过快, 后续锁还没注册上, 当前锁就已经释放了
                zk.setData("/", threadName.getBytes(), -1);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cd.countDown();

        } else {
            //不是第一个
            zk.exists("/"+children.get(index-1), this, this, "exists context");
        }


    }

    //StatCallback 处理exists的状态监控
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
