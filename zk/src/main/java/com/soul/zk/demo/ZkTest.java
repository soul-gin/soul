package com.soul.zk.demo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZkTest {

    public static void main(String[] args) {

        //线程安全, 处理new ZooKeeper() 是个异步事件, 实际返回的 ZooKeeper 对象是还在连接中的(connecting......)
        final CountDownLatch cd = new CountDownLatch(1);

        //zk是有session概念的, 没有连接池的概念
        //集群ip信息通过逗号分割, session超时时间, watch: 观察 回调
        // watch: 观察,回调 分两类
        // watch的注册值发生在 读类型调用, get exists
        //第一类: new ZooKeeper()时传入, 这个watch是session级别的, 跟path, node没有关系
        try {
            final ZooKeeper zk = new ZooKeeper("192.168.25.106:2181,192.168.25.107:2181,192.168.25.108:2181", 3000, new Watcher() {
                //watch的回调方法
                @Override
                public void process(WatchedEvent event) {
                    Event.KeeperState state = event.getState();
                    String path = event.getPath();
                    Event.EventType type = event.getType();
                    System.out.println("default watch event=" + event.toString());

                    switch (state) {
                        case Unknown:
                            break;
                        case Disconnected:
                            break;
                        case NoSyncConnected:
                            break;
                        case SyncConnected:
                            System.out.println("connected");
                            //zk回调告诉我们连接成功后, 计数减一
                            cd.countDown();
                            break;
                        case AuthFailed:
                            break;
                        case ConnectedReadOnly:
                            break;
                        case SaslAuthenticated:
                            break;
                        case Expired:
                            break;
                        default:
                            System.out.println("null state");
                    }

                    switch (type) {
                        case None:
                            break;
                        case NodeCreated:
                            break;
                        case NodeDeleted:
                            break;
                        case NodeDataChanged:
                            break;
                        case NodeChildrenChanged:
                            break;
                        default:
                            System.out.println("null type");
                    }
                }
            });

            ////zk回调告诉我们连接成功后, 阻塞释放
            cd.await();
            //获取zk连接状态
            ZooKeeper.States state = zk.getState();
            switch (state) {
                case CONNECTING:
                    System.out.println("connecting......");
                    break;
                case ASSOCIATING:
                    break;
                case CONNECTED:
                    System.out.println("connected......");
                    break;
                case CONNECTEDREADONLY:
                    break;
                case CLOSED:
                    break;
                case AUTH_FAILED:
                    break;
                case NOT_CONNECTED:
                    break;
                default:
                    System.out.println("null state");
            }

            //1.路径, 2.节点存储的信息, 3.权限限制, 4.节点类型
            //目前设置的是临时节点, 且创建 ZooKeeper 节点的session time out是3秒, 则临时节点在client断开连接后3秒后会消失
            String pathName = zk.create("/ts", "oldMessage".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            //获取
            final Stat stat = new Stat();
            //获取数据, 并且注册watch
            byte[] node = zk.getData("/ts", new Watcher() {
                //这里注册的watch是一次性的
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("udf watch event=" + event);

                    try {
                        //如果想要watch一直有效, 那么在watch回调完成后, 需要再次注册
                        //true表示使用default watch(使用的是new ZooKeeper传递的watch)再次注册
                        zk.getData("/ts", true, stat);
                    }catch (KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, stat);
            System.out.println("node=" + new String(node));

            //修改: 1.路径 2.节点修改为什么信息 3.版本号
            //发生了watch
            Stat stat1 = zk.setData("/ts", "newMessage1".getBytes(), 0);
            //需要重新注册watch, 才能再次触发watch
            Stat stat2 = zk.setData("/ts", "newMessage2".getBytes(), stat1.getVersion());


            //异步回调
            System.out.println("-------async begin-------");
            zk.getData("/ts", false, new AsyncCallback.DataCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                    System.out.println("-------async call back-------");
                    System.out.println(ctx.toString());
                    System.out.println(new String(data));
                }
            }, "context/上下文");
            System.out.println("-------async over-------");

            //等待3秒以便输出回调信息
            Thread.sleep(3000);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
