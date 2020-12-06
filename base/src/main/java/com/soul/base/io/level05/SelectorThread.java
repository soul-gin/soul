package com.soul.base.io.level05;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectorThread extends ThreadLocal<LinkedBlockingQueue<Channel>> implements Runnable {
    // 每个线程对应着一个selector
    // 多线程情况下,该主机, 该程序的并发客户端被分配到多个selector上
    // 注意, 每个客户端, 只绑定到其中一个selector
    // 其实不会有交互问题

    @Override
    protected LinkedBlockingQueue<Channel> initialValue() {
        //每个线程持有自己的独立对象, 这里可丰富处理
        return new LinkedBlockingQueue<>();
    }

    Selector selector = null;

    LinkedBlockingQueue<Channel> lbq = get();

    SelectorThreadGroup stg;

    public SelectorThread(SelectorThreadGroup stg) {
        try {
            this.stg = stg;
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        //Loop
        while(true){
            try {
                //1.select()
                // 会阻塞, 需要 wakeup() 才能继续后面的逻辑
                int nums = selector.select();


                //2.处理selectKeys
                //如果有读取事件
                if (nums > 0){
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while(iter.hasNext()){
                        //线程处理过程
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()){
                            //接受客户端的过程(接收之后需要注册)
                            acceptHandler(key);

                        } else if (key.isReadable()){
                            readHandler(key);
                        } else if (key.isWritable()){

                        }

                    }
                }

                //3.处理一些task
                //处理注册
                if (!lbq.isEmpty()){
                    Channel c = lbq.take();
                    //根据类型区分
                    if (c instanceof ServerSocketChannel){
                        //注册server端
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + " register listen ... ");
                    } else if (c instanceof SocketChannel){
                        //注册client端
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer bf = ByteBuffer.allocateDirect(4096);
                        client.register(selector, SelectionKey.OP_READ, bf);
                        System.out.println(Thread.currentThread().getName() + " register client : " + client.getRemoteAddress());
                    }


                }


            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void readHandler(SelectionKey key) {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        //清理
        buffer.clear();
        while(true){
            try {
                int num = client.read(buffer);
                if (num > 0){
                    //将读取到的内容翻转, 然后直接写出
                    buffer.flip();
                    //有数据则持续写出
                    while(buffer.hasRemaining()){
                        client.write(buffer);
                    }
                    //写完清理
                    buffer.clear();
                } else if (num == 0 ){
                    //读完则跳出
                    break;
                } else if (num < 0){
                    //客户端断开
                    System.out.println("client: " + client.getRemoteAddress() + " closed......");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void acceptHandler(SelectionKey key){

        System.out.println(Thread.currentThread().getName() + " acceptHandler ... ");

        ServerSocketChannel server = (ServerSocketChannel)key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);

            //选择一个多路复用器进行注册
            stg.nextSelector(client);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWorker(SelectorThreadGroup stgWorker) {
        this.stg = stgWorker;
    }
}
