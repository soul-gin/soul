package com.soul.base.io.level04;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {

    SelectorThread[] sts;

    ServerSocketChannel server;

    AtomicInteger xid = new AtomicInteger(0);


    public SelectorThreadGroup(int num) {
        //num 线程数
        sts = new SelectorThread[num];
        for (int i = 0; i < num; i++) {
            sts[i] = new SelectorThread(this);
            //通过线程启动
            new Thread(sts[i]).start();
        }

    }


    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            //注册到哪个selector上
            nextSelectorV2(server);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void nextSelector(Channel ch) {

        SelectorThread st = next();
        try {
            //1.通过队列传递数据 消息(ch 有可能是 server 有可能是 client)
            st.lbq.put(ch);
            //2.通过打断阻塞, 让对应的线程自己去在打断后完成注册selector
            st.selector.wakeup();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void nextSelectorV2(Channel ch) {
        //针对 server 连接进行特殊处理
        if (ch instanceof ServerSocketChannel){
            try {
                //直接指定第一个线程, 作为server连接
                sts[0].lbq.put(ch);
                //打断阻塞
                sts[0].selector.wakeup();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //针对 client 连接进行处理
            SelectorThread st = nextV2();

            try {
                //1.通过队列传递数据 消息(ch 有可能是 server 有可能是 client)
                st.lbq.put(ch);
                //2.通过打断阻塞, 让对应的线程自己去在打断后完成注册selector
                st.selector.wakeup();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    //无论 serversocket socket 都可以复用这个方法
    private SelectorThread next() {
        //每个线程均可接收server 和 client
        int index = xid.incrementAndGet() % sts.length;
        return sts[index];
    }

    private SelectorThread nextV2() {
        //这里做处理, 可以避免server和client均注册在同一个线程
        //只返回后两个线程, 作为client连接; 直接指定第一个线程, 作为server连接
        int index = xid.incrementAndGet() % (sts.length - 1);
        return sts[index+1];
    }


}
