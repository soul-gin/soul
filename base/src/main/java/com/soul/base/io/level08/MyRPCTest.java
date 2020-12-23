package com.soul.base.io.level08;

import com.soul.base.io.level08.proxy.MyProxy;
import com.soul.base.io.level08.rpc.Dispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1. 假设一个需求, 写一个RPC
 * 2. 来回通讯, 连接数量, 拆包
 * 3. 动态代理, 序列化, 协议封装
 * 4. 连接池
 * 5. 就像调用本地方法一样去调用远程方法, 面向java就是所谓的面向interface编程
 *
 * 注意: netty channel read 不能保证每次读取的数据的完整性,
 * 而且不是一次read处理一个message, 内核有自己的缓冲区大小,
 * 每次从内核拉取的数据可能是10.4 个message, 即会出现某个message被从某个位置切断
 * 但是第二次再从内核拉取时, 两次拉取数据拼接在一起时, 中间被切断的数据是可以保证完整的
 * (前后read能保证数据完整性)
 * 解决方案: 将不足的body和header留到下一次读取
 */
public class MyRPCTest {

    //模拟server端(提供方)
    @Test
    public void startServer(){

        //具体服务端的实现类
        MyCar car = new MyCar();
        //设置全局Map信息
        Dispatcher dis = Dispatcher.getDis();
        dis.register(Car.class.getName(), car);

        NioEventLoopGroup boss = new NioEventLoopGroup(10);
        NioEventLoopGroup work = boss;
        ServerBootstrap sbs = new ServerBootstrap();
        ChannelFuture bind = sbs.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        //System.out.println("server accept client port=" + channel.remoteAddress().getPort());
                        ChannelPipeline p = channel.pipeline();
                        //编解码器
                        p.addLast(new ServerDecode());
                        //数据处理, 添加全局参数
                        p.addLast(new ServerRequestHandler(dis));
                    }
                }).bind(new InetSocketAddress("localhost", 9090));

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //模拟consumer端(调用方)
    @Test
    public void getCarInfo(){
        new Thread(() -> {
            startServer();
        }).start();

        System.out.println("server started......");

        //模拟并发情况下, 可能会出现header切割异常问题, 需要处理
        int size = 20;
        AtomicInteger count = new AtomicInteger(0);
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                //动态代理实现
                Car car = MyProxy.proxyGet(Car.class);
                String reqMsg = "hello" + count.incrementAndGet();
                String carInfo = car.getCarInfo(reqMsg);
                System.out.println("client over resMsg=" + carInfo + " reqMsg=" + reqMsg);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
