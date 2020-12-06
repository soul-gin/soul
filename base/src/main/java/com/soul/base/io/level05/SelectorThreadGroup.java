package com.soul.base.io.level05;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {

    SelectorThread[] sts;

    ServerSocketChannel server;

    AtomicInteger xid = new AtomicInteger(0);

    //不重新赋值, 则boss和worker不进行区分
    SelectorThreadGroup stg = this;

    //boss 和 worker 区分开, 通过赋值
    public void setWorker(SelectorThreadGroup stg){
        this.stg = stg;
    }


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
            nextSelector(server);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void nextSelector(Channel ch) {

        if (ch instanceof ServerSocketChannel){
            try {
                //listen 选择了 boss 组中的一个线程后, 要更新这个线程的worker组
                SelectorThread st = next();
                st.setWorker(stg);

                //1.通过队列传递数据 消息(ch 有可能是 server 有可能是 client)
                st.lbq.put(ch);
                //2.通过打断阻塞, 让对应的线程自己去在打断后完成注册selector
                st.selector.wakeup();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //针对 client 连接进行处理
            SelectorThread st = nextV3();

            //1.通过队列传递数据 消息(ch 有可能是 server 有可能是 client)
            st.lbq.add(ch);
            //2.通过打断阻塞, 让对应的线程自己去在打断后完成注册selector
            st.selector.wakeup();
        }

    }


    private SelectorThread next() {
        //使用当前boss线程组
        int index = xid.incrementAndGet() % sts.length;
        return sts[index];
    }

    private SelectorThread nextV3() {
        //使用worker线程组
        int index = xid.incrementAndGet() % stg.sts.length;
        return stg.sts[index];
    }


}
