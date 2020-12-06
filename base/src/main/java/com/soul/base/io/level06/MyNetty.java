package com.soul.base.io.level06;

import io.netty.buffer.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyNetty {


    @Test
    public void myByteBuffer(){
        //netty的 ByteBuf 优于jdk的api, 不需要翻转使用了, 读写更加方便
        //第一个为初始容量, 第二个为最大容量
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(5, 10);
        print(buf);

        //向buf中写入测试, 写入4字节未达初始容量
        buf.writeBytes(new byte[]{0,1,2,3});
        print(buf);


        //向buf中写入测试, 再写入1字节达初始容量, 显示不可写, 但可突破
        buf.writeBytes(new byte[]{4});
        print(buf);

        //向buf中写入测试, 再写入4字节超过初始容量
        buf.writeBytes(new byte[]{5,6,7,8});
        print(buf);

        //向buf中写入测试, 再写入1字节达最大容量, 这时是再写入要报错了
        buf.writeBytes(new byte[]{9});
        print(buf);
    }

    @Test
    public void myByteBuffer2(){
        //netty的 ByteBuf 优于jdk的api, 不需要翻转使用了, 读写更加方便
        //第一个为初始容量, 第二个为最大容量
        //是否池化: 非池化
        //ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(5, 10);
        //池化
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(5, 10);
        print(buf);

        //向buf中写入测试, 写入4字节未达初始容量
        buf.writeBytes(new byte[]{0,1,2,3});
        print(buf);


        //向buf中写入测试, 再写入1字节达初始容量, 显示不可写, 但可突破
        buf.writeBytes(new byte[]{4});
        print(buf);

        //向buf中写入测试, 再写入4字节超过初始容量
        buf.writeBytes(new byte[]{5,6,7,8});
        print(buf);

        //向buf中写入测试, 再写入1字节达最大容量, 这时是再写入要报错了
        buf.writeBytes(new byte[]{9});
        print(buf);
    }


    public static void print(ByteBuf buf){
        //是否可读
        System.out.println("isReadable=" + buf.isReadable());
        //读取的索引位置(从哪开始读)
        System.out.println("readerIndex=" + buf.readerIndex());
        //可以读多少字节
        System.out.println("readerIndex=" + buf.readableBytes());
        //是否可以写
        System.out.println("isWritable=" + buf.isWritable());
        //可写索引位置(从哪开始写)
        System.out.println("writerIndex=" + buf.writerIndex());
        //可以写多少字节
        System.out.println("writableBytes=" + buf.writableBytes());
        //动态分配的上限
        System.out.println("capacity=" + buf.capacity());
        //最终的上限
        System.out.println("maxCapacity=" + buf.maxCapacity());
        //是否堆外直接分配: true-堆外内存, false-堆内内存
        System.out.println("isDirect=" + buf.isDirect());
        System.out.println("--------");

    }


    /**
     * NioEventLoopGroup test
     */
    @Test
    public void loopExecutor(){
        //线程池, 实际根据指定的数字创建, 目前只会创建1个线程
        NioEventLoopGroup selector = new NioEventLoopGroup(1);
        //因为只有1个线程, 且不会扩容, 所以只会打印 hello_1
        selector.execute(() -> {
            System.out.println("hello_1");
            try {
                while(true){
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //不会打印 hello_1
        selector.execute(() -> {
            System.out.println("hello_2");
            try {
                while(true){
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //阻塞, 防止当前线程退出
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端
     * 连接别人
     * 1. 主动发送数据
     * 2. 别人什么时候发送给我 (通过 event 响应式处理比较优雅, selector)
     */
    @Test
    public void clientMode(){
        //测试: 在 192.168.25.101 的shell界面使用nc创建服务端:
        // nc -l 192.168.25.101 9090

        //线程池, 实际根据指定的数字创建, 目前只会创建1个线程
        NioEventLoopGroup thread = new NioEventLoopGroup(1);

        //客户端模式
        NioSocketChannel client = new NioSocketChannel();

        //客户端注册到 an event loop
        thread.register(client);

        //获取读的channel
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());

        try {
            //reactor 异步特征, 连接是异步的,
            ChannelFuture connect = client.connect(new InetSocketAddress("192.168.25.101", 9090));
            //需要通过 Future 确认连接成功, 注意linux防火墙是否关闭
            ChannelFuture sync = connect.sync();

            ByteBuf buf = Unpooled.copiedBuffer("hello server, I`m client".getBytes());
            ChannelFuture send = client.writeAndFlush(buf);
            //确保数据发送成功
            send.sync();

            //等待客户端断开
            sync.channel().closeFuture().sync();
            System.out.println("client over...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void serverMode(){

        //测试: 在 192.168.25.101 的shell界面使用nc创建客户端:
        // nc 192.168.25.1 9090

        //线程池, 实际根据指定的数字创建, 目前只会创建1个线程
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();

        //服务端注册到 an event loop
        thread.register(server);

        //响应式
        ChannelPipeline p = server.pipeline();
        //accept接收客户端, 并且注册到selector上
        p.addLast(new MyAcceptHandler(thread, new MyChannelInit()));

        //server端绑定端口
        ChannelFuture bind = server.bind(new InetSocketAddress("192.168.25.1", 9090));
        try {
            //等待绑定成功, 且等待至最终客户端退出
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
